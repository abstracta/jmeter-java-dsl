package us.abstracta.jmeter.javadsl.core.postprocessors;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.Test;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslJsr223PostProcessorTest extends JmeterDslTest {

  @Test
  public void shouldReportNoFailureWhenJsr223PostProcessorModifiesFailedRequest() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            JmeterDsl.httpSampler("invalidUrl")
                .children(
                    jsr223PostProcessor("prev.successful = true")
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

}
