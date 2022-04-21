package us.abstracta.jmeter.javadsl.core.controllers;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;

public class WeightedSwitchControllerTest extends JmeterDslTest {

  @Test
  public void shouldOnlyExecuteGivenPercentOfTheTimesWhenInPlan() throws Exception{
    int threads = 1;
    int iterations = 100;
    long weight1 = 60;
    long weight2 = 30;
    double totalWeight = weight1 + weight2;

    TestPlanStats stats = testPlan(
        threadGroup(threads, iterations,
            weightedSwitchController()
                .add(weight1, httpSampler(SAMPLE_1_LABEL, wiremockUri))
                .add(weight2, httpSampler(SAMPLE_2_LABEL, wiremockUri))
        )
    ).run();
    assertThat(stats.byLabel(SAMPLE_1_LABEL).samplesCount()).isEqualTo(Math.round(threads * iterations * weight1 / totalWeight));
    assertThat(stats.byLabel(SAMPLE_2_LABEL).samplesCount()).isEqualTo(Math.round(threads * iterations * weight2 / totalWeight));
  }

  @Test
  public void shouldThrowExceptionIfSumOfWeightsExceed100() {
    long weight1 = 50;
    long weight2 = 60;
    try {
      weightedSwitchController()
          .add(weight1, httpSampler(SAMPLE_1_LABEL, wiremockUri))
          .add(weight2, httpSampler(SAMPLE_2_LABEL, wiremockUri));
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalArgumentException.class);
      assertThat(e.getMessage()).isEqualTo("Total sum of weights should be less or equal 100");
    }
  }
}
