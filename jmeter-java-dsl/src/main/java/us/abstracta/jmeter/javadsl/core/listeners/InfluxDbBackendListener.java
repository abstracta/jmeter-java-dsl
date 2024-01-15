package us.abstracta.jmeter.javadsl.core.listeners;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.visualizers.backend.influxdb.InfluxdbBackendListenerClient;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;

/**
 * Test element which publishes all test run metrics to an InfluxDB instance.
 *
 * @since 0.4
 */
public class InfluxDbBackendListener extends DslBackendListener<InfluxDbBackendListener> {

  private static final String URL_ARG = "influxdbUrl";
  private static final String TITLE_ARG = "testTitle";
  private static final String MEASUREMENT_ARG = "measurement";
  private static final String APPLICATION_ARG = "application";
  private static final String TOKEN_ARG = "influxdbToken";
  private static final String SAMPLERS_REGEX_ARG = "samplersRegex";
  private static final String TAG_ARGS_PREFIX = "TAG_";
  private static final String PCT_ARG = "percentiles";

  protected String title = "Test jmeter-java-dsl " + Instant.now().toString();
  protected String token;
  protected final Map<String, String> tags = new HashMap<>();
  protected String samplersRegex;
  protected String measurement;
  protected String applicationName;
  protected String percentiles;

  public InfluxDbBackendListener(String url) {
    super(InfluxdbBackendListenerClient.class, url);
  }

  /**
   * Allows specifying a token for authentication with InfluxDB 2 instances.
   * <p>
   * Check <a href="https://docs.influxdata.com/influxdb/v2.0/security/">InfluxDB documentation</a>
   * for more details.
   *
   * @param token to use to authenticate to InfluxDB
   * @return the listener for further configuration or usage.
   */
  public InfluxDbBackendListener token(String token) {
    this.token = token;
    return this;
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
   * @return the listener for further configuration or usage.
   */
  public InfluxDbBackendListener title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Allows specifying an application name tag to be included with collected metrics.
   * <p>
   * This name can later be used to identify tests generated by this application on a given
   * measurement, from other applications.
   *
   * @param applicationName specifies the name of the application tag.
   * @return the listener for further configuration or usage.
   * @since 0.38
   */
  public InfluxDbBackendListener application(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

  /**
   * Allows specifying the name of the measurement that contains collected metrics sent to
   * InfluxDB.
   *
   * @param measurement specifies the name of the measurement.
   * @return the listener for further configuration or usage.
   * @since 0.38
   */
  public InfluxDbBackendListener measurement(String measurement) {
    this.measurement = measurement;
    return this;
  }

  /**
   * Allows specifying a regular expression used to filter collected metrics.
   * <p>
   * This regular expression is applied to sample labels, and when matched, collected sample metrics
   * will be sent to InfluxDB. Otherwise, they will be ignored.
   * <p>
   * For example ^[^_].*" - will exclude samplers which labels start with symbol "_".
   *
   * @param samplersRegex specifies the name of the samplersRegex.
   * @return the listener for further configuration or usage.
   * @since 0.38
   */
  public InfluxDbBackendListener samplersRegex(String samplersRegex) {
    this.samplersRegex = samplersRegex;
    return this;
  }

  /**
   * Allows adding tags to be included with every measurement sent to InfluxDB.
   *
   * @param name  specifies the name of the tag. Take into consideration that, in contrast to JMeter
   *              GUI, no <pre>TAG_</pre> prefix should be included.
   * @param value specifies the value of the tag.
   * @return the listener for further configuration or usage.
   */
  public InfluxDbBackendListener tag(String name, String value) {
    tags.put(name, value);
    return this;
  }

  /**
   * Allows specifying a list of percentiles that will be calculated and sent to InfluxDb.
   * For example "50;95" - will send pct50.0 and pct95.0.
   *
   * @param percentiles specifies a list of percentiles separated by ';'.
   * @return the listener for further configuration or usage.
   */
  public InfluxDbBackendListener percentiles(String percentiles) {
    this.percentiles = percentiles;
    return this;
  }

  @Override
  protected Arguments buildListenerArguments() {
    Arguments ret = new Arguments();
    ret.addArgument(URL_ARG, url);
    if (token != null) {
      ret.addArgument(TOKEN_ARG, token);
    }
    ret.addArgument("summaryOnly", "false");
    ret.addArgument(TITLE_ARG, title);
    if (applicationName != null) {
      ret.addArgument(APPLICATION_ARG, applicationName);
    }
    if (measurement != null) {
      ret.addArgument(MEASUREMENT_ARG, measurement);
    }
    if (samplersRegex != null) {
      ret.addArgument(SAMPLERS_REGEX_ARG, samplersRegex);
    }
    if (percentiles != null) {
      ret.addArgument(PCT_ARG, percentiles);
    }
    tags.forEach((name, value) -> ret.addArgument(TAG_ARGS_PREFIX + name, value));
    return ret;
  }

  public static class CodeBuilder extends BackendListenerCodeBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(InfluxdbBackendListenerClient.class, builderMethods);
    }

    protected MethodCall buildBackendListenerCall(Map<String, String> args,
        Map<String, String> defaultValues) {
      MethodCall ret = buildMethodCall(new StringParam(args.get(URL_ARG)))
          .chain("token", buildArgParam(TOKEN_ARG, args, defaultValues))
          .chain("title", buildArgParam(TITLE_ARG, args, defaultValues))
          .chain("application", buildArgParam(APPLICATION_ARG, args, defaultValues))
          .chain("measurement", buildArgParam(MEASUREMENT_ARG, args, defaultValues))
          .chain("samplersRegex", buildArgParam(SAMPLERS_REGEX_ARG, args, defaultValues))
          .chain("percentiles", buildArgParam(PCT_ARG, args, defaultValues));
      args.entrySet().stream()
          .filter(e -> e.getKey().startsWith(TAG_ARGS_PREFIX))
          .forEach(
              e -> ret.chain("tag", new StringParam(e.getKey().substring(TAG_ARGS_PREFIX.length())),
                  new StringParam(e.getValue())));
      return ret;
    }

  }

}
