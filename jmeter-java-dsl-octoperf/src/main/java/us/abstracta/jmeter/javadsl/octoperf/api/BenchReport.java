package us.abstracta.jmeter.javadsl.octoperf.api;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BenchReport {

  private final String id;
  private final List<String> benchResultIds;
  private final List<BenchReportItem> items;
  private String url;

  @JsonCreator
  public BenchReport(@JsonProperty("id") String id,
      @JsonProperty("benchResultIds") List<String> benchResultIds,
      @JsonProperty("items") List<BenchReportItem> items) {
    this.id = id;
    this.benchResultIds = benchResultIds;
    this.items = items;
  }

  public List<String> getBenchResultIds() {
    return benchResultIds;
  }

  public List<BenchReportItem> getItems() {
    return items;
  }

  public void setProject(Project project) {
    this.url = project.getBaseUrl() + "/analysis/" + id;
  }

  public String getUrl() {
    return url;
  }

  @JsonTypeInfo(use = NAME, include = PROPERTY, defaultImpl = Void.class)
  @JsonSubTypes({
      @JsonSubTypes.Type(SummaryReportItem.class),
      @JsonSubTypes.Type(StatisticTableReportItem.class),
  })
  public abstract static class BenchReportItem {

    // we don't need getters since Jackson gets the values from fields
    private final String id;
    private final String name;
    private final List<ReportItemMetric> metrics = new ArrayList<>();

    protected BenchReportItem(String id, String name) {
      this.id = id;
      this.name = name;
    }

    public List<ReportItemMetric> getMetrics() {
      return metrics;
    }

  }

  public static class ReportItemMetric {

    private final String id;
    private final String type = "HIT";
    private final List<ReportItemQueryFilter> filters = Collections.emptyList();
    private final String benchResultId;
    private final BenchReportConfig config = null;

    public ReportItemMetric() {
      id = null;
      benchResultId = null;
    }

    public ReportItemMetric(ReportMetricId id, String benchResultId) {
      this.id = id.name();
      this.benchResultId = benchResultId;
    }

    public String getBenchResultId() {
      return benchResultId;
    }

  }

  public enum ReportMetricId {
    ASSERTIONS_ERROR_TOTAL, ASSERTIONS_FAILURE_TOTAL, ASSERTIONS_SUCCESS_TOTAL, CONNECT_TIME_APDEX,
    CONNECT_TIME_AVG, CONNECT_TIME_MAX, CONNECT_TIME_MIN, CONNECT_TIME_STD, CONNECT_TIME_VAR,
    ERRORS_TOTAL, ERRORS_RATE, ERRORS_PERCENT, HITS_RATE, HITS_TOTAL, HITS_SUCCESSFUL_TOTAL,
    HITS_SUCCESSFUL_PERCENT, HTTP_MEDIA_TYPES, HTTP_MEDIA_TYPES_THROUGHPUT, HTTP_METHODS,
    HTTP_RESPONSE_CODES, LATENCY_APDEX, LATENCY_AVG, LATENCY_MAX, LATENCY_MIN, LATENCY_STD,
    LATENCY_VAR, MONITORING, NETWORK_TIME_AVG, NETWORK_TIME_MAX, NETWORK_TIME_MIN, NETWORK_TIME_VAR,
    RESPONSE_TIME_APDEX, RESPONSE_TIME_AVG, RESPONSE_TIME_MAX, RESPONSE_TIME_MEDIAN,
    RESPONSE_TIME_MIN, RESPONSE_TIME_PERCENTILE_80, RESPONSE_TIME_PERCENTILE_90,
    RESPONSE_TIME_PERCENTILE_95, RESPONSE_TIME_PERCENTILE_99, RESPONSE_TIME_STD,
    RESPONSE_TIME_VAR, RESPONSE_SIZE, SENT_BYTES_AVG, SENT_BYTES_MAX, SENT_BYTES_MIN,
    SENT_BYTES_RATE, SENT_BYTES_STD, SENT_BYTES_TOTAL, THROUGHPUT_RATE, THROUGHPUT_TOTAL, USERLOAD
  }

  public static class ReportItemQueryFilter {

  }

  public static class BenchReportConfig {

  }

  @JsonTypeName("SummaryReportItem")
  public static class SummaryReportItem extends BenchReportItem {

    @JsonCreator
    public SummaryReportItem(@JsonProperty("id") String id, @JsonProperty("name") String name) {
      super(id, name);
    }

  }

  @JsonTypeName("StatisticTableReportItem")
  public static class StatisticTableReportItem extends BenchReportItem {

    private final String computeType = "NORMAL";
    private final int size = 0;

    @JsonCreator
    public StatisticTableReportItem(@JsonProperty("id") String id,
        @JsonProperty("name") String name) {
      super(id, name);
    }

  }

}
