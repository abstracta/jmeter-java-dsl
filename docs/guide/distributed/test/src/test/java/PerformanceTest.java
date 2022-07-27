import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.engines.DistributedJmeterEngine;

public class PerformanceTest {

  @Test
  public void shouldGetExpectedCountWhenRunTestInRemoteEngine() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            httpSampler("https://myservice")
        )
    ).runIn(new DistributedJmeterEngine("server:1099"));
    assertThat(stats.overall().samplesCount()).isEqualTo(1);
  }

}
