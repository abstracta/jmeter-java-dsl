package us.abstracta.jmeter.javadsl.codegeneration;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

@TestInstance(Lifecycle.PER_CLASS)
public abstract class MethodCallBuilderTest {

  @TempDir
  public Path tempDir;
  protected final DslCodeGenerator codeGenerator;

  protected MethodCallBuilderTest() {
    codeGenerator = new DslCodeGenerator();
  }

  private Stream<Arguments> findCodeBuilderTests() {
    Map<String, Method> methods = extractBuilderTestMethods();
    Map<String, String> methodCodes = extractCodeBuilderTestCodes();
    return methods.entrySet().stream()
        .map(m -> Arguments.of(m.getKey(), m.getValue(), methodCodes.get(m.getKey())));
  }

  private Map<String, Method> extractBuilderTestMethods() {
    return Arrays.stream(getClass().getDeclaredMethods())
        .collect(Collectors.toMap(Method::getName, m -> m));
  }

  private Map<String, String> extractCodeBuilderTestCodes() {
    Class<?> testsClass = getClass();
    SourceRoot sourceRoot = new SourceRoot(
        CodeGenerationUtils.mavenModuleRoot(testsClass).resolve("src/test/java"));
    String testsClassName = testsClass.getSimpleName();
    String declaringClassName = testsClass.getDeclaringClass().getSimpleName();
    CompilationUnit parsed = sourceRoot.parse(testsClass.getPackage().getName(),
        declaringClassName + ".java");
    return parsed.getClassByName(declaringClassName)
        // This should never happen since name of file should always match class name
        .orElseThrow(
            () -> new RuntimeException("Class " + declaringClassName + " not found"))
        .getMembers().stream()
        .filter(
            m -> m instanceof ClassOrInterfaceDeclaration && testsClassName.equals(
                ((ClassOrInterfaceDeclaration) m).getNameAsString()))
        .map(m -> (ClassOrInterfaceDeclaration) m)
        .findAny()
        // This should never happen because extractBuilderTestMethods should fail before
        .orElseThrow(() -> new RuntimeException("No " + testsClassName + " inner class found."))
        .getMethods().stream()
        .collect(
            Collectors.toMap(NodeWithSimpleName::getNameAsString,
                m -> m.getBody().map(Objects::toString).orElse("").replace("\r\n", "\n")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("findCodeBuilderTests")
  public void shouldGetOriginalCodeWhenSaveJmxAndGenerateCode(String testName, Method builderMethod,
      String methodCode) throws Exception {
    Path jmxPath = tempDir.resolve("test.jmx");
    ((DslTestPlan) builderMethod.invoke(this)).saveAsJmx(jmxPath.toString());
    assertThat(
        buildMethodBodyWith(codeGenerator.buildMethodCallFromJmxFile(jmxPath.toFile()).buildCode()))
        .isEqualTo(methodCode);
  }

  private String buildMethodBodyWith(String code) {
    // we reformat code to match MethodDeclaration.getBody reformatted code
    String ret = code
        .replaceAll("(\n\\s*)//([^\n]+)\n\\s*(\\.)?", "$1$3//$2<EOC>")
        .replaceAll("\n\\s+", " ")
        .replaceAll("\\(\\s*", "(")
        .replaceAll("\\s*\\)", ")")
        .replaceAll("\\s*\\.", ".")
        .replace("<EOC>", "\n    ");
    return "{\n    return " + ret + ";\n}";
  }

}
