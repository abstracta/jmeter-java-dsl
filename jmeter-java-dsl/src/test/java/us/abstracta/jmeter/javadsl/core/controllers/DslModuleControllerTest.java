package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentControllerTest.DEFAULT_FRAGMENT_NAME;
import static us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentControllerTest.buildFragmentDisabledJmx;
import static us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentControllerTest.buildFragmentJmx;
import static us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentControllerTest.buildFragmentMethod;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;

public class DslModuleControllerTest {

  private static final String FRAGMENT_METHOD_NAME = "testFragment";
  private static final String FRAGMENT_METHOD_CALL = FRAGMENT_METHOD_NAME + "()";
  private static final String IF_CONTROLLER_METHOD_CALL = "ifController()";
  private static final String IF_CONTROLLER_DEFAULT_NAME = "If Controller";

  @Nested
  public class CodeBuilderTest extends MethodCallFragmentBuilderTest {

    @Test
    public void shouldReuseFragmentMethodWhenModuleUsesPreviousEnabledFragment(@TempDir Path tmp)
        throws Exception {
      String jmx = buildModuleTestPlanJmx(
          buildFragmentJmx(),
          buildModuleJmx(DEFAULT_FRAGMENT_NAME));
      assertThat(jmx2dsl(jmx, tmp))
          .isEqualTo(buildFragmentPlanDsl(
              buildThreadGroupDsl(FRAGMENT_METHOD_CALL, FRAGMENT_METHOD_CALL)));
    }

    private String buildModuleTestPlanJmx(String... children) {
      return buildTestPlanJmx(buildThreadGroupJmx(children));
    }

    public String buildFragmentPlanDsl(String... children) {
      return buildTestPlanDslTemplate(Arrays.asList(children))
          .staticImports(Collections.singleton(DslTestFragmentController.class.getName()))
          .imports(Collections.singleton(DslTestFragmentController.class.getName()))
          .methodDefinitions(Collections.singletonList(buildFragmentMethod()))
          .solve();
    }

    private String buildModuleJmx(String controllerName) {
      return new StringTemplate(testResourceContents("fragments/module.jmx"))
          .bind("controllerName", controllerName)
          .solve();
    }

    @Test
    public void shouldDefineAndUseFragmentMethodWhenModuleUsesLaterEnabledFragment(
        @TempDir Path tmp) throws Exception {
      String jmx = buildModuleTestPlanJmx(
          buildModuleJmx(DEFAULT_FRAGMENT_NAME),
          buildFragmentJmx());
      assertThat(jmx2dsl(jmx, tmp))
          .isEqualTo(buildFragmentPlanDsl(
              buildThreadGroupDsl(FRAGMENT_METHOD_CALL, FRAGMENT_METHOD_CALL)));
    }

    @Test
    public void shouldUseFragmentMethodWhenModuleUsesDisabledFragment(@TempDir Path tmp)
        throws Exception {
      String jmx = buildModuleTestPlanJmx(
          buildFragmentDisabledJmx(),
          buildModuleJmx(DEFAULT_FRAGMENT_NAME)
      );
      assertThat(jmx2dsl(jmx, tmp))
          .isEqualTo(buildFragmentPlanDsl(
              "httpCookies()",
              "httpCache()",
              buildThreadGroupDsl("//" + FRAGMENT_METHOD_CALL, FRAGMENT_METHOD_CALL)));
    }

    @Test
    public void shouldDefineAndUseMethodWhenModuleUsesPreviouslyDefinedController(@TempDir Path tmp)
        throws Exception {
      String jmx = buildModuleTestPlanJmx(
          buildIfControllerJmx(),
          buildModuleJmx(IF_CONTROLLER_DEFAULT_NAME));
      assertThat(jmx2dsl(jmx, tmp))
          .isEqualTo(
              buildIfControllerPlanDsl(IF_CONTROLLER_METHOD_CALL, IF_CONTROLLER_METHOD_CALL));
    }

    private String buildIfControllerJmx() {
      return testResourceContents("fragments/if-controller.jmx");
    }

    public String buildIfControllerPlanDsl(String... children) {
      return buildTestPlanDslTemplate(Collections.singletonList(buildThreadGroupDsl(children)))
          .imports(Collections.singleton(DslIfController.class.getName()))
          .methodDefinitions(Collections.singletonList(
              testResourceContents("fragments/IfControllerDsl.java")))
          .solve();
    }

    @Test
    public void shouldDefineAndUseMethodWhenModuleUsesLaterDefinedController(@TempDir Path tmp)
        throws Exception {
      String jmx = buildModuleTestPlanJmx(
          buildModuleJmx(IF_CONTROLLER_DEFAULT_NAME),
          buildIfControllerJmx()
      );
      assertThat(jmx2dsl(jmx, tmp))
          .isEqualTo(
              buildIfControllerPlanDsl(IF_CONTROLLER_METHOD_CALL, IF_CONTROLLER_METHOD_CALL));
    }

  }

}
