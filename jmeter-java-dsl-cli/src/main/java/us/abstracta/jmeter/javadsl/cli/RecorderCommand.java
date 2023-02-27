package us.abstracta.jmeter.javadsl.cli;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.TypeConversionException;
import us.abstracta.jmeter.javadsl.cli.recorder.CorrelationRuleBuilder;
import us.abstracta.jmeter.javadsl.cli.recorder.JmeterDslRecorder;
import us.abstracta.jmeter.javadsl.cli.recorder.RecordingBrowser;

@Command(name = "recorder", header = "Record a JMeter DSL test plan using a browser",
    description = {
        "This is an initial implementation of recording facility. We have many ideas to improve "
            + "recording process, but we would really like to hear yours. Please open discussions "
            + "or issues in https://github.com/abstracta/jmeter-java-dsl repository so we can "
            + "improve this farther!"},
    usageHelpAutoWidth = true, sortOptions = false)
public class RecorderCommand implements Callable<Integer> {

  private static final String WORKDIR_OPTION = "--workdir";
  private static final String CORRELATIONS_OPTION = "--correlations";

  @Parameters(paramLabel = "URL", description = {"Initial URL to start the recording",
      "E.g.: https://mysite.com"}, arity = "0..1")
  private URL url;

  @Option(names = {WORKDIR_OPTION}, defaultValue = "recording",
      description = {"Directory where logs (eg: jtl files) and other relevant data is stored.",
          "E.g.: " + WORKDIR_OPTION + "=target/recording",
          "This directory contains useful information for reviewing recording details to tune "
              + "recorded test plan, or to trace issues.",
          "Default: ${DEFAULT_VALUE}"})
  private File workdir;

  @Option(names = {JmdslConfig.CONFIG_OPTION}, paramLabel = "YAML_CONFIG_FILE",
      description = {
          "Yaml configuration file as an alternative to command line options.",
          "E.g.: " + JmdslConfig.CONFIG_OPTION + "=mysite.jmdsl.yml",
          "This provides an alternative to passing every configuration as parameter and make "
              + "configuration more reusable and shareable.",
          "Each recorder command option can be set in a property in yaml file under `recorder` "
              + "element, replacing hyphens with camel case (eg: --header-excludes, is renamed to "
              + "headerExcludes).",
          "You can check the config file schema for more details at: "
              + "https://github.com/abstracta/jmeter-java-dsl/releases/download/"
              + "v${sys:jmdsl.version}/jmdsl-config-schema.json.",
          "Default: ${DEFAULT-VALUE}."
      }, defaultValue = JmdslConfig.DEFAULT_CONFIG_FILE)
  @JsonIgnore
  private File configFile;

  @Option(names = {CORRELATIONS_OPTION}, paramLabel = "VARIABLE,EXTRACTOR_REGEX,REPLACEMENT_REGEX",
      split = ";", converter = CorrelationRuleBuilderConverter.class,
      description = {
          "Correlation rules which define what parts of responses have to be used in following "
              + "requests.",
          "E.g.: " + CORRELATIONS_OPTION + "='productId,name=\\\"productId\\\" "
              + "value=\\\"([^\\\"]+)\\\",productId=(.*);productPrice,name=\\\"productPrice\\\" "
              + "value=\\\"([^\\\"]+)\\\",productPrice=(.*)'",
          "Defining proper correlation rules avoids fixed values in recorded test plans, making "
              + "them more resilient to changes.",
          "Each rule is defined by a variable name (to store extracted values), a regular "
              + "expression to extract values to be used in following requests and another regular "
              + "expression to replace extracted values in requests.",
          "First capturing group of extractor regex captures the actual value to be extracted.",
          "First capturing group of replacement regex defines the actual string to be replaced by "
              + "the variable reference.",
          "Consider specifying correlation rules in a config yaml (through "
              + JmdslConfig.CONFIG_OPTION + " option) file to avoid command line options "
              + "limitations, foster re-usability, and easier tuning."})
  private List<CorrelationRuleBuilder> correlations = Collections.emptyList();

  @JsonUnwrapped
  @ArgGroup(validate = false, heading = "Requests filtering:%n")
  private RequestsFiltering requestsFiltering = new RequestsFiltering();

  private static class RequestsFiltering {

    private static final String URL_INCLUDES_OPTION = "--url-includes";
    private static final String URL_EXCLUDES_OPTION = "--url-excludes";
    private static final String IGNORE_DEFAULT_URL_FILTER_OPTION = "--url-ignore-default-filter";

    @Option(names = {URL_INCLUDES_OPTION}, paramLabel = "REGEX", split = ",",
        description = {
            "Regular expressions which specify to only record requests with matching URLs.",
            "E.g.: " + URL_INCLUDES_OPTION + "=[^?]*mysite.com.*",
            "URLs are stripped from the scheme part (eg: http://) before matching the pattern. "
                + "E.g: mysite.com/accounts is used instead of http://mysite.com/accounts).",
            "Usually you would like to use a regex that matches the domain of the service under "
                + "test to avoid generating load to external servers.",
            "These regexes will be used in combination with " + URL_EXCLUDES_OPTION
                + " and default URLs filter.",
            "Check " + IGNORE_DEFAULT_URL_FILTER_OPTION
                + " for more details on default URLs filter."})
    private List<Pattern> urlIncludes = Collections.emptyList();

