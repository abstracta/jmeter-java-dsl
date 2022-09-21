package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.weightedSwitchController;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslWeightedSwitchControllerTest extends JmeterDslTest {

  @Test
  public void shouldOnlyExecuteGivenPercentOfTheTimesWhenInPlan() throws Exception {
    int threads = 1;
    int iterations = 20;
    long weight1 = 60;
    long weight2 = 30;
    String label3 = "sample3";

    TestPlanStats stats = testPlan(
        threadGroup(threads, iterations,
            weightedSwitchController()
                .child(weight1, httpSampler(SAMPLE_1_LABEL, wiremockUri))
                .children(httpSampler(SAMPLE_2_LABEL, wiremockUri))
                .child(weight2, httpSampler(label3, wiremockUri))
        )
    ).run();

    assertThat(buildSampleCountsMap(stats)).isEqualTo(
        buildExpectedSamplesCounts(threads * iterations,
            new WeightedLabel(SAMPLE_1_LABEL, weight1),
            new WeightedLabel(SAMPLE_2_LABEL, DslWeightedSwitchController.DEFAULT_WEIGHT),
            new WeightedLabel(label3, weight2)));
  }

  private Map<String, Long> buildSampleCountsMap(TestPlanStats stats) {
    return stats.labels().stream()
        .collect(Collectors.toMap(s -> s, s -> stats.byLabel(s).samplesCount()));
  }

  private Map<String, Long> buildExpectedSamplesCounts(int totalIterations,
      WeightedLabel... weightedLabels) {
    long totalWeight = Arrays.stream(weightedLabels)
        .mapToLong(e -> e.weight)
        .sum();
    return Arrays.stream(weightedLabels)
        .collect(Collectors.toMap(e -> e.label,
            e -> Math.round((double) totalIterations * e.weight / totalWeight)));
  }

  private static class WeightedLabel {

    private final String label;
    private final long weight;

    private WeightedLabel(String label, long weight) {
      this.label = label;
      this.weight = weight;
    }

  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithWeightedSwitchController() {
      return testPlan(
          threadGroup(1, 1,
              weightedSwitchController()
                  .child(2, httpSampler("sample1", "http://localhost"))
                  .child(3, httpSampler("sample2", "http://localhost"))
          )
      );
    }

    public DslTestPlan testPlanWithWeightedSwitchControllerAndUnweightedElements() {
      return testPlan(
          threadGroup(1, 1,
              weightedSwitchController()
                  .children(httpSampler("sample0", "http://localhost"))
                  .child(2, httpSampler("sample1", "http://localhost"))
                  .children(httpSampler("sample2", "http://localhost"))
                  .child(3, httpSampler("sample3", "http://localhost"))
                  .children(httpSampler("sample4", "http://localhost"))
          )
      );
    }

  }

}
