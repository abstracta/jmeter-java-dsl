package us.abstracta.jmeter.javadsl.core.postprocessors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class DslDebugPostProcessorTest extends JmeterDslTest {

  @BeforeEach
  public void setUp() {
    stubFor(get(anyUrl())
        .willReturn(
            aResponse().withBody("[{\"id\":1,\"name\":\"test\"}, {\"id\":2,\"name\":\"test2\"}]")));
  }

  @Test
  public void shouldIncludeJMeterVariablesWhenTestPlanWithDebugPostProcessorAndDefaultConfig()
      throws Exception {
    StringBuilder subSampleResponseBody = new StringBuilder();
    runPlanWithDebugger(debugPostProcessor(), subSampleResponseBody);
    assertThat(subSampleResponseBody.toString()).matches(
        "(?s)JMeterVariables:\n.*USER_IDS_1=1\nUSER_IDS_2=2.*");
  }

  private void runPlanWithDebugger(DslDebugPostProcessor debugPostProcessor,
      StringBuilder subSampleResponseBody) throws IOException {
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .children(
                    jsonExtractor("USER_IDS", "[].id")
                        .matchNumber(-1),
                    debugPostProcessor,
                    jsr223PostProcessor(s -> subSampleResponseBody.append(
                        s.prev.getSubResults()[0].getResponseDataAsString()))
                )
        )
    ).run();
  }

  @Test
  public void shouldIncludeConfiguredDataWhenTestPlanWithDebugPostProcessorAndCustomConfig()
      throws Exception {
    StringBuilder subSampleResponseBodyHolder = new StringBuilder();
    runPlanWithDebugger(debugPostProcessor()
        .jmeterVariables(false)
        .jmeterProperties(true)
        .samplerProperties(true)
        .systemProperties(true), subSampleResponseBodyHolder);
    String subSampleResponseBody = subSampleResponseBodyHolder.toString();
    assertThat(subSampleResponseBody).matches(
        "(?s)SamplerProperties:\n.*JMeterProperties:\n.*SystemProperties:\n.*");
    assertThat(subSampleResponseBody).doesNotContain("JMeterVariables:");
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithDebugPostProcessor() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              debugPostProcessor()
          )
      );
    }

    public DslTestPlan testPlanWithDebugPostProcessorAndNoneDefaultSettings() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              debugPostProcessor()
                  .jmeterVariables(false)
                  .samplerProperties()
                  .jmeterProperties()
                  .systemProperties()
          )
      );
    }

  }

}
