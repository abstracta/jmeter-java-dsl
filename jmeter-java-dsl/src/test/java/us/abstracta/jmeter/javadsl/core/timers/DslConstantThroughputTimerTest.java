package us.abstracta.jmeter.javadsl.core.timers;

import org.junit.jupiter.api.*;
import us.abstracta.jmeter.javadsl.*;
import us.abstracta.jmeter.javadsl.codegeneration.*;
import us.abstracta.jmeter.javadsl.core.*;

import java.time.*;
import static org.assertj.core.api.Assertions.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class DslConstantThroughputTimerTest extends JmeterDslTest {

  @Test
  public void checkCorrectSamplesThroughputTimerDefaultCalcModeAllThreadsInThreadGroupShared() throws Exception {

    TestPlanStats stats = testPlan(
            threadGroup (10,Duration.ofSeconds(10),
                    dummySampler ("dummy","foo"),
                    throughputTimer(120.0))
    ).run();

    assertThat(stats.byLabel ("dummy").samplesCount()).isBetween(18L,20L);
  }

  @Test
  public void checkCorrectSamplesThroughputTimerCalcModeThisThreadOnly() throws Exception {

    TestPlanStats stats = testPlan(
            threadGroup (10,Duration.ofSeconds(10),
                dummySampler ("dummy","foo"),
                      throughputTimer(120.0)
                          .modeThisThreadOnly())
    ).run();
    assertThat(stats.byLabel ("dummy").samplesCount()).isBetween(196L,200L);
  }

  @Test
  public void checkCorrectSamplesThroughputTimerCalcModeAllActiveThreadsShared() throws Exception {

    TestPlanStats stats = testPlan(
        throughputTimer(120.0)
            .modeAllActiveThreadsShared(),
        threadGroup (10,Duration.ofSeconds(10),
            dummySampler ("dummy","foo")),
        threadGroup (10,Duration.ofSeconds(10),
            dummySampler ("dummy","foo"))

    ).run();
    assertThat(stats.byLabel ("dummy").samplesCount()).isBetween(18L,20L);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithThroughputTimer() {
      return testPlan(
          threadGroup(1, 1,
              throughputTimer (1.0),
              httpSampler("http://localhost")
          )
      );
    }

  }

}
