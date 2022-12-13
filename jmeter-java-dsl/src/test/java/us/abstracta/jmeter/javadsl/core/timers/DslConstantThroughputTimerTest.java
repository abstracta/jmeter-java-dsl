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
  public void checkCorrectSamplesConstantThroughputTimerCalMode1() throws Exception {

    TestPlanStats stats = testPlan(
            threadGroup (10,Duration.ofSeconds(10),
                    dummySampler ("dummy","foo"),
                    constantThroughputTimer(DslConstantThroughputTimer.CalcModes.ThisThreadOnly,
                            120.0))
    ).run();
    assertThat(stats.byLabel ("dummy").samplesCount()).isBetween(196L,200L);
  }
  
  @Test
  public void checkCorrectSamplesConstantThroughputTimerCalMode2() throws Exception {

    TestPlanStats stats = testPlan(
            constantThroughputTimer(DslConstantThroughputTimer.CalcModes.AllActiveThreads,
                    120.0),
            threadGroup (10,Duration.ofSeconds(10),
                    dummySampler ("dummy","foo")),
            threadGroup (10,Duration.ofSeconds(10),
                    dummySampler ("dummy","foo"))
    ).run();
    assertThat(stats.byLabel ("dummy").samplesCount()).isBetween(38L,39L);
  }
  
  @Test
  public void checkCorrectSamplesConstantThroughputTimerCalMode3() throws Exception {

    TestPlanStats stats = testPlan(
            threadGroup (10,Duration.ofSeconds(10),
                     dummySampler ("dummy","foo"),
                       constantThroughputTimer(DslConstantThroughputTimer.CalcModes.AllActiveThreadsInCurrentThreadGroup,
                            120.0))

    ).run();
    assertThat(stats.byLabel ("dummy").samplesCount()).isBetween(28L,30L);

  }
  
  @Test
  public void checkCorrectSamplesConstantThroughputTimerCalMode4() throws Exception {

    TestPlanStats stats = testPlan(
            constantThroughputTimer(DslConstantThroughputTimer.CalcModes.AllActiveThreads_Shared,
                    120.0),
            threadGroup (10,Duration.ofSeconds(10),
                    dummySampler ("dummy","foo")),
            threadGroup (10,Duration.ofSeconds(10),
                    dummySampler ("dummy","foo"))
  ).run();
    assertThat(stats.byLabel ("dummy").samplesCount()).isEqualTo(20);
  }

  @Test
  public void checkCorrectSamplesConstantThroughputTimerCalMode5() throws Exception {

    TestPlanStats stats = testPlan(
            threadGroup (10,Duration.ofSeconds(10),
                    dummySampler ("dummy","foo"),
                      constantThroughputTimer(DslConstantThroughputTimer.CalcModes.AllActiveThreadsInCurrentThreadGroup_Shared,
                                            120.0))

    ).run();
    assertThat(stats.byLabel ("dummy").samplesCount()).isBetween(18L,20L);
  }
  
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithConstantTimer() {
      return testPlan(
          threadGroup(1, 1,
              constantThroughputTimer (DslConstantThroughputTimer.CalcModes.AllActiveThreads,1.0),
              httpSampler("http://localhost")
          )
      );
    }

  }

}
