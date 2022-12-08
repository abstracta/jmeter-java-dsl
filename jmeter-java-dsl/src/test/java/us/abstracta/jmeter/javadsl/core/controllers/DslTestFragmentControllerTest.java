package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;

public class DslTestFragmentControllerTest extends MethodCallFragmentBuilderTest {

  public static final String DEFAULT_FRAGMENT_NAME = "Test Fragment";
  private static final String DEFAULT_FRAGMENT_METHOD_NAME = "testFragment";
  private static final String DEFAULT_FRAGMENT_METHOD_CALL = DEFAULT_FRAGMENT_METHOD_NAME + "()";

  public static String buildFragmentJmx() {
    return buildFragmentJmx(DEFAULT_FRAGMENT_NAME);
  }

  private static String buildFragmentJmx(String name) {
    return buildFragmentJmx(name, true);
  }

  private static String buildFragmentJmx(String name, boolean enabled) {
    return new StringTemplate(testResourceContents("fragments/fragment.template.jmx"))
        .bind("name", name)
        .bind("enabled", enabled)
        .solve();
  }

  public static String buildFragmentDisabledJmx() {
    return buildFragmentJmx(DEFAULT_FRAGMENT_NAME, false);
  }

  public static String buildFragmentMethod() {
    return buildFragmentMethod(DEFAULT_FRAGMENT_METHOD_NAME, DEFAULT_FRAGMENT_NAME);
  }

  public static String buildFragmentMethod(String methodName, String fragmentName) {
    return new StringTemplate(testResourceContents("fragments/TestFragmentMethodDsl.template.java"))
        .bind("methodName", methodName)
        .bind("fragmentName", DEFAULT_FRAGMENT_NAME.equals(fragmentName) ? ""
            : String.format("\"%s\",", fragmentName))
        .solve();
  }

  @Nested
  public class CodeBuilderTest {

    @Test
    public void shouldGenerateDslWithFragmentMethodWhenConvertTestPlanWithFragment(
        @TempDir Path tmp) throws IOException {
      String testPlanJmx = buildTestPlanJmx(buildFragmentJmx());
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(buildFragmentMethod(), DEFAULT_FRAGMENT_METHOD_CALL));
    }

    private String buildTestPlanDsl(String method, String child) {
      return buildTestPlanDsl(Collections.singletonList(method), Collections.singletonList(child));
    }

    public String buildTestPlanDsl(List<String> methods, List<String> children) {
      return buildTestPlanDslTemplate(children)
          .staticImports(Collections.singleton(DslTestFragmentController.class.getName()))
          .imports(Collections.singleton(DslTestFragmentController.class.getName()))
          .methodDefinitions(methods)
          .solve();
    }

    @Test
    public void shouldGenerateDslWithFragmentNameWhenConvertFragmentNonDefaultName(
        @TempDir Path tmp) throws IOException {
      String fragmentName = "My Fragment";
      String testPlanJmx = buildTestPlanJmx(buildFragmentJmx(fragmentName));
      String methodName = "myFragment";
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              buildFragmentMethod(methodName, fragmentName),
              methodName + "()"));
    }

    @Test
    public void shouldGenerateDslWithFragmentNameWhenConvertFragmentWithNameStartingWithDigit(
        @TempDir Path tmp) throws IOException {
      String fragmentName = "2Fragment";
      String testPlanJmx = buildTestPlanJmx(buildFragmentJmx(fragmentName));
      String methodName = "fragment" + fragmentName;
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              buildFragmentMethod(methodName, fragmentName),
              methodName + "()"));
    }

    @Test
    public void shouldGenerateDslWithFragmentNameWhenConvertFragmentWithNameWithSpecialChars(
        @TempDir Path tmp) throws IOException {
      String fragmentName = "My(){Fragment}";
      String testPlanJmx = buildTestPlanJmx(buildFragmentJmx(fragmentName));
      String methodName = "myFragment";
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              buildFragmentMethod(methodName, fragmentName),
              methodName + "()"));
    }

    @Test
    public void shouldGenerateDslWithFragmentsWhenConvertFragmentsWithCollidingNames(
        @TempDir Path tmp) throws IOException {
      String testPlanJmx = buildTestPlanJmx(
          buildFragmentJmx(),
          buildFragmentJmx());
      String methodName2 = "testFragment2";
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              Arrays.asList(
                  buildFragmentMethod(),
                  buildFragmentMethod(methodName2, DEFAULT_FRAGMENT_NAME)),
              Arrays.asList(DEFAULT_FRAGMENT_METHOD_CALL, methodName2 + "()")
          ));
    }

    @Test
    public void shouldGenerateDslWithCommentedFragmentCallWhenConvertDisabledFragment(
        @TempDir Path tmp) throws IOException {
      String testPlanJmx = buildTestPlanJmx(buildFragmentDisabledJmx());
      String cacheMethodCall = "httpCache()";
      assertThat(jmx2dsl(testPlanJmx, tmp))
          .isEqualTo(buildTestPlanDsl(Collections.singletonList(buildFragmentMethod()),
              Arrays.asList("httpCookies()", cacheMethodCall, "//" + DEFAULT_FRAGMENT_METHOD_CALL))
              .replace(cacheMethodCall + ",", cacheMethodCall + "//,"));
    }

  }

}
