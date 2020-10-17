package us.abstracta.jmeter.javadsl.core.visualizers;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.influxDbListener;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.influxdb.querybuilder.BuiltQuery.QueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.InfluxDBContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class InfluxDbBackendListenerTest extends JmeterDslTest {

  private static final Logger LOG = LoggerFactory.getLogger(JmeterDslTest.class);
  private static final String INFLUXDB_DATABASE = "jmeter";

  @Test
  public void shouldSendMetricsToInfluxDbWhenInfluxDbListenerInPlan() throws IOException {
    try (InfluxDBContainer<?> influxDbContainer = buildInfluxDbContainer()) {
      influxDbContainer.start();
      testPlan(
          threadGroup(1, TEST_ITERATIONS,
              JmeterDsl.httpSampler(SAMPLE_1_LABEL, wiremockUri),
              JmeterDsl.httpSampler(SAMPLE_2_LABEL, wiremockUri)
          ),
          influxDbListener(influxDbContainer.getUrl() + "/write?db=" + INFLUXDB_DATABASE)
      ).run();
      assertThat(getInfluxDbRecordedMetrics(influxDbContainer))
          .isEqualTo(buildExpectedTotalCounts());
    }
  }

  private InfluxDBContainer<?> buildInfluxDbContainer() {
    return new InfluxDBContainer<>(
        DockerImageName.parse("influxdb:1.8"))
        .withDatabase(INFLUXDB_DATABASE)
        .withAuthEnabled(false)
        .withLogConsumer(new Slf4jLogConsumer(LOG));
  }

  @NotNull
  private Map<String, Long> getInfluxDbRecordedMetrics(InfluxDBContainer<?> influxDbContainer) {
    QueryResult result = influxDbContainer.getNewInfluxDB()
        .query(QueryBuilder.select().sum("count").as("count")
            .from(INFLUXDB_DATABASE, "jmeter")
            .where(QueryBuilder.eq("statut", "all"))
            .groupBy("transaction"));
    Map<String, Long> map = new InfluxDBResultMapper().toPOJO(result, TransactionCount.class)
        .stream()
        .collect(Collectors.toMap(c -> c.transaction, c -> c.count));
    renameMapKey("all", OVERALL_STATS_LABEL, map);
    return map;
  }

  @Measurement(name = "jmeter")
  public static class TransactionCount {

    @Column(name = "transaction")
    public String transaction;
    @Column(name = "count")
    public long count;

  }

  private void renameMapKey(String fromKey, String toKey, Map<String, Long> map) {
    map.put(toKey, map.get(fromKey));
    map.remove(fromKey);
  }

}
