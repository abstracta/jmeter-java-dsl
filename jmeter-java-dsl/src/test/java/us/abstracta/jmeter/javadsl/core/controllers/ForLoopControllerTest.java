package us.abstracta.jmeter.javadsl.core.controllers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.forLoopController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.regexExtractor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class ForLoopControllerTest extends JmeterDslTest {

  private static final int LOOP_ITERATIONS = 3;

  @Test
  public void shouldExecuteExpectedNumberOfTimesWhenLoopControllerInPlan() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, TEST_ITERATIONS,
            forLoopController(LOOP_ITERATIONS,
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(TEST_ITERATIONS * LOOP_ITERATIONS);
  }

  @Test
  public void shouldExposeJMeterVariableWithControllerNameWhenLoopControllerInPlan()
      throws Exception {
    String loopName = "COUNT";
    testPlan(
        threadGroup(1, 1,
            forLoopController(loopName, LOOP_ITERATIONS,
                httpSampler(wiremockUri + "/${__jm__" + loopName + "__idx}")
            )
        )
    ).run();
    verifyRequestMadeForPath("/0");
    verifyRequestMadeForPath("/1");
    verifyRequestMadeForPath("/2");
  }

  private void verifyRequestMadeForPath(String path) {
    verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo(path)));
  }

  @Test
  public void shouldExecuteExpectedNumberOfTimesWhenLoopControllerWithExpressionInPlan() throws Exception {
    String varName = "LOOP_COUNT";
    String loopsPrefix = "counts=";
    stubFor(get(anyUrl()).willReturn(aResponse().withBody(loopsPrefix + LOOP_ITERATIONS)));
    TestPlanStats stats = testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri)
                .children(
                    regexExtractor(varName, loopsPrefix + "(\\d+)")
                ),
            forLoopController("${" + varName + "}",
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(TEST_ITERATIONS * (1 +LOOP_ITERATIONS));
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithForLoopController() {
      return testPlan(
          threadGroup(1, 1,
              forLoopController("for", 5,
                  httpSampler("http://localhost")
              )
          )
      );
    }

  }

}
