package us.abstracta.jmeter.javadsl.core.listeners;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.visualizers.backend.influxdb.InfluxdbBackendListenerClient;

/**
 * Test element which publishes all test run metrics to an InfluxDB instance.
 *
 * @since 0.4
 */
public class InfluxDbBackendListener extends DslBackendListener {

  private String title = "Test jmeter-java-dsl " + Instant.now().toString();
  private String token;
  private final Map<String, String> tags = new HashMap<>();
  private String samplersRegex;
  private String measurement;
  private String applicationName;

  public InfluxDbBackendListener(String url) {
    super(InfluxdbBackendListenerClient.class, url);
  }

  /**
   * Allows setting a title for the test which will be included in started and ended annotations in
   * "events" measurement.
   * <p>
   * Consider setting this value to something that properly describes your application and the
   * particular test run (some timestamp, some CI/CD build ID, some commit ID, etc.).
   * <p>
   * When not specified, this will default to "jmeter-java-dsl" plus the current timestamp.
   *
   * @param title to be included in started and ended annotations.
   * @return this instance for fluent API usage.
   */
  public InfluxDbBackendListener title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Allows specifying a token for authentication with InfluxDB 2 instances.
   * <p>
   * Check <a href="https://docs.influxdata.com/influxdb/v2.0/security/">InfluxDB documentation</a>
   * for more details.
   *
   * @param token to use to authenticate to InfluxDB
   * @return this instance for fluent API usage.
   */
  public InfluxDbBackendListener token(String token) {
    this.token = token;
    return this;
  }

  /**
   * Specifies the length of sample results queue used to asynchronously send the information to
   * InfluxDB.
   *
   * @param queueSize the size of the queue to use
   * @return this instance for fluent API usage.
   * @see #setQueueSize(int)
   * @since 0.32
   */
  public InfluxDbBackendListener queueSize(int queueSize) {
    setQueueSize(queueSize);
    return this;
  }

  /**
   * Allows adding tags to be included with every measurement sent to InfluxDB.
   *
   * @param name specifies the name of the tag. Take into consideration that, in contrast to JMeter
   * GUI, no <pre>TAG_</pre> prefix should be included.
   * @param value specifies the value of the tag.
   * @return this instance for fluent API usage.
   */
  public InfluxDbBackendListener tag(String name, String value) {
    tags.put(name, value);
    return this;
  }

  /**
   * Allows specify the measurement which will contains all data of test.
   *
   * @param measurement specifies the name of the measurement.
   * @return this instance for fluent API usage.
   */
  public InfluxDbBackendListener measurement(String measurement) {
    this.measurement = measurement;
    return this;
  }

  /**
   * Allows specify the applicationName to do easily determinate tests on
   * this chosen application in results.
   *
   * @param applicationName specifies the name of the applicationName.
   * @return this instance for fluent API usage.
   */
  public InfluxDbBackendListener applicationName(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

  /**
   * Allows specify the samplersRegex exclude samplers from results.
   * Based on regular expressions.
   * Samplers which doesn't match this regexp not sending to Influxdb.
   * For example "^[^_].*" - will exclude
   * samplers which labels start with symbol "_".
   *
   * @param samplersRegex specifies the name of the samplersRegex.
   * @return this instance for fluent API usage.
   */
  public InfluxDbBackendListener samplersRegex(String samplersRegex) {
    this.samplersRegex = samplersRegex;
    return this;
  }

  @Override
  protected Arguments buildListenerArguments() {
    Arguments ret = new Arguments();
    ret.addArgument("influxdbUrl", url);
    ret.addArgument("summaryOnly", "false");
    ret.addArgument("testTitle", title);
    if (measurement != null) {
      ret.addArgument("measurement", measurement);
    }
    if (applicationName != null) {
      ret.addArgument("applicationName", applicationName);
    }
    if (samplersRegex != null) {
      ret.addArgument("samplersRegex", samplersRegex);
    }
    if (token != null) {
      ret.addArgument("influxdbToken", token);
    }
    tags.forEach((name, value) -> ret.addArgument("TAG_" + name, value));
    return ret;
  }

}
