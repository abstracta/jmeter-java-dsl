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

  @Test
  public void shouldLastAtLeastMinimumTimeWhenUsingRandomUniformTimer() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            uniformRandomTimer(10000,12000),
            httpSampler(wiremockUri)
        )
    ).run();
    assertThat(stats.overall().elapsedTime().toMillis()).isGreaterThan(10000);
  }

  @Test
  public void shouldAffectOnlyFirstControllerWhenUsingRandomUniformTimer() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1,1,
            transaction("Step1",
                uniformRandomTimer(10000,12000),
                httpSampler(wiremockUri)
            ),
            transaction("Step2",
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.byLabel("Step1").elapsedTime().toMillis()).isGreaterThan(10000);
    assertThat(stats.byLabel("Step2").elapsedTime().toMillis()).isLessThan(3000);
  }


  @Test
  public void shouldAffectBothControllerWhenUsingRandomUniformTimer() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1,1,
            uniformRandomTimer(10000,12000),
            transaction("Step1",
                httpSampler(wiremockUri)
            ),
            transaction("Step2",
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.byLabel("Step1").elapsedTime().toMillis()).isGreaterThan(10000);
    assertThat(stats.byLabel("Step2").elapsedTime().toMillis()).isGreaterThan(10000);
  }
}
