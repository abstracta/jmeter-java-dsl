package us.abstracta.jmeter.javadsl.datadog;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.constantTimer;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.datadog.DatadogBackendListener.datadogListener;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.datadog.DatadogBackendListener.DatadogSite;

public class DatadogBackendListenerTest {

  private static final String TAG = "app:jmeter-java-dsl";
  private static final String DATADOG_API_KEY_ENV_VAR = "DATADOG_API_KEY";
  private static final int TIME_THRESHOLD_SECONDS = 10;

  @Test
  public void shouldSendMetricsToDatadogWhenDataDogBackendListerInPlan() throws Exception {
    Instant start = Instant.now();
    testPlan(
        threadGroup(1, 1,
            httpSampler("http://localhost")
                .children(constantTimer(Duration.ofSeconds(1))),
            datadogListener(System.getenv(DATADOG_API_KEY_ENV_VAR))
                .tags(TAG)
        )
    ).run();
    assertThat(findJmeterDatadogResponseCountSince(start)).isEqualTo(1);
  }

  private int findJmeterDatadogResponseCountSince(Instant fromInstant) throws IOException {
    try (DatadogApiClient cli = new DatadogApiClient(System.getenv(DATADOG_API_KEY_ENV_VAR),
        System.getenv("DATADOG_APPLICATION_KEY"))) {
      return cli.getResponsesCount(fromInstant.plusSeconds(-TIME_THRESHOLD_SECONDS),
            Instant.now().plusSeconds(TIME_THRESHOLD_SECONDS), TAG);
    }
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public CodeBuilderTest() {
      codeGenerator.addBuildersFrom(DatadogBackendListener.class);
    }

    public DslTestPlan testPlanWithDefaultDatadogListener() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("https://localhost"),
              datadogListener("test")
          )
      );
    }

    public DslTestPlan testPlanWithNonDefaultDatadogListener() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              datadogListener("test")
                  .site(DatadogSite.EU)
                  .resultsLogs(true)
                  .tags("jmeter-dsl", "test")
                  .queueSize(5)
          )
      );
    }

  }

}
