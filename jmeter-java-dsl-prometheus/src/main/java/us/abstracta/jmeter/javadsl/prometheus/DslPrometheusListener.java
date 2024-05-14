package us.abstracta.jmeter.javadsl.prometheus;

import com.github.johrstrom.collector.BaseCollectorConfig.JMeterCollectorType;
import com.github.johrstrom.listener.ListenerCollectorConfig;
import com.github.johrstrom.listener.ListenerCollectorConfig.Measurable;
import com.github.johrstrom.listener.PrometheusListener;
import com.github.johrstrom.listener.PrometheusServer;
import com.github.johrstrom.listener.gui.PrometheusListenerGui;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import us.abstracta.jmeter.javadsl.core.listeners.BaseListener;

/**
 * Test element which publishes test run metrics in Prometheus endpoint.
 * <p>
 * This element uses <a
 * href="https://github.com/johrstrom/jmeter-prometheus-plugin">jmeter-prometheus-plugin</a>.
 *
 * @since 1.28
 */
public class DslPrometheusListener extends BaseListener {

  private final List<PrometheusMetric<?>> metrics = new ArrayList<>();
  private int port = 9270;
  private String host = "0.0.0.0";
  private Duration delay = Duration.ofSeconds(10);

  public DslPrometheusListener() {
    super("Prometheus Listener", PrometheusListenerGui.class);
  }

  /**
   * Creates a new Prometheus Listener publishing test run metrics in a Prometheus endpoint.
   * <p>
   * By default, this element publishes a default set of metrics in endpoint
   * http://0.0.0.0:9270/metrics. Use {@link #host(String)} and {@link #port(int)} to change the
   * endpoint, and {@link #metrics(PrometheusMetric...)} to set your own metrics.
   * <p>
   * The default published metrics are:
   * <pre>{@code
   * PrometheusMetric.responseTime("ResponseTime", "the response time of samplers")
   *   .labels(PrometheusMetric.SAMPLE_LABEL, PrometheusMetric.RESPONSE_CODE)
   *   .quantile(0.75, 0.5)
   *   .quantile(0.95, 0.1)
   *   .quantile(0.99, 0.01)
   *   .maxAge(Duration.ofMinutes(1)),
   * PrometheusMetric.successRatio("Ratio", "the success ratio of samplers")
   *   .labels(PrometheusMetric.SAMPLE_LABEL, PrometheusMetric.RESPONSE_CODE)
   * }</pre>
   *
   * @return the listener instance for further configuration or usage.
   */
  public static DslPrometheusListener prometheusListener() {
    return new DslPrometheusListener();
  }

  /**
   * Specifies a custom set of metrics to publish.
   *
   * @param metrics specifies the set of metrics to publish.
   * @return the listener instance for further configuration or usage.
   * @see PrometheusMetric
   */
  public DslPrometheusListener metrics(PrometheusMetric<?>... metrics) {
    this.metrics.addAll(Arrays.asList(metrics));
    return this;
  }

  /**
   * Specifies the port where to publish the metrics.
   * <p>
   * <b>Warning:</b> port setting is test plan wide. This means that if you set different values in
   * different prometheus listeners, then all the metrics will be published in the last set port.
   *
   * @param port specifies the port to publish the metrics. By default, it is 9270.
   * @return the listener instance for further configuration or usage.
   */
  public DslPrometheusListener port(int port) {
    this.port = port;
    return this;
  }

  /**
   * Specifies the host the internal prometheus server whill listen to requests for metrics.
   * <p>
   * <b>Warning:</b> host setting is test plan wide. This means that if you set different values in
   * different prometheus listeners, then all the metrics will be published in the last set host.
   *
   * @param host specifies the host to publish the metrics. By default, it is 0.0.0.0.
   * @return the listener instance for further configuration or usage.
   */
  public DslPrometheusListener host(String host) {
    this.host = host;
    return this;
  }

