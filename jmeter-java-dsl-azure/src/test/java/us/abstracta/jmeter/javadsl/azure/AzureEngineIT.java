package us.abstracta.jmeter.javadsl.azure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.abstracta.jmeter.javadsl.JmeterDsl.autoStop;
import static us.abstracta.jmeter.javadsl.JmeterDsl.csvDataSet;
import static us.abstracta.jmeter.javadsl.JmeterDsl.dummySampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.core.listeners.AutoStopListener.AutoStopCondition.sampleTime;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.engines.AutoStoppedTestException;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class AzureEngineIT {

  @Test
  public void shouldRunTestInAzure() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            csvDataSet(new TestResource("users.csv")).randomOrder(),
            dummySampler("${USER}")
        )
    ).runIn(new AzureEngine(System.getenv("AZURE_CREDS"))
        .testName("jmeter-java-dsl")
        .testTimeout(Duration.ofMinutes(10)));
    assertThat(stats.overall().samplesCount()).isEqualTo(1);
  }

  @Test
  public void shouldAutoStopTestWhenConditionIsMet() {
    assertThrows(AutoStoppedTestException.class, () ->
        testPlan(
            threadGroup(1, Duration.ofMinutes(1),
                dummySampler("OK")
                    .responseTime(Duration.ofSeconds(1))
                    .simulateResponseTime(true),
                autoStop()
                    .when(sampleTime().percentile(99).greaterThan(Duration.ZERO))
            )
        ).runIn(new AzureEngine(System.getenv("AZURE_CREDS"))
            .testName("jmeter-java-dsl")
            .testTimeout(Duration.ofMinutes(10)))
    );
  }

}
