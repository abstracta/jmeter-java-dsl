package us.abstracta.jmeter.javadsl.core.logiccontrollers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.transaction;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslTransactionControllerTest extends JmeterDslTest {

  @Test
  public void shouldIncludeTransactionSampleInResultsWhenTestPlanWithTransaction()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            transaction("My Transaction",
                httpSampler(wiremockUri)
            )
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(2);
  }

}
