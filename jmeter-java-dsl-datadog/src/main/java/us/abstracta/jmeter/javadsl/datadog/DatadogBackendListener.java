package us.abstracta.jmeter.javadsl.datadog;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jmeter.config.Arguments;
import org.datadog.jmeter.plugins.DatadogBackendClient;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.params.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam.EnumPropertyValue;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringArrayParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.listeners.DslBackendListener;

/**
 * Test element which publishes all test run metrics to DataDog.
 *
 * @since 1.13
 */
public class DatadogBackendListener extends DslBackendListener<DatadogBackendListener> {

  public static final String API_KEY_ARG = "apiKey";
  public static final String API_URL_ARG = "datadogUrl";
  public static final String LOG_URL_ARG = "logIntakeUrl";
  public static final String RESULT_LOGS_ARG = "sendResultsAsLogs";
  public static final String TAGS_ARG = "customTags";
  protected final String apiKey;
  protected String apiUrl;
  protected String logsUrl;
  protected boolean resultsAsLogs = false;
  protected String[] tags;

  public DatadogBackendListener(String apiKey) {
    super(DatadogBackendClient.class, null);
    this.apiKey = apiKey;
    site(DatadogSite.US1);
  }

  /**
   * Creates a new DataDog Backend listener posting sample result metrics to DataDog for easy
   * reporting and analysis.
   *
   * @param apiKey specifies the DataDog api key. To create one just go to <a
   *               href="https://app.datadoghq.com/organization-settings/api-keys">your organization
   *               api keys settings</a>.
   * @return the listener for further configuration and usage.
   */
  public static DatadogBackendListener datadogListener(String apiKey) {
    return new DatadogBackendListener(apiKey);
  }

  /**
   * Allows selecting the site of your DataDog instance to use proper URLs for such site.
   *
   * @param site specifies the site to use. When no site is specified, US1 is used by default.
   * @return the listener for further configuration and usage.
   */
  public DatadogBackendListener site(DatadogSite site) {
    apiUrl = site.propertyValue();
    logsUrl = String.format("https://http-intake.logs.%s/v1/input", site.domain);
    return this;
  }

  public enum DatadogSite implements EnumPropertyValue {
    US1("datadoghq.com"), US3("us3.datadoghq.com"), US5("us5.datadoghq.com"), EU(
        "datadoghq.eu"), US1_FED("ddog-gov.com"), AP1("ap1.datadoghq.com");

    private final String domain;

    DatadogSite(String domain) {
      this.domain = domain;
    }

    @Override
    public String propertyValue() {
      return String.format("https://api.%s/api/", domain);
    }
  }

  /**
   * Allows to enable/disable sending sample results as logs to DataDog.
   * <p>
   * It is useful to enable sample results as logs to be able to get all the information of every
   * request in DataDog. But, enabling them incurs in additional DataDog usage (cost) and network
   * traffic. So, only enable them when you actually need them (eg: tracing issues, or you need
   * detail of each request).
   *
   * @param enabled specifies to enable logging of sample results to DataDog when true. By default,
   *                results logs are not sent to DataDog.
   * @return the listener for further configuration and usage.
   */
  public DatadogBackendListener resultsLogs(boolean enabled) {
    resultsAsLogs = enabled;
    return this;
  }

  /**
   * Allows to specify tags to add to every metric sent to DataDog.
   * <p>
   * This is useful in scenarios where you need to add additional metadata to the metric for later
   * on filtering, grouping and comparison in DataDog. You can check more information <a
   * href="https://docs.datadoghq.com/getting_started/tagging/">here</a>.
   *
   * @param tags specifies the tags to add to every metric.
   * @return the listener for further configuration and usage.
   */
  public DatadogBackendListener tags(String... tags) {
    this.tags = tags;
    return this;
  }

  @Override
  protected Arguments buildListenerArguments() {
    Arguments ret = new Arguments();
    ret.addArgument(API_KEY_ARG, apiKey);
    ret.addArgument(API_URL_ARG, apiUrl);
    ret.addArgument(LOG_URL_ARG, buildLogsUrl());
    ret.addArgument(RESULT_LOGS_ARG, String.valueOf(resultsAsLogs));
    ret.addArgument(TAGS_ARG, tags != null ? String.join(",", tags) : "");
    return ret;
  }

  private String buildLogsUrl() {
    if (tags == null) {
      return logsUrl;
    }
    try {
      return new URIBuilder(logsUrl)
          .addParameter("ddtags", String.join(",", tags))
          .toString();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static class CodeBuilder extends BackendListenerCodeBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(DatadogBackendClient.class, builderMethods);
    }

    @Override
    protected MethodCall buildBackendListenerCall(Map<String, String> args,
        Map<String, String> defaultValues) {
      String apiUrl = args.get(API_URL_ARG);
      return buildMethodCall(new StringParam(args.get(API_KEY_ARG)))
          .chain("site", new EnumParam<>(DatadogSite.class, apiUrl, DatadogSite.US1))
          .chain("resultsLogs", new BoolParam(args.get(RESULT_LOGS_ARG), false))
          .chain("tags", new StringArrayParam(args.get(TAGS_ARG)));
    }

  }

}
