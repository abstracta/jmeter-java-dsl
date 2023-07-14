package us.abstracta.jmeter.javadsl.blazemeter;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.csvDataSet;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class BlazeMeterEngineTest {

  private static final String SAMPLE_LABEL = "httpSample";
  private static final int TEST_ITERATIONS = 3;
  private static final String OVERALL_STATS_LABEL = "overall";

  @Test
  public void shouldRunTestInBlazeMeter() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            csvDataSet(new TestResource("users.csv")),
            httpSampler(SAMPLE_LABEL, "https://localhost/users/${USER}")
        )
    ).runIn(new BlazeMeterEngine(System.getenv("BZ_TOKEN"))
        .testName("DSL test")
        .totalUsers(1)
        .iterations(TEST_ITERATIONS)
        .threadsPerEngine(1)
        .testTimeout(Duration.ofMinutes(10))
        .useDebugRun());
    assertThat(extractTotalCounts(stats)).isEqualTo(buildBlazemeterExpectedCounts());
  }

  private Map<String, Long> extractTotalCounts(TestPlanStats stats) {
    Map<String, Long> actualStats = new HashMap<>();
    actualStats.put(OVERALL_STATS_LABEL, stats.overall().samplesCount());
    for (String label : stats.labels()) {
      actualStats.put(label, stats.byLabel(label).samplesCount());
    }
    return actualStats;
  }

  private Object buildBlazemeterExpectedCounts() {
    Map<String, Long> expectedStats = new HashMap<>();
    expectedStats.put(OVERALL_STATS_LABEL, (long) TEST_ITERATIONS);
    expectedStats.put(SAMPLE_LABEL, (long) TEST_ITERATIONS);
    return expectedStats;
  }

}
