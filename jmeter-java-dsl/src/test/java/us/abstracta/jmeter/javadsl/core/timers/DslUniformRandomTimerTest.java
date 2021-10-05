package us.abstracta.jmeter.javadsl.core.timers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.transaction;
import static us.abstracta.jmeter.javadsl.JmeterDsl.uniformRandomTimer;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslUniformRandomTimerTest extends JmeterDslTest {

  public static final int MINIMUM_MILLIS = 10000;
  public static final int MAXIMUM_MILLIS = 12000;

  @Test
  public void shouldLastAtLeastMinimumTimeWhenUsingRandomUniformTimer() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            uniformRandomTimer(MINIMUM_MILLIS, MAXIMUM_MILLIS),
            httpSampler(wiremockUri)
        )
    ).run();
    assertThat(stats.overall().elapsedTime().toMillis()).isGreaterThan(MINIMUM_MILLIS);
  }

  @Test
  public void shouldAffectOnlyFirstControllerWhenUsingRandomUniformTimer() throws Exception {
    String transaction1Label = "Step1";
    String transaction2Label = "Step2";
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            transaction(transaction1Label,
                uniformRandomTimer(MINIMUM_MILLIS, MAXIMUM_MILLIS),
                httpSampler(wiremockUri)
            ),
            transaction(transaction2Label,
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.byLabel(transaction1Label).elapsedTime().toMillis()).isGreaterThan(
        MINIMUM_MILLIS);
    assertThat(stats.byLabel(transaction2Label).elapsedTime().toMillis()).isLessThan(3000);
  }


  @Test
  public void shouldAffectBothControllerWhenUsingRandomUniformTimer() throws Exception {
    String transaction1Label = "Step1";
    String transaction2Label = "Step2";
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            uniformRandomTimer(MINIMUM_MILLIS, MAXIMUM_MILLIS),
            transaction(transaction1Label,
                httpSampler(wiremockUri)
            ),
            transaction(transaction2Label,
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.byLabel(transaction1Label).elapsedTime().toMillis()).isGreaterThan(
        MINIMUM_MILLIS);
    assertThat(stats.byLabel(transaction2Label).elapsedTime().toMillis()).isGreaterThan(
        MINIMUM_MILLIS);
  }
}
