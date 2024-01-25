package us.abstracta.jmeter.javadsl.core.samplers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadPause;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslFlowControlActionTest extends JmeterDslTest {

  @Test
  public void shouldLastAtLeastConfiguredTimeWhenUsingConstantTimer() throws Exception {
    Duration timerDuration = Duration.ofSeconds(5);
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            threadPause(timerDuration),
            httpSampler(wiremockUri)
        )
    ).run();
    assertThat(stats.duration()).isGreaterThan(timerDuration);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithThreadPauseAndDurationParam() {
      return testPlan(
          threadGroup(1, 1,
              threadPause(Duration.ofSeconds(1)),
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan testPlanWithThreadPauseAndJMeterExpressionParam() {
      return testPlan(
          threadGroup(1, 1,
              threadPause("${TIMER}"),
              httpSampler("http://localhost")
          )
      );
    }

  }

}
