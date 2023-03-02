package us.abstracta.jmeter.javadsl.codegeneration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;

/**
 * Handles resolution of junit 5 test class code generation.
 *
 * @since 1.3
 */
public class TestClassTemplate {

  private final Set<String> dependencies = new TreeSet<>();
  private final Set<String> staticImports = new TreeSet<>();
  private final Set<String> imports = new TreeSet<>();
  private final List<String> methodDefinitions = new ArrayList<>();
  private String testPlan;

  public static TestClassTemplate fromTestPlanMethodCall(MethodCall testPlan,
      Map<Class<?>, String> dependencies) {
    return new TestClassTemplate()
        .dependencies(testPlanUsedDependencies(testPlan, dependencies))
        .staticImports(testPlan.getStaticImports())
        .imports(testPlan.getImports())
        .methodDefinitions(testPlanMethodDefinitions(testPlan))
        .testPlan(testPlanCode(testPlan));
  }

  private static Set<String> testPlanUsedDependencies(MethodCall testPlan,
      Map<Class<?>, String> dependencies) {
    return dependencies.entrySet().stream()
        .filter(e -> testPlan.getStaticImports().contains(e.getKey().getName())
            || testPlan.getImports().contains(e.getKey().getName()))
        .map(Entry::getValue)
        .collect(Collectors.toSet());
  }

  private static List<String> testPlanMethodDefinitions(MethodCall testPlan) {
    return testPlan.getMethodDefinitions().entrySet().stream()
        .map(e -> buildMethodDefinitionCode(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }

  private static String buildMethodDefinitionCode(String methodName, MethodCall methodCall) {
    return String.format("private %s %s() {\n"
            + "%sreturn %s;\n"
            + "}", methodCall.getReturnType().getSimpleName(), methodName, Indentation.INDENT,
        methodCall.buildAssignmentCode(Indentation.indentLevel(2)));
  }

  private static String testPlanCode(MethodCall testPlan) {
    String indent = Indentation.indentLevel(3);
    String ret = testPlan.buildAssignmentCode(indent);
    return ret.endsWith(Indentation.INDENT + ")") ? ret : ret + "\n" + indent;
  }

  public TestClassTemplate dependencies(Set<String> dependencies) {
    this.dependencies.addAll(dependencies);
    return this;
  }

  public TestClassTemplate staticImports(Set<String> imports) {
    staticImports.addAll(imports);
    return this;
  }

  public TestClassTemplate imports(Set<String> imports) {
    this.imports.addAll(imports);
    return this;
  }

  public TestClassTemplate methodDefinitions(List<String> methods) {
    methodDefinitions.addAll(methods);
    return this;
  }

  public TestClassTemplate testPlan(String testPlanCode) {
    this.testPlan = testPlanCode;
    return this;
  }

  public String solve() {
    try {
      Map<Boolean, List<String>> defaultImports = Arrays.stream(
              resourceContents("/default-imports.txt")
                  .split("\n"))
          .filter(s -> !s.isEmpty())
          .collect(Collectors.partitioningBy(s -> s.startsWith("static ")));
      return new StringTemplate(resourceContents("/TestClass.template.java"))
          .bind("dependencies", buildDependencies())
          .bind("staticImports", buildStaticImports(defaultImports.get(true)))
          .bind("imports", buildImports(defaultImports.get(false), imports))
          .bind("methodDefinitions", buildMethodDefinitions(methodDefinitions))
          .bind("testPlan", testPlan)
          .ignoreMissingBindings()
          .solve();
    } catch (IOException e) {
      // Only would happen if can't access resource that should be included in jar
      throw new RuntimeException(e);
    }
  }

  private String resourceContents(String resource) {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(getClass().getResourceAsStream(resource), StandardCharsets.UTF_8))) {
      return reader.lines()
          .collect(Collectors.joining("\n"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String buildDependencies() throws IOException {
    TreeSet<String> ret = new TreeSet<>();
    ret.addAll(Arrays.asList(resourceContents("/default-dependencies.txt")
        .split("\n")));
    ret.addAll(dependencies);
    return ret.stream()
        .map(d -> "//DEPS " + d)
        .collect(Collectors.joining("\n"));
  }

  private String buildStaticImports(List<String> defaultImports) {
    Set<String> additionalImports = staticImports.stream()
        .map(s -> String.format("static %s.*", s))
        .collect(Collectors.toSet());
    return buildImports(defaultImports, additionalImports);
  }

  private String buildImports(List<String> defaultImports, Set<String> imports) {
    TreeSet<String> ret = new TreeSet<>(defaultImports);
    ret.addAll(imports);
    return ret.stream()
        .map(i -> String.format("import %s;", i.replace("$", ".")))
        .collect(Collectors.joining("\n"));
  }

  private String buildMethodDefinitions(List<String> methods) {
    return methods.stream()
        .map(m -> String.format("\n%s\n", Indentation.indent(m, Indentation.INDENT)))
        .collect(Collectors.joining());
  }

}
