package us.abstracta.jmeter.javadsl.core.timers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.uniformRandomTimer;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslUniformRandomTimerTest extends JmeterDslTest {

  @Test
  public void shouldLastAtLeastMinimumTimeWhenUsingRandomUniformTimer() throws Exception {
    Duration minimum = Duration.ofSeconds(5);
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            uniformRandomTimer(minimum, Duration.ofSeconds(7)),
            httpSampler(wiremockUri)
        )
    ).run();
    assertThat(stats.duration()).isGreaterThan(minimum);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithUniformRandomTimer() {
      return testPlan(
          threadGroup(1, 1,
              uniformRandomTimer(Duration.ofSeconds(1), Duration.ofSeconds(5)),
              httpSampler("http://localhost")
          )
      );
    }

  }

}
