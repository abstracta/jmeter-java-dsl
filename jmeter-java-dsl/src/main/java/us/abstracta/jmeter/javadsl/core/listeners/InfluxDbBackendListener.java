package us.abstracta.jmeter.javadsl.core.listeners;

import java.time.Instant;
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
   */
  public InfluxDbBackendListener queueSize(int queueSize) {
    setQueueSize(queueSize);
    return this;
  }

  @Override
  protected Arguments buildListenerArguments() {
    Arguments ret = new Arguments();
    ret.addArgument("influxdbUrl", url);
    ret.addArgument("summaryOnly", "false");
    ret.addArgument("testTitle", title);
    if (token != null) {
      ret.addArgument("influxdbToken", token);
    }
    return ret;
  }

}