  /**
   * Specifies a duration to wait after the test run ends, before stop publishing the metrics.
   * <p>
   * <b>Warning:</b> It is very important to set this value greater than Prometheus server
   * scrape_interval to avoid missing metrics at the end of test execution.
   * <p>
   * <b>Warning:</b> this setting is test plan wide. This means that if you set different values in
   * different prometheus listeners, then only the last set value will be used.
   *
   * @param duration specifies the duration to wait before stop publishing the metrics. Take into
   *                 consideration that the JMeter plugin supports seconds granularity, so, if a
   *                 finer value is used, only the provided seconds will be used. By default, it is
   *                 set to 10 seconds.
   * @return the listener instance for further configuration or usage.
   */
  public DslPrometheusListener endWait(Duration duration) {
    this.delay = duration;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    if (metrics.isEmpty()) {
      metrics(PrometheusMetric.responseTime("ResponseTime", "the response time of samplers")
              .labels(PrometheusMetric.SAMPLE_LABEL, PrometheusMetric.RESPONSE_CODE)
              .quantile(0.75, 0.5)
              .quantile(0.95, 0.1)
              .quantile(0.99, 0.01)
              .maxAge(Duration.ofMinutes(1)),
          PrometheusMetric.successRatio("Ratio", "the success ratio of samplers")
              .labels(PrometheusMetric.SAMPLE_LABEL, PrometheusMetric.RESPONSE_CODE));
    }
    JMeterUtils.setProperty(PrometheusServer.PROMETHEUS_PORT, String.valueOf(port));
    JMeterUtils.setProperty(PrometheusServer.PROMETHEUS_IP, host);
    JMeterUtils.setProperty(PrometheusServer.PROMETHEUS_DELAY, String.valueOf(delay.getSeconds()));
    PrometheusListener ret = new PrometheusListener();
    ret.setCollectorConfigs(metrics.stream()
        .map(PrometheusMetric::buildCollectorConfig)
        .collect(Collectors.toList()));
    return ret;
  }

  /**
   * Allows configuring metrics to be published by the listener.
   */
  public abstract static class PrometheusMetric<T extends PrometheusMetric<T>> {

    public static final String SAMPLE_LABEL = "label";
    public static final String RESPONSE_CODE = "code";

    protected final String name;
    protected final String help;
    protected final Measurable measuring;
    protected final List<String> labels = new ArrayList<>();

    protected PrometheusMetric(String name, String help, Measurable measuring) {
      this.name = name;
      this.help = help;
      this.measuring = measuring;
    }

    /**
     * Configures a metric collect information about the samples response time.
     * <p>
     * By default, <a href="https://prometheus.io/docs/practices/histograms/">Prometheus Summary
     * metrics</a> are collected. Use {@link #labels(String...)},
     * {@link SummaryPrometheusMetric#quantile(double, double)} or
     * {@link SummaryPrometheusMetric#quantile(double, double)}{@link
     * SummaryPrometheusMetric#histogram(double...)} to further configure details on response time
     * collected metrics.
     *
     * @param name specifies the name of the metric.
     * @return the metric instance for further configuration or usage.
     */
    public static SummaryPrometheusMetric responseTime(String name) {
      return PrometheusMetric.responseTime(name, "");
    }

    /**
     * Same as {@link #responseTime(String)} but allows to set a help text.
     *
     * @see #responseTime(String)
     */
    public static SummaryPrometheusMetric responseTime(String name, String help) {
      return new SummaryPrometheusMetric(name, help, Measurable.ResponseTime);
    }

    /**
     * Configures a metric collect information about samples success and failures.
     * <p>
     * Review the <a
     * href="https://github.com/johrstrom/jmeter-prometheus-plugin?tab=readme-ov-file#success-ratio">JMeter
     * plugin documentation</a> for more details.
     *
     * @param name specifies the name of the metric.
     * @return the metric instance for further configuration or usage.
     */
    public static SuccessRatioPrometheusMetric successRatio(String name) {
      return PrometheusMetric.successRatio(name, "");
    }

    /**
     * Same as {@link #successRatio(String)} but allows to set a help text.
     *
     * @see #successRatio(String)
     */
    public static SuccessRatioPrometheusMetric successRatio(String name, String help) {
      return new SuccessRatioPrometheusMetric(name, help, Measurable.SuccessRatio);
    }

    /**
     * Add labels that enrich the collected metric data.
     * <p>
     * You can use pre-defined values {@link PrometheusMetric#SAMPLE_LABEL} and
     * {@link PrometheusMetric#RESPONSE_CODE} to get labels for each sample label and response
     * code.
     * <p>
     * Additionally, you can use JMeter variables values as labels. Check <a
     * href="https://github.com/johrstrom/jmeter-prometheus-plugin?tab=readme-ov-file#using-jmeter-variables-as-labels">this
     * section of JMeter plugin documentation</a> for more details.
     *
     * @param labels specifies the labels to add to the metric.
     * @return the metric instance for further configuration or usage.
     */
    public T labels(String... labels) {
      this.labels.addAll(Arrays.asList(labels));
      return (T) this;
    }

