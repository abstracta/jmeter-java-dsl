package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.forLoopController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
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
    TestPlanStats stats = testPlan(
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
    wiremockServer.verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo(path)));
  }

}
