package us.abstracta.jmeter.javadsl.core.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.dummySampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.runtimeController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.whileController;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslRuntimeControllerTest extends JmeterDslTest {

  @Test
  public void shouldExecuteChildElementsUntilPeriodExpiresWhenRuntimeControllerInPlan()
      throws Exception {
    int iterations = 2;
    int runtimeDurationSeconds = 3;
    int samplerDurationSeconds = 1;
    TestPlanStats stats = testPlan(
        threadGroup(1, iterations,
            runtimeController(Duration.ofSeconds(runtimeDurationSeconds),
                whileController("true",
                    dummySampler("OK")
                        .responseTime(Duration.ofSeconds(samplerDurationSeconds))
                        .simulateResponseTime(true)
                )
            )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(
        iterations * runtimeDurationSeconds / samplerDurationSeconds);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithRuntimeController() {
      return testPlan(
          threadGroup(1, 1,
              runtimeController(Duration.ofSeconds(5),
                  httpSampler("http://localhost")
              )
          )
      );
    }

  }

}
