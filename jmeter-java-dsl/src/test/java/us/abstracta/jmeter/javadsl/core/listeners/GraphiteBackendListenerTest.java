package us.abstracta.jmeter.javadsl.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static us.abstracta.jmeter.javadsl.JmeterDsl.graphiteListener;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import devcsrj.okhttp3.logging.HttpLoggingInterceptor;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.MountableFile;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class GraphiteBackendListenerTest extends JmeterDslTest {

  private static final int GRAPHITE_RECEIVER_PORT = 2004;
  private static final int GRAPHITE_HTTP_PORT = 80;
  private static final Pattern GRAPHITE_METRIC_NAME_PATTERN = Pattern.compile(
      "jmeter\\.(.*?)\\.a\\.count");

  private static final Logger LOG = LoggerFactory.getLogger(GraphiteBackendListenerTest.class);

  @Test
  public void shouldSendMetricsToInfluxDbWhenInfluxDbListenerInPlan() throws Exception {
    try (GenericContainer<?> container = buildContainer()) {
      container.start();
      testPlan(
          threadGroup(1, TEST_ITERATIONS,
              httpSampler(SAMPLE_1_LABEL, wiremockUri),
              httpSampler(SAMPLE_2_LABEL, wiremockUri)
          ),
          graphiteListener("localhost:" + container.getMappedPort(GRAPHITE_RECEIVER_PORT))
      ).run();
      await()
          .atMost(Duration.ofSeconds(30))
          .pollInterval(Duration.ofSeconds(1))
          .untilAsserted(() -> assertThat(getRecordedMetrics(container))
              .isEqualTo(buildExpectedTotalCounts()));
    }
  }

  private GenericContainer<?> buildContainer() {
    return new GenericContainer<>("graphiteapp/graphite-statsd:1.1.10-5")
        .withExposedPorts(GRAPHITE_HTTP_PORT, GRAPHITE_RECEIVER_PORT)
        .withCopyFileToContainer(MountableFile.forClasspathResource("/graphite/carbon.conf"),
            "/opt/graphite/conf/carbon.conf")
        .withCopyFileToContainer(
            MountableFile.forClasspathResource("/graphite/storage-schemas.conf"),
            "/opt/graphite/conf/storage-schemas.conf")
        .waitingFor(new LogMessageWaitStrategy()
            .withRegEx(".*ok: run: nginx:.*")
            .withStartupTimeout(Duration.ofSeconds(60)));
  }

  private Map<String, Long> getRecordedMetrics(GenericContainer<?> container) throws Exception {
    List<TargetResult> ret = queryGraphite(
        "http://localhost:" + container.getMappedPort(GRAPHITE_HTTP_PORT),
        "summarize(jmeter.*.a.count,\"1h\")", "-1h");
    return ret.stream()
        .collect(Collectors.toMap(this::extractMetricName, r -> Math.round(r.datapoints.stream()
            .filter(d -> d.value != null)
            .findAny()
            .get().value)));
  }

  private String extractMetricName(TargetResult r) {
    String targetName = r.tags.get("name");
    Matcher m = GRAPHITE_METRIC_NAME_PATTERN.matcher(targetName);
    if (!m.matches()) {
      throw new RuntimeException("No match of target for " + targetName);
    }
    String ret = m.group(1);
    return "all".equals(ret) ? "overall" : ret;
  }

  private List<TargetResult> queryGraphite(String url, String target, String from)
      throws Exception {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    if (LOG.isDebugEnabled()) {
      builder.addInterceptor(new HttpLoggingInterceptor());
    }
    OkHttpClient httpClient = builder.build();
    try {
      GraphiteApi api = new Retrofit.Builder()
          .baseUrl(url)
          .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()
              .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)))
          .client(httpClient)
          .build()
          .create(GraphiteApi.class);
      Response<List<TargetResult>> ret = api.queryMetrics(target, "json", from).execute();
      if (!ret.isSuccessful()) {
        try (ResponseBody errorBody = ret.errorBody()) {
          throw new RuntimeException(
              "Error querying graphite: " + ret.code() + " - " + errorBody.string());
        }
      }
      return ret.body();
    } finally {
      httpClient.dispatcher().executorService().shutdown();
      httpClient.connectionPool().evictAll();
    }
  }

  public interface GraphiteApi {

    @GET("render")
    Call<List<TargetResult>> queryMetrics(@Query("target") String target,
        @Query("format") String format, @Query("from") String from);

  }

  public static class TargetResult {

    private String target;
    private Map<String, String> tags;
    private List<Datapoint> datapoints;
  }

  @JsonFormat(shape = JsonFormat.Shape.ARRAY)
  @JsonPropertyOrder({"value", "timestamp"})
  public static class Datapoint {

    private Double value;
    private long timestamp;
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithGraphiteListener() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              graphiteListener("localhost")
          )
      );
    }

    public DslTestPlan testPlanWithGraphiteListenerAndNonDefaultSettings() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              graphiteListener("localhost:2005")
                  .metricsPrefix("jmeterdsl.")
          )
      );
    }

  }
}
