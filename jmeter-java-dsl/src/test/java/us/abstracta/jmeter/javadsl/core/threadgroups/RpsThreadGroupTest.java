package us.abstracta.jmeter.javadsl.core.threadgroups;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.rpsThreadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class RpsThreadGroupTest extends JmeterDslTest {

  public static final int STAGE_DURATION_SECONDS = 5;
  public static final int BASE_RPS = 1;
  public static final double THRESHOLD = 0.30;

  @Test
  public void shouldGetExpectedRequestsCountWhenTestPlanWithRpsByRequest() throws Exception {
    TestPlanStats stats = testPlan(
        rpsThreadGroup()
            .rampToAndHold(BASE_RPS, Duration.ZERO, Duration.ofSeconds(STAGE_DURATION_SECONDS))
            .rampToAndHold(BASE_RPS * 2, Duration.ZERO, Duration.ofSeconds(STAGE_DURATION_SECONDS))
            .children(
                httpSampler(wiremockUri),
                httpSampler(wiremockUri)
            )
    ).run();
    int expectedRequestsCount =
        STAGE_DURATION_SECONDS * BASE_RPS + STAGE_DURATION_SECONDS * BASE_RPS * 2;
    assertThatValIsExpectedWithThreshold(stats.overall().samplesCount(), expectedRequestsCount,
        THRESHOLD);
  }

  private void assertThatValIsExpectedWithThreshold(long val, int expectedVal, double threshold) {
    assertThat(val).isBetween(Math.round((1 - threshold) * expectedVal),
        Math.round((1 + threshold) * expectedVal));
  }

  @Test
  public void shouldGetExpectedRequestsCountWhenTestPlanWithRpsByIterations() throws Exception {
    TestPlanStats stats = testPlan(
        rpsThreadGroup()
            .counting(RpsThreadGroup.EventType.ITERATIONS)
            .rampToAndHold(BASE_RPS, Duration.ZERO, Duration.ofSeconds(STAGE_DURATION_SECONDS))
            .rampToAndHold(BASE_RPS * 2, Duration.ZERO, Duration.ofSeconds(STAGE_DURATION_SECONDS))
            .children(
                httpSampler(wiremockUri),
                httpSampler(wiremockUri)
            )
    ).run();
    int expectedRequestsCount =
        (STAGE_DURATION_SECONDS * BASE_RPS + STAGE_DURATION_SECONDS * BASE_RPS * 2) * 2;
    assertThatValIsExpectedWithThreshold(stats.overall().samplesCount(), expectedRequestsCount,
        THRESHOLD);
  }

}
