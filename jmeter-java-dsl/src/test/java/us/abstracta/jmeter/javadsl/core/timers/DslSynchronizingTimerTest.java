package us.abstracta.jmeter.javadsl.core.timers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class DslSynchronizingTimerTest {

  @Test
  public void shouldSynchronizeRequestsWhenTestPlanWithSynchronizingTimer() throws Exception {
    List<Instant> reqTimes = new ArrayList<>();
    testPlan(
        threadGroup(2, 1,
            jsr223Sampler(s -> {
              if (Thread.currentThread().getName().endsWith("2")) {
                Thread.sleep(3000);
              }
            }),
            jsr223Sampler(s -> {
              synchronized (reqTimes) {
                reqTimes.add(Instant.now());
              }
            }).children(
                synchronizingTimer()
            )
        )
    ).run();
    assertThat(Duration.between(reqTimes.get(0), reqTimes.get(1))).isLessThan(Duration.ofMillis(500));
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithSynchronizingTimer() {
      return testPlan(
          threadGroup(2, 3,
              synchronizingTimer(),
              httpSampler("http://localhost")
          )
      );
    }

  }

}
