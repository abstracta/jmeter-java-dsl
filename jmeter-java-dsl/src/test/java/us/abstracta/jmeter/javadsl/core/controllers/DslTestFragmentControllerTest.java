package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.buildHttpSamplerDsl;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.buildHttpSamplerJmx;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.buildTestPlanDsl;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.buildTestPlanJmx;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.jmx2dsl;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.testResourceContents;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;

public class DslTestFragmentControllerTest {

  public static final String DEFAULT_FRAGMENT_NAME = "Test Fragment";

  public static String buildFragmentJmx(String name, String... childrenJmx) {
    return new StringTemplate(testResourceContents("fragment.template.jmx"))
        .bind("name", name)
        .bind("children", String.join("\n", childrenJmx))
        .solve();
  }

  public static String buildFragmentMethod(String methodName, String fragmentName,
      String... children) {
    return new StringTemplate(testResourceContents("TestFragmentMethodDsl.template.java"))
        .bind("methodName", methodName)
        .bind("fragmentName", DEFAULT_FRAGMENT_NAME.equals(fragmentName) ? ""
            : String.format("\"%s\",", fragmentName))
        .bind("children", String.join("\n,", children))
        .solve();
  }

  @Nested
  public class CodeBuilderTest {

    @Test
    public void shouldGenerateDslWithFragmentMethodWhenConvertTestPlanWithFragment(
        @TempDir Path tmp) throws IOException {
      String testPlanJmx = buildTestPlanJmx(
          buildFragmentJmx(DEFAULT_FRAGMENT_NAME, buildHttpSamplerJmx()));
      String methodName = "testFragment";
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              buildFragmentMethod(methodName, DEFAULT_FRAGMENT_NAME, buildHttpSamplerDsl()),
              methodName + "()"));
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
              buildFragmentMethod(methodName, fragmentName, buildHttpSamplerDsl()),
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
              buildFragmentMethod(methodName, fragmentName, buildHttpSamplerDsl()),
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
              buildFragmentMethod(methodName, fragmentName, buildHttpSamplerDsl()),
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
                  buildFragmentMethod(methodName1, DEFAULT_FRAGMENT_NAME, buildHttpSamplerDsl()),
                  buildFragmentMethod(methodName2, DEFAULT_FRAGMENT_NAME, buildHttpSamplerDsl())),
              Arrays.asList(methodName1 + "()", methodName2 + "()")
          ));
    }

  }

}
