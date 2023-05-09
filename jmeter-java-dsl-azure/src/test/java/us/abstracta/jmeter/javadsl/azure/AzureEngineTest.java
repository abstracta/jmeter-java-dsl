package us.abstracta.jmeter.javadsl.azure;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class AzureEngineTest {

  @Test
  public void shouldRunTestInAzure() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            httpSampler("https://localhost")
        )
    ).runIn(new AzureEngine(System.getenv("AZURE_CREDS"))
        .testName("jmeter-java-dsl")
        .testTimeout(Duration.ofMinutes(10)));
    assertThat(stats.overall().samplesCount()).isEqualTo(1);
  }

}