    @Option(names = {URL_EXCLUDES_OPTION}, paramLabel = "REGEX", split = ",",
        description = {
            "Regular expressions which specify to NOT record requests with matching URLs.",
            "E.g.: " + URL_EXCLUDES_OPTION + "=(?i).*\\.html(\\?.*)?",
            "URLs are stripped from the scheme part (eg: http://) before matching the pattern. "
                + "E.g: mysite.com/accounts is used  instead of http://mysite.com/accounts).",
            "These regexes will be used in combination with " + URL_INCLUDES_OPTION
                + " and default URLs filter.",
            "Check " + IGNORE_DEFAULT_URL_FILTER_OPTION
                + " for more details on default URLs filter."})
    private List<Pattern> urlExcludes = Collections.emptyList();

    @Option(names = {IGNORE_DEFAULT_URL_FILTER_OPTION},
        description = {"Specifies not to use the default URL filter.",
            "This option might be helpful when you want a recorded test plan that also exercises "
                + "downloading static resources/assets (like images, styles, etc).",
            "The default filter ignores URLs matching: " + JmeterDslRecorder.DEFAULT_EXCLUDED_URLS})
    private boolean ignoreDefaultUrlFilter;
  }

  @JsonUnwrapped
  @ArgGroup(validate = false, heading = "Headers filtering:%n")
  private HeadersFiltering headersFiltering = new HeadersFiltering();

  private static class HeadersFiltering {

    private static final String HEADER_EXCLUDES_OPTION = "--header-excludes";
    private static final String IGNORE_DEFAULT_HEADER_FILTER_OPTION =
        "--header-ignore-default-filter";

    @Option(names = {HEADER_EXCLUDES_OPTION}, paramLabel = "REGEX", split = ",",
        description = {
            "Regular expressions which specify to ignore matching headers from recording.",
            "E.g.: " + HEADER_EXCLUDES_OPTION + "=(?i)sec-.*",
            "These regexes will be used in combination with default headers filter.",
            "Check " + IGNORE_DEFAULT_HEADER_FILTER_OPTION
                + " for more details on default headers filter."})
    private List<Pattern> headerExcludes = Collections.emptyList();

    @Option(names = {IGNORE_DEFAULT_HEADER_FILTER_OPTION},
        description = {"Specifies not to use the default headers filter.",
            "This option might be helpful when you want a to record all headers which require "
                + "specific values by the service under test (eg: User-Agent, Referer, etc).",
            "The default filter ignores headers matching: "
                + JmeterDslRecorder.DEFAULT_EXCLUDED_HEADERS})
    private boolean ignoreDefaultHeaderFilter;
  }

  @Override
  public Integer call() throws Exception {
    loadConfigFileDefaults();
    try (JmeterDslRecorder recorder = buildRecorder();
        RecordingBrowser browser = new RecordingBrowser(url, recorder.getProxy())) {
      browser.awaitClosed();
    }
    return 0;
  }

  private void loadConfigFileDefaults() throws IOException {
    new JmdslConfig(this)
        .updateWithDefaultsFrom(JmdslConfig.fromConfigFile(configFile));
  }

  private JmeterDslRecorder buildRecorder() throws IOException {
    JmeterDslRecorder ret = new JmeterDslRecorder()
        .logsDirectory(workdir);
    if (requestsFiltering.ignoreDefaultUrlFilter) {
      ret.clearUrlFilter();
    }
    ret.urlIncludes(requestsFiltering.urlIncludes);
    ret.urlsExcludes(requestsFiltering.urlExcludes);
    if (headersFiltering.ignoreDefaultHeaderFilter) {
      ret.clearHeaderFilter();
    }
    ret.headerExcludes(headersFiltering.headerExcludes);
    correlations.forEach(ret::correlationRule);
    return ret.start();
  }

  public static class CorrelationRuleBuilderConverter implements
      ITypeConverter<CorrelationRuleBuilder> {

    @Override
    public CorrelationRuleBuilder convert(String value) {
      String[] parts = value.split(",");
      if (parts.length < 2 || parts.length > 3) {
        throw new TypeConversionException(
            "Invalid format: must be 'variable,extractor,replacement' but was '" + value + "'");
      }
      return new CorrelationRuleBuilder(parts[0], compileRegex(parts[1]),
          parts.length > 2 ? compileRegex(parts[2]) : null);
    }

    private Pattern compileRegex(String param) {
      // remove quotes escaping required to avoid picocli interpretation
      return Pattern.compile(param.replace("\\\"", "\""));
    }

  }

}
