package us.abstracta.jmeter.javadsl.prometheus;

import static us.abstracta.jmeter.javadsl.JmeterDsl.dummySampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.prometheus.DslPrometheusListener.prometheusListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DslPrometheusListenerTest {

  private static final Logger LOG = LoggerFactory.getLogger(DslPrometheusListenerTest.class);

  @Test
  public void shouldPublishDefaultMetricsWhenPrometheusListenerWithoutConfig() throws Exception {
    MetricsConditionChecker collector = new MetricsConditionChecker();
    runConcurrently(
        testPlanRunner(),
        collector
    );
  }

  @SafeVarargs
  private final void runConcurrently(Callable<Void>... callables) throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(callables.length);
    try {
      List<Future<Void>> rets = executor.invokeAll(Arrays.asList(callables));
      for (Future<Void> ret : rets) {
        ret.get();
      }
    } finally {
      executor.shutdownNow();
    }
  }

  private Callable<Void> testPlanRunner() {
    return () -> {
      testPlan(
          threadGroup(1, 1,
              dummySampler("OK")
          ),
          prometheusListener()
      ).run();
      return null;
    };
  }

  private static class MetricsConditionChecker implements Callable<Void> {

    private static final Duration POLL_PERIOD = Duration.ofSeconds(1);
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(15);

    @Override
    public Void call() throws InterruptedException, TimeoutException {
      Instant start = Instant.now();
      do {
        Thread.sleep(POLL_PERIOD.toMillis());
        try {
          String response = urlQuery("http://localhost:9270/metrics");
          if (response.matches(".*Ratio_success_total\\{[^}]+} 1\\.0.*")) {
            return null;
          }
        } catch (IOException | IllegalStateException e) {
          LOG.warn("Failed to poll metrics", e);
        }
      } while (Instant.now().isBefore(start.plus(POLL_TIMEOUT)));
      throw new TimeoutException(
          "Could not collect the expected metrics within the timeout of " + POLL_TIMEOUT
              + " seconds");
    }

    private String urlQuery(String url) throws IOException {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      try {
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
          BufferedReader in = new BufferedReader(
              new InputStreamReader(connection.getInputStream()));
          String inputLine;
          StringBuilder response = new StringBuilder();
          while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
          }
          in.close();
          return response.toString();
        } else {
          throw new IllegalStateException("HTTP error code : " + responseCode);
        }
      } finally {
        connection.disconnect();
      }
    }
  }
}
