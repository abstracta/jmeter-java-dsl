package us.abstracta.jmeter.javadsl.core.assertions;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.responseAssertion;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import javax.naming.InitialContext;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslResponseAssertionTest extends JmeterDslTest {

  @Test
  public void shouldFailRequestWhenResponseAssertionDoesNotMatch() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            JmeterDsl.
                httpSampler(wiremockUri)
                .children(
                    responseAssertion()
                        .containsSubstrings("test")
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(1);
  }

  @Test
  public void shouldMarkRequestAsSuccessWhenInvalidRequestButResponseAssertionIgnoresStatus()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            JmeterDsl.
                httpSampler("invalidurl")
                .children(
                    responseAssertion()
                        .ignoreStatus()
                )
        )
    ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

}
