package us.abstracta.jmeter.javadsl.core.timers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.constantTimer;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslConstantTimerTest extends JmeterDslTest {

  @Test
  public void shouldLastAtLeastConfiguredTimeWhenUsingConstantTimer() throws Exception {
    Duration timerDuration = Duration.ofSeconds(5);
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            constantTimer(timerDuration),
            httpSampler(wiremockUri)
        )
    ).run();
    assertThat(stats.duration()).isGreaterThan(timerDuration);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithConstantTimer() {
      return testPlan(
          threadGroup(1, 1,
              constantTimer(Duration.ofSeconds(1)),
              httpSampler("http://localhost")
          )
      );
    }

  }

}
