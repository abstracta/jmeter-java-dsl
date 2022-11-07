package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.codegeneration.DslCodeGenerator;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;

public class DslTestFragmentControllerTest {

  @Nested
  public class CodeBuilderTest {

    private static final String DEFAULT_FRAGMENT_NAME = "Test Fragment";

    @Test
    public void shouldGenerateDslWithFragmentMethodWhenConvertTestPlanWithFragment(
        @TempDir Path tmp)
        throws IOException {
      String testPlanJmx = buildTestPlanJmx(
          buildFragmentJmx(DEFAULT_FRAGMENT_NAME, buildHttpSamplerJmx()));
      String methodName = "testFragment";
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              testFragmentMethodDsl(methodName, DEFAULT_FRAGMENT_NAME, buildHttpSamplerDsl()),
              methodName + "()"));
    }

    private String buildTestPlanJmx(String... childrenJmx) throws IOException {
      return new StringTemplate(testResourceContents("base-test-plan.template.jmx"))
          .bind("children", String.join("\n", childrenJmx))
          .solve();
    }

    private String testResourceContents(String resourceName) throws IOException {
      return testResource(resourceName.startsWith("/") ? resourceName.substring(1)
          : "codegeneration/fragments/" + resourceName).contents();
    }

    private String buildFragmentJmx(String name, String... childrenJmx) throws IOException {
      return new StringTemplate(testResourceContents("fragment.template.jmx"))
          .bind("name", name)
          .bind("children", String.join("\n", childrenJmx))
          .solve();
    }

    private String buildHttpSamplerJmx() throws IOException {
      return testResourceContents("http-sampler.jmx");
    }

    private String jmx2dsl(String testPlanJmx, Path tmp) throws IOException {
      Path testPlanPath = tmp.resolve("testplan.jmx");
      Files.write(testPlanPath, testPlanJmx.getBytes(StandardCharsets.UTF_8));
      return new DslCodeGenerator().generateCodeFromJmx(testPlanPath.toFile());
    }

    private String buildTestPlanDsl(String method, String child) throws IOException {
      return buildTestPlanDsl(Collections.singletonList(method), Collections.singletonList(child));
    }

    private String buildTestPlanDsl(List<String> methods, List<String> children)
        throws IOException {
      Map<Boolean, List<String>> imports = Arrays.stream(
              testResourceContents("/default-imports.txt")
                  .split("\n"))
          .filter(s -> !s.isEmpty())
          .collect(Collectors.partitioningBy(s -> s.startsWith("static ")));
      return new StringTemplate(testResourceContents("/TestClass.template.java"))
          .bind("dependencies", buildDependencies())
          .bind("staticImports", buildStaticImports(imports.get(true)))
          .bind("imports", buildImports(imports.get(false)))
          .bind("methodDefinitions", buildMethodDefinitions(methods))
          .bind("testPlan", buildTestPlanCode(children))
          .solve();
    }

    private String buildDependencies() throws IOException {
      return Arrays.stream(testResourceContents("/default-dependencies.txt")
              .split("\n"))
          .map(d -> "//DEPS " + d)
          .collect(Collectors.joining("\n"));
    }

    private String buildStaticImports(List<String> defaultImports) {
      List<String> additionalImports = Collections.singletonList(
          String.format("static %s.*", DslTestFragmentController.class.getName()));
      return buildImports(defaultImports, additionalImports);
    }

    private String buildImports(List<String> defaultImports) {
      List<String> additionalImports = Collections.singletonList(
          DslTestFragmentController.class.getName());
      return buildImports(defaultImports, additionalImports);
    }

    private String buildImports(List<String> defaultImports, List<String> imports) {
      List<String> ret = new ArrayList<>(defaultImports);
      ret.addAll(imports);
      return ret.stream()
          .sorted()
          .map(i -> String.format("import %s;", i))
          .collect(Collectors.joining("\n"));
    }

    private String buildMethodDefinitions(List<String> methods) {
      return methods.stream()
          .map(m -> String.format("\n%s\n", indent(m, MethodCall.INDENT)))
          .collect(Collectors.joining());
    }

    private String indent(String str, String indentation) {
      return indentation + str.replace("\n", "\n" + indentation);
    }

    private String buildTestPlanCode(List<String> children) {
      String parentIndentation = MethodCall.indentLevel(2);
      String childIndentation = MethodCall.indentLevel(4);
      return String.format("testPlan(\n%s\n%s)", children.stream()
          .map(c -> indent(c, childIndentation))
          .collect(Collectors.joining(",\n")), parentIndentation);
    }

    private String testFragmentMethodDsl(String methodName, String fragmentName, String... children)
        throws IOException {
      return new StringTemplate(testResourceContents("TestFragmentMethodDsl.template.java"))
          .bind("methodName", methodName)
          .bind("fragmentName", DEFAULT_FRAGMENT_NAME.equals(fragmentName) ? ""
              : String.format("\"%s\",", fragmentName))
          .bind("children", String.join("\n,", children))
          .solve();
    }

    private String buildHttpSamplerDsl() throws IOException {
      return testResourceContents("HttpSamplerDsl.java");
    }

    @Test
    public void shouldGenerateDslWithFragmentNameWhenConvertFragmentNonDefaultName(
        @TempDir Path tmp) throws IOException {
      String fragmentName = "My Fragment";
      String testPlanJmx = buildTestPlanJmx(
          buildFragmentJmx(fragmentName, buildHttpSamplerJmx()));
      String methodName = "myFragment";
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              testFragmentMethodDsl(methodName, fragmentName, buildHttpSamplerDsl()),
              methodName + "()"));
    }

    @Test
    public void shouldGenerateDslWithFragmentNameWhenConvertFragmentWithNameStartingWithDigit(
        @TempDir Path tmp) throws IOException {
      String fragmentName = "2Fragment";
      String testPlanJmx = buildTestPlanJmx(
          buildFragmentJmx(fragmentName, buildHttpSamplerJmx()));
      String methodName = "fragment" + fragmentName;
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              testFragmentMethodDsl(methodName, fragmentName, buildHttpSamplerDsl()),
              methodName + "()"));
    }

    @Test
    public void shouldGenerateDslWithFragmentNameWhenConvertFragmentWithNameWithSpecialChars(
        @TempDir Path tmp) throws IOException {
      String fragmentName = "My(){Fragment}";
      String testPlanJmx = buildTestPlanJmx(
          buildFragmentJmx(fragmentName, buildHttpSamplerJmx()));
      String methodName = "myFragment";
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              testFragmentMethodDsl(methodName, fragmentName, buildHttpSamplerDsl()),
              methodName + "()"));
    }

    @Test
    public void shouldGenerateDslWithFragmentsWhenConvertFragmentsWithCollidingNames(
        @TempDir Path tmp) throws IOException {
      String testPlanJmx = buildTestPlanJmx(
          buildFragmentJmx(DEFAULT_FRAGMENT_NAME, buildHttpSamplerJmx()),
          buildFragmentJmx(DEFAULT_FRAGMENT_NAME, buildHttpSamplerJmx()));
      String methodName1 = "testFragment";
      String methodName2 = "testFragment2";
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              Arrays.asList(
                  testFragmentMethodDsl(methodName1, DEFAULT_FRAGMENT_NAME, buildHttpSamplerDsl()),
                  testFragmentMethodDsl(methodName2, DEFAULT_FRAGMENT_NAME, buildHttpSamplerDsl())),
              Arrays.asList(methodName1 + "()", methodName2 + "()")
          ));
    }

  }

}
