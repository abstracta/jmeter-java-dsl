package us.abstracta.jmeter.javadsl.octoperf;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class OctoPerfEngineTest {

  @Test
  public void shouldRunTestInOctoPerf() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            httpSampler("https://localhost")
        )
    ).runIn(new OctoPerfEngine(System.getenv("OCTOPERF_API_KEY"))
        .projectName("jmeter-java-dsl")
        .totalUsers(1)
        .holdFor(Duration.ofSeconds(10))
        .testTimeout(Duration.ofMinutes(10)));
    assertThat(stats.overall().samplesCount()).isGreaterThanOrEqualTo(1);
  }

}
