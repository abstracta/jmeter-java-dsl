package us.abstracta.jmeter.javadsl.codegeneration;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentController;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;

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

  @NotNull
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
                m -> m.getBody().map(Objects::toString).orElse("")));
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
    String ret = code.replaceAll("\n\\s+", " ").replaceAll("\\(\\s*", "(")
        .replaceAll("\\s*\\)", ")").replaceAll("\\s*\\.", ".");
    return "{\n    return " + ret + ";\n}";
  }

  public static String buildTestPlanJmx(String... childrenJmx) throws IOException {
    return new StringTemplate(testResourceContents("test-plan.template.jmx"))
        .bind("children", String.join("\n", childrenJmx))
        .solve();
  }

  public static String testResourceContents(String resourceName) {
    try {
      return testResource(resourceName.startsWith("/") ? resourceName.substring(1)
          : "codegeneration/" + resourceName).contents();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String jmx2dsl(String testPlanJmx, Path tmp) throws IOException {
    Path testPlanPath = tmp.resolve("testplan.jmx");
    Files.write(testPlanPath, testPlanJmx.getBytes(StandardCharsets.UTF_8));
    return new DslCodeGenerator().generateCodeFromJmx(testPlanPath.toFile());
  }

  public static String buildHttpSamplerJmx() {
    return testResourceContents("http-sampler.jmx");
  }

  public static String buildTestPlanDsl(String method, String child) {
    return buildTestPlanDsl(Collections.singletonList(method), Collections.singletonList(child));
  }

  public static String buildTestPlanDsl(List<String> methods, List<String> children) {
    return new TestClassTemplate()
        .dependencies(Collections.singleton("us.abstracta.jmeter:jmeter-java-dsl"))
        .staticImports(Collections.singleton(DslTestFragmentController.class.getName()))
        .imports(Collections.singleton(DslTestFragmentController.class.getName()))
        .methodDefinitions(methods)
        .testPlan(buildTestPlanMethodCode(children))
        .solve();
  }

  private static String buildTestPlanMethodCode(List<String> children) {
    String childIndent = Indentation.indentLevel(4);
    return String.format("testPlan(\n%s\n%s)", children.stream()
        .map(c -> Indentation.indent(c, childIndent))
        .collect(Collectors.joining(",\n")), Indentation.indentLevel(2));
  }

  public static String buildHttpSamplerDsl() {
    return testResourceContents("HttpSamplerDsl.java");
  }

}
