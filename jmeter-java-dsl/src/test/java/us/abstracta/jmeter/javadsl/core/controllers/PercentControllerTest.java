package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.percentController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PercentControllerTest extends JmeterDslTest {

  @Test
  public void shouldOnlyExecuteGivenPercentOfTheTimesWhenInPlan() throws Exception {
    int threads = 2;
    int iterations = 10;
    float percent = 25;
    TestPlanStats stats = testPlan(
        threadGroup(threads, iterations,
            percentController(percent,
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(
        (long) (threads * iterations * percent / 100));
  }

}
