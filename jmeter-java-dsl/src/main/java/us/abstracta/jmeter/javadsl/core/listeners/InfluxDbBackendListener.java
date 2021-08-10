package us.abstracta.jmeter.javadsl.core.listeners;

import java.time.Instant;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jmeter.visualizers.backend.BackendListenerGui;
import org.apache.jmeter.visualizers.backend.influxdb.InfluxdbBackendListenerClient;
import us.abstracta.jmeter.javadsl.core.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.MultiLevelTestElement;

/**
 * Test element which publishes all test run metrics to an InfluxDB instance.
 *
 * @since 0.4
 */
public class InfluxDbBackendListener extends BaseTestElement implements MultiLevelTestElement {

  private final String url;
  private String title = "Test jmeter-java-dsl " + Instant.now().toString();
  private String token;
  private int queueSize = 5000;

  public InfluxDbBackendListener(String url) {
    super("Backend Listener", BackendListenerGui.class);
    this.url = url;
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
   * <p>
   * When the queue reaches this limit, then the test plan execution will be affected since sample
   * results will get blocked until there is space in the queue, affecting the general execution of
   * the test plan and in consequence collected metrics.
   * <p>
   * When not specified, this value defaults to 5000.
   *
   * @param queueSize the size of the queue to use
   * @return this instance for fluent API usage.
   */
  public InfluxDbBackendListener queueSize(int queueSize) {
    this.queueSize = queueSize;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    BackendListener ret = new BackendListener();
    ret.setClassname(InfluxdbBackendListenerClient.class.getName());
    ret.setQueueSize(String.valueOf(queueSize));
    ret.setArguments(buildArguments());
    return ret;
  }

  private Arguments buildArguments() {
    Arguments ret = new InfluxdbBackendListenerClient().getDefaultParameters();
    setArgument("influxdbUrl", url, ret);
    setArgument("summaryOnly", "false", ret);
    setArgument("testTitle", title, ret);
    if (token != null) {
      setArgument("influxdbToken", token, ret);
    }
    return ret;
  }

  private void setArgument(String name, String value, Arguments args) {
    args.removeArgument(name);
    args.addArgument(name, value);
  }

}
