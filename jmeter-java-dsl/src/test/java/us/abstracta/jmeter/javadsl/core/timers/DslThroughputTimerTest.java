package us.abstracta.jmeter.javadsl.core.timers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import us.abstracta.jmeter.javadsl.*;
import us.abstracta.jmeter.javadsl.codegeneration.*;
import us.abstracta.jmeter.javadsl.core.*;

import java.time.*;
import us.abstracta.jmeter.javadsl.core.samplers.DslDummySampler;
import us.abstracta.jmeter.javadsl.core.timers.DslThroughputTimer.ThroughputMode;

import static org.assertj.core.api.Assertions.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class DslThroughputTimerTest extends JmeterDslTest {

  private static final int THREADS = 2;
  private static final int REQUESTS_PER_SECOND = 2;
  private static final int THROUGHPUT = REQUESTS_PER_SECOND * 60;
  private static final Duration TEST_DURATION = Duration.ofSeconds(5);

  @Test
  public void shouldGetExpectedSampleCountWhenThroughputTimerAtTestPlanLevel()
      throws Exception {
    TestPlanStats stats = testPlan(
        throughputTimer(THROUGHPUT),
        threadGroup(THREADS, TEST_DURATION,
            sampler()
        ),
        threadGroup(THREADS, TEST_DURATION,
            sampler()
        )
    ).run();
    assertThatSamplesCountIsAround(REQUESTS_PER_SECOND * TEST_DURATION.getSeconds(), THREADS * 2,
        stats);
  }

  private void assertThatSamplesCountIsAround(long expected, int threads, TestPlanStats stats) {
    // we always add a threshold depending on number of threads since last execution will always
    // run past the specified time
    assertThat(stats.overall().samplesCount())
        .isBetween(expected, expected + threads);
  }

  @NotNull
  private static DslDummySampler sampler() {
    return dummySampler("OK");
  }

  @Test
  public void shouldGetExpectedSampleCountWhenThroughputTimerAtThreadGroupLevel()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(THREADS, TEST_DURATION,
            throughputTimer(THROUGHPUT),
            sampler()
        ),
        threadGroup(THREADS, TEST_DURATION,
            throughputTimer(THROUGHPUT),
            sampler()
        )
    ).run();
    assertThatSamplesCountIsAround(2 * REQUESTS_PER_SECOND * TEST_DURATION.getSeconds(),
        2 * THREADS, stats);
  }

  @Test
  public void shouldGetExpectedSampleCountWhenThroughputTimerPerThread()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(THREADS, TEST_DURATION,
            throughputTimer(THROUGHPUT)
                .perThread(),
            sampler()
        )
    ).run();
    assertThatSamplesCountIsAround(THREADS * REQUESTS_PER_SECOND * TEST_DURATION.getSeconds(),
        THREADS, stats);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithThroughputTimerAtTestPlanLevel() {
      return testPlan(
          threadGroup(1, 1,
              throughputTimer(1.0),
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan testPlanWithThroughputTimerAtThreadGroupLevel() {
      return testPlan(
          threadGroup(1, 1,
              throughputTimer(1.0),
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan testPlanWithThroughputTimerAtTestPlanLevelWithThreadGroupControl() {
      return testPlan(
          throughputTimer(1.0)
              .calculation(ThroughputMode.THREAD_GROUP_EVEN),
          threadGroup(1, 1,
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan testPlanWithThroughputTimerWithThreadControl() {
      return testPlan(
          throughputTimer(1.0)
              .perThread(),
          threadGroup(1, 1,
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan testPlanWithThroughputTimerWithAllThreadsAccurateControl() {
      return testPlan(
          throughputTimer(1.0)
              .calculation(ThroughputMode.ALL_THREADS_ACCURATE),
          threadGroup(1, 1,
              httpSampler("http://localhost")
          )
      );
    }

  }

}