    public ListenerCollectorConfig buildCollectorConfig() {
      ListenerCollectorConfig ret = new ListenerCollectorConfig();
      ret.setMetricName(name);
      ret.setHelp(help);
      ret.setLabels(labels.toArray(new String[0]));
      configureCollectorConfig(ret);
      ret.setListenTo(ListenerCollectorConfig.SAMPLES);
      ret.setMeasuring(measuring.toString());
      return ret;
    }

    protected abstract void configureCollectorConfig(ListenerCollectorConfig config);

  }

  public static class SummaryPrometheusMetric extends PrometheusMetric<SummaryPrometheusMetric> {

    protected String quantiles = "";
    protected Duration maxAge = Duration.ofMinutes(1);

    protected SummaryPrometheusMetric(String name, String help, Measurable measuring) {
      super(name, help, measuring);
    }

    /**
     * Specifies to collect histogram data for the given metric.
     * <p>
     * Check <a href="https://prometheus.io/docs/practices/histograms/">Prometheus Documentation</a>
     * for an explanation on differences between using histograms and summaries.
     *
     * @param buckets specifies the bucket upper bounds where information is collected. This
     *                determines the granularity of the generated histogram.
     * @return the metric instance for further configuration or usage.
     */
    public HistogramPrometheusMetric histogram(double... buckets) {
      return new HistogramPrometheusMetric(this.name, this.help, this.measuring, buckets)
          .labels(this.labels.toArray(new String[0]));
    }

    /**
     * Adds a quantile value to calculate for the summary metric.
     *
     * @param quantile specifies the quantile value to calculate. Eg: 0.95 for the 95th percentile.
     *                 This must be a value between 0 and 1.
     * @param error    specifies the tolerance error for the calculated quantile. Smaller values
     *                 require more resources (CPU and RAM). This must be a value between 0 and 1.
     *                 Consider using 0.001 for quantiles greater than 0.99 or smaller than 0.01,
     *                 and 0.01 or bigger for the rest.
     * @return the metric instance for further configuration or usage.
     */
    public SummaryPrometheusMetric quantile(double quantile, double error) {
      quantiles += "|" + String.format("%f,%f", quantile, error);
      return this;
    }

    /**
     * Sets the size of the moving time window to calculate the quantiles.
     * <p>
     * This value depends on the period of time you want the quantiles calculation to be based on. A
     * bigger value requires more resources (CPU and RAM).
     *
     * @param duration specifies the size of the moving time window. Prometheus supports up to
     *                 seconds granularity for the time window, so if you use a more granular value
     *                 (like millis) on ly the seconds will be taken into consideration. By default,
     *                 this value is set to 1 minute.
     * @return the metric instance for further configuration or usage.
     */
    public SummaryPrometheusMetric maxAge(Duration duration) {
      maxAge = duration;
      return this;
    }

    @Override
    protected void configureCollectorConfig(ListenerCollectorConfig config) {
      config.setType(JMeterCollectorType.SUMMARY.toString());
      quantiles = !quantiles.isEmpty() ? quantiles.substring(1) : "";
      quantiles += ";" + maxAge.getSeconds();
      config.setQuantileOrBucket(quantiles);
    }

  }

  public static class HistogramPrometheusMetric extends
      PrometheusMetric<HistogramPrometheusMetric> {

    protected final String buckets;

    protected HistogramPrometheusMetric(String name, String help, Measurable measuring,
        double... buckets) {
      super(name, help, measuring);
      this.buckets = Arrays.stream(buckets)
          .mapToObj(String::valueOf)
          .collect(Collectors.joining(","));
    }

    @Override
    protected void configureCollectorConfig(ListenerCollectorConfig config) {
      config.setType(JMeterCollectorType.HISTOGRAM.toString());
      config.setQuantileOrBucket(buckets);
    }

  }

  public static class SuccessRatioPrometheusMetric extends
      PrometheusMetric<SuccessRatioPrometheusMetric> {

    protected SuccessRatioPrometheusMetric(String name, String help, Measurable measuring) {
      super(name, help, measuring);
    }

    @Override
    protected void configureCollectorConfig(ListenerCollectorConfig config) {
      config.setType(JMeterCollectorType.SUCCESS_RATIO.toString());
    }

  }

}
