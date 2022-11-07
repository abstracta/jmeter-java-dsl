package us.abstracta.jmeter.javadsl.codegeneration;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class DslPerformanceTest {

  private final MethodCall testPlanMethodCall;
  private final Map<Class<?>, String> dependencies;

  public DslPerformanceTest(MethodCall testPlanMethodCall, Map<Class<?>, String> dependencies) {
    this.testPlanMethodCall = testPlanMethodCall;
    this.dependencies = dependencies;
  }

  public String buildCode() {
    try {
      return new StringTemplate(new TestResource("TestClass.template.java").contents())
          .bind("dependencies", buildDependencies())
          .bind("staticImports", buildStaticImports())
          .bind("imports", buildImports())
          .bind("methodDefinitions", buildMethodDefinitions())
          .bind("testPlan", buildTestPlanCode())
          .solve();
    } catch (IOException e) {
      // Only would happen if can't access resource that should be included in jar
      throw new RuntimeException(e);
    }
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
        : "\n" + testPlanMethodCall.getMethodDefinitions().entrySet().stream()
            .map(e -> buildMethodDefinitionCode(e.getKey(), e.getValue()))
            .collect(Collectors.joining("\n"));
  }

  private static String buildMethodDefinitionCode(String methodName, MethodCall methodCall) {
    return String.format("%1sprivate %s %s() {\n"
            + "%1$s%1$sreturn %s;\n"
            + "%1$s}\n", MethodCall.INDENT, methodCall.getReturnType().getSimpleName(), methodName,
        MethodCall.decreaseLastParenthesisIndentation(methodCall.buildCode(indentLevel(3))));
  }

  private static String indentLevel(int level) {
    StringBuilder ret = new StringBuilder();
    for (int i = 0; i < level; i++) {
      ret.append(MethodCall.INDENT);
    }
    return ret.toString();
  }

  private String buildTestPlanCode() {
    String indent = indentLevel(3);
    String testPlanCode = MethodCall.decreaseLastParenthesisIndentation(
        testPlanMethodCall.buildCode(indent));
    return testPlanCode.endsWith(MethodCall.INDENT + ")") ? testPlanCode
        : testPlanCode + "\n" + indent;
  }

}
