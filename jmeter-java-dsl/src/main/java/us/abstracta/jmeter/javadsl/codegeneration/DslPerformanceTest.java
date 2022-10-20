package us.abstracta.jmeter.javadsl.codegeneration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslPerformanceTest {

  private final MethodCall testPlanMethodCall;
  private final Map<Class<?>, String> dependencies;

  public DslPerformanceTest(MethodCall testPlanMethodCall, Map<Class<?>, String> dependencies) {
    this.testPlanMethodCall = testPlanMethodCall;
    this.dependencies = dependencies;
  }

  public String buildCode() {
    return String.format(findTestClassTemplate(), buildDependencies(), buildStaticImports(),
        buildImports(), buildMethodDefinitions(), buildTestMethodCall());
  }

  private String buildDependencies() {
    TreeSet<String> dependencyPaths = new TreeSet<>(Arrays.asList(
        "org.junit.jupiter:junit-jupiter-engine:5.9.0",
        "org.junit.platform:junit-platform-launcher:1.9.0",
        "org.assertj:assertj-core:3.23.1"
    ));
    dependencyPaths.addAll(dependencies.entrySet().stream()
        .filter(e -> testPlanMethodCall.getStaticImports().contains(e.getKey().getName())
            || testPlanMethodCall.getImports().contains(e.getKey().getName()))
        .map(Entry::getValue)
        .collect(Collectors.toList()));
    return dependencyPaths.stream()
        .map(d -> "//DEPS " + d)
        .collect(Collectors.joining("\n"));
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

  private String buildStaticImports() {
    TreeSet<String> imports = new TreeSet<>();
    imports.add("org.assertj.core.api.Assertions.assertThat");
    imports.addAll(testPlanMethodCall.getStaticImports().stream()
        .map(c -> c + ".*")
        .collect(Collectors.toList()));
    return buildImportsCode(imports, "static ");
  }

  private String buildImportsCode(TreeSet<String> imports, String importModifier) {
    return imports.stream()
        .map(s -> "import " + importModifier + s.replace("$", ".") + ";")
        .collect(Collectors.joining("\n"));
  }

  private String buildImports() {
    TreeSet<String> imports = new TreeSet<>(Arrays.asList(
        "org.junit.jupiter.api.Test",
        "org.junit.platform.engine.discovery.DiscoverySelectors",
        "org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder",
        "org.junit.platform.launcher.core.LauncherFactory",
        "org.junit.platform.launcher.listeners.SummaryGeneratingListener",
        "org.junit.platform.launcher.listeners.TestExecutionSummary"
    ));
    Set<String> classes = new HashSet<>(testPlanMethodCall.getImports());
    classes.addAll(Arrays.asList(IOException.class.getName(), PrintWriter.class.getName(),
        TestPlanStats.class.getName()));
    imports.addAll(classes);
    return buildImportsCode(imports, "");
  }

  private String buildMethodDefinitions() {
    Map<String, MethodCall> methodDefinitions = testPlanMethodCall.getMethodDefinitions();
    return methodDefinitions.isEmpty() ? ""
        : "\n " + testPlanMethodCall.getMethodDefinitions().entrySet().stream()
            .map(e -> String.format("  private %s %s() {\n"
                    + "    return %s;\n"
                    + "  }\n", e.getValue().getReturnType().getSimpleName(), e.getKey(),
                e.getValue().buildCode("      ")))
            .collect(Collectors.joining("\n"));
  }

  private String buildTestMethodCall() {
    String indent = "      ";
    String testPlanCode = testPlanMethodCall.buildCode(indent);
    return testPlanCode.matches("\\s+\\)$") ? testPlanCode : testPlanCode + "\n" + indent;
  }

}
