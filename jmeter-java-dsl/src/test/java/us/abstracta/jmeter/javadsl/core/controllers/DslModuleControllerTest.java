package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.buildHttpSamplerDsl;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.buildHttpSamplerJmx;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.buildTestPlanDsl;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.buildTestPlanJmx;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.jmx2dsl;
import static us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest.testResourceContents;
import static us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentControllerTest.buildFragmentJmx;
import static us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentControllerTest.buildFragmentMethod;
import static us.abstracta.jmeter.javadsl.core.controllers.DslTestFragmentControllerTest.DEFAULT_FRAGMENT_NAME;

import java.nio.file.Path;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.codegeneration.Indentation;
import us.abstracta.jmeter.javadsl.core.util.StringTemplate;

public class DslModuleControllerTest {

  private static final String FRAGMENT_METHOD_NAME = "testFragment";
  private static final String FRAGMENT_METHOD_CALL = FRAGMENT_METHOD_NAME + "()";

  @Nested
  public class CodeBuilderTest {

    @Test
    public void shouldReuseFragmentMethodWhenModuleUsesPreviousEnabledFragment(@TempDir Path tmp)
        throws Exception {
      String jmx = buildTestPlanJmx(
          buildThreadGroupJmx(
              buildFragmentJmx(DEFAULT_FRAGMENT_NAME, buildHttpSamplerJmx()),
              buildModuleJmx()
          ));
      assertThat(jmx2dsl(jmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              buildFragmentMethod(FRAGMENT_METHOD_NAME, DEFAULT_FRAGMENT_NAME,
                  buildHttpSamplerDsl()),
              buildThreadGroupDsl(FRAGMENT_METHOD_CALL, FRAGMENT_METHOD_CALL)
          ));
    }

    private String buildThreadGroupJmx(String... childrenJmx) {
      return new StringTemplate(testResourceContents("thread-group.template.jmx"))
          .bind("children", String.join("\n", childrenJmx))
          .solve();
    }

    private String buildModuleJmx() {
      return testResourceContents("module.jmx");
    }

    private String buildThreadGroupDsl(String... children) {
      return String.format("threadGroup(1, 1,\n%s\n)",
          Indentation.indent(String.join(",\n", children), Indentation.INDENT));
    }

    @Test
    public void shouldDefineAndUseFragmentMethodWhenModuleUsesLaterEnabledFragment(
        @TempDir Path tmp) throws Exception {
      String jmx = buildTestPlanJmx(
          buildThreadGroupJmx(
              buildModuleJmx(),
              buildFragmentJmx(DEFAULT_FRAGMENT_NAME, buildHttpSamplerJmx())
          ));
      assertThat(jmx2dsl(jmx, tmp))
          .isEqualTo(buildTestPlanDsl(
              buildFragmentMethod(FRAGMENT_METHOD_NAME, DEFAULT_FRAGMENT_NAME,
                  buildHttpSamplerDsl()),
              buildThreadGroupDsl(FRAGMENT_METHOD_CALL, FRAGMENT_METHOD_CALL)
          ));
    }

  }

}
