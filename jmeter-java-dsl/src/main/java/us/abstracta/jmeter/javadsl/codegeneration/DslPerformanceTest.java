package us.abstracta.jmeter.javadsl.codegeneration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslPerformanceTest {

  private final MethodCall testPlanMethodCall;

  public DslPerformanceTest(MethodCall testPlanMethodCall) {
    this.testPlanMethodCall = testPlanMethodCall;
  }

  public String buildCode() {
    String indent = "      ";
    String testPlanCode = testPlanMethodCall.buildCode(indent);
    return String.format(findTestClassTemplate(),
        buildStaticImports(testPlanMethodCall.getStaticImports()),
        buildImports(testPlanMethodCall.getImports()),
        testPlanCode.matches("\\s+\\)$") ? testPlanCode : testPlanCode + "\n" + indent);
  }

  private String findTestClassTemplate() {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(getClass().getResourceAsStream("/TestClass.template.java"),
            StandardCharsets.UTF_8))) {
      return reader.lines()
          .collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String buildStaticImports(Set<Class<?>> staticImportClasses) {
    TreeSet<String> imports = new TreeSet<>();
    imports.add("org.assertj.core.api.Assertions.assertThat");
    imports.addAll(staticImportClasses.stream()
        .map(c -> c.getName() + ".*")
        .collect(Collectors.toList()));
    return buildImportsCode(imports, "static ");
  }

  private String buildImportsCode(TreeSet<String> imports, String importModifier) {
    return imports.stream()
        .map(s -> "import " + importModifier + s.replace("$", ".") + ";")
        .collect(Collectors.joining("\n"));
  }

  private String buildImports(Set<Class<?>> importClasses) {
    TreeSet<String> imports = new TreeSet<>();
    imports.add("org.junit.jupiter.api.Test");
    Set<Class<?>> classes = new HashSet<>(importClasses);
    classes.addAll(Arrays.asList(IOException.class, TestPlanStats.class));
    imports.addAll(classes.stream()
        .map(Class::getName)
        .collect(Collectors.toList()));
    return buildImportsCode(imports, "");
  }

}
