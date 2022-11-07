package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.codegeneration.DslCodeGenerator;
import us.abstracta.jmeter.javadsl.codegeneration.Indentation;
import us.abstracta.jmeter.javadsl.codegeneration.TestClassTemplate;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;

public class DslTestFragmentControllerTest {

  @Nested
  public class CodeBuilderTest {

    private static final String DEFAULT_FRAGMENT_NAME = "Test Fragment";

    @Test
    public void shouldGenerateDslWithFragmentMethodWhenConvertTestPlanWithFragment(
        @TempDir Path tmp) throws IOException {
      String testPlanJmx = buildTestPlanJmx(
          buildFragmentJmx(DEFAULT_FRAGMENT_NAME, buildHttpSamplerJmx()));
      String methodName = "testFragment";
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              testFragmentMethodDsl(methodName, DEFAULT_FRAGMENT_NAME, buildHttpSamplerDsl()),
              methodName + "()"));
    }

    private String buildTestPlanJmx(String... childrenJmx) {
      return new StringTemplate(testResourceContents("base-test-plan.template.jmx"))
          .bind("children", String.join("\n", childrenJmx))
          .solve();
    }

    private String testResourceContents(String resourceName) {
      try {
        return testResource(resourceName.startsWith("/") ? resourceName.substring(1)
            : "codegeneration/fragments/" + resourceName).contents();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    private String buildFragmentJmx(String name, String... childrenJmx) {
      return new StringTemplate(testResourceContents("fragment.template.jmx"))
          .bind("name", name)
          .bind("children", String.join("\n", childrenJmx))
          .solve();
    }

    private String buildHttpSamplerJmx() {
      return testResourceContents("http-sampler.jmx");
    }

    private String jmx2dsl(String testPlanJmx, Path tmp) throws IOException {
      Path testPlanPath = tmp.resolve("testplan.jmx");
      Files.write(testPlanPath, testPlanJmx.getBytes(StandardCharsets.UTF_8));
      return new DslCodeGenerator().generateCodeFromJmx(testPlanPath.toFile());
    }

    private String buildTestPlanDsl(String method, String child) {
      return buildTestPlanDsl(Collections.singletonList(method), Collections.singletonList(child));
    }

    private String buildTestPlanDsl(List<String> methods, List<String> children) {
      return new TestClassTemplate()
          .dependencies(Collections.singleton("us.abstracta.jmeter:jmeter-java-dsl"))
          .staticImports(Collections.singleton(DslTestFragmentController.class.getName()))
          .imports(Collections.singleton(DslTestFragmentController.class.getName()))
          .methodDefinitions(methods)
          .testPlan(buildTestPlanCode(children))
          .solve();
    }

    private String buildTestPlanCode(List<String> children) {
      String parentIndentation = Indentation.indentLevel(2);
      String childIndentation = Indentation.indentLevel(4);
      return String.format("testPlan(\n%s\n%s)", children.stream()
          .map(c -> Indentation.indent(c, childIndentation))
          .collect(Collectors.joining(",\n")), parentIndentation);
    }

    private String testFragmentMethodDsl(String methodName, String fragmentName,
        String... children) {
      return new StringTemplate(testResourceContents("TestFragmentMethodDsl.template.java"))
          .bind("methodName", methodName)
          .bind("fragmentName", DEFAULT_FRAGMENT_NAME.equals(fragmentName) ? ""
              : String.format("\"%s\",", fragmentName))
          .bind("children", String.join("\n,", children))
          .solve();
    }

    private String buildHttpSamplerDsl() {
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
