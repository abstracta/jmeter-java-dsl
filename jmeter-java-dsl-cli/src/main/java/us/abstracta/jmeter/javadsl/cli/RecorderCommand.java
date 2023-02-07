package us.abstracta.jmeter.javadsl.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.output.NullOutputStream;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeDriverService.Builder;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.IDefaultValueProvider;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.OptionSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import us.abstracta.jmeter.javadsl.cli.RecorderCommand.YamlConfigProvider;
import us.abstracta.jmeter.javadsl.cli.recorder.JmeterDslRecorder;

@Command(name = "recorder", header = "Record a JMeter DSL test plan using a browser",
    defaultValueProvider = YamlConfigProvider.class)
public class RecorderCommand implements Callable<Integer> {

  private static final Logger LOG = LoggerFactory.getLogger(RecorderCommand.class);
  private static final Duration BROWSER_OPEN_POLL_PERIOD = Duration.ofMillis(500);
  private static final String CONFIG_OPTION = "--config";

  @Option(names = {CONFIG_OPTION}, paramLabel = "YAML_CONFIG_FILE",
      description = {
          "Yaml configuration file as an alternative to command line options.",
          "This provides an alternative to passing every configuration as parameter and make "
              + "configuration more reusable and shareable.",
          "Each option corresponds to a property in yaml file under `recorder` element "
              + "replacing hyphens with camel case (eg: --header-excludes, is renamed to "
              + "headerExcludes).",
          "Check the config file schema at: "
              + "https://github.com/abstracta/jmeter-java-dsl/releases/download/"
              + "v${sys:jmdsl.version}/jmdsl-config-schema.json.",
          "Default: ${DEFAULT-VALUE}."
      }, defaultValue = ".jmdsl.yml")
  private File config;

  @Option(names = {"--workdir"}, defaultValue = "recording",
      description = {"Directory where logs (eg: jtl files) and other relevant data is stored.",
          "This directory contains useful information for reviewing recording details to tune "
              + "recorded test plan, or to trace issues.",
          "Default: ${DEFAULT_VALUE}"})
  private String workdir;

  @ArgGroup(validate = false, heading = "Requests filtering:%n")
  private RequestsFiltering requestsFiltering = new RequestsFiltering();

  private static class RequestsFiltering {

    private static final String URL_INCLUDES_OPTION = "--url-includes";
    private static final String URL_EXCLUDES_OPTION = "--url-excludes";
    private static final String IGNORE_DEFAULT_URL_FILTER_OPTION = "--url-ignore-default-filter";

    @Option(names = {URL_INCLUDES_OPTION}, paramLabel = "REGEX", split = ",",
        description = {
            "Regular expressions which specify to only record requests with matching URLs.",
            "URLs are stripped from the scheme part (eg: http://) before matching the pattern. "
                + "E.g: mysite.com/accounts is used instead of http://mysite.com/accounts).",
            "Usually you would like to use a regex that matches the domain of the service under "
                + "test to avoid generating load to external servers. Eg: " + URL_INCLUDES_OPTION
                + "=[^?]*mysite.com.*",
            "These regexes will be used in combination with " + URL_EXCLUDES_OPTION
                + " and default URLs filter.",
            "Check " + IGNORE_DEFAULT_URL_FILTER_OPTION
                + " for more details on default URLs filter."})
    private List<String> urlIncludes = Collections.emptyList();

    @Option(names = {URL_EXCLUDES_OPTION}, paramLabel = "REGEX", split = ",",
        description = {
            "Regular expressions which specify to NOT record requests with matching URLs.",
            "URLs are stripped from the scheme part (eg: http://) before matching the pattern. "
                + "E.g: mysite.com/accounts is used  instead of http://mysite.com/accounts).",
            "These regexes will be used in combination with " + URL_INCLUDES_OPTION
                + " and default URLs filter.",
            "Check " + IGNORE_DEFAULT_URL_FILTER_OPTION
                + " for more details on default URLs filter."})
    private List<String> urlExcludes = Collections.emptyList();

    @Option(names = {IGNORE_DEFAULT_URL_FILTER_OPTION},
        description = {"Specifies not to use the default URL filter.",
            "This option might be helpful when you want a recorded test plan that also exercises "
                + "downloading static resources/assets (like images, styles, etc).",
            "The default filter ignores URLs matching: " + JmeterDslRecorder.DEFAULT_EXCLUDED_URLS})
    private boolean ignoreDefaultUrlFilter;
  }

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
    private List<String> headerExcludes = Collections.emptyList();

    @Option(names = {IGNORE_DEFAULT_HEADER_FILTER_OPTION},
        description = {"Specifies not to use the default headers filter.",
            "This option might be helpful when you want a to record all headers which require "
                + "specific values by the service under test (eg: User-Agent, Referer, etc).",
            "The default filter ignores these headers: ${sys:defaultHeadersFilter}"})
    private boolean ignoreDefaultHeaderFilter;
  }

  @Parameters(paramLabel = "URL", description = {"Initial URL to start the recording",
      "E.g.: https://mysite.com"}, arity = "0..1")
  private String url;

  public RecorderCommand() {
    // This is required since is not possible to include non-constant values on annotations
    System.setProperty("defaultHeadersFilter",
        String.join(",", JmeterDslRecorder.DEFAULT_EXCLUDED_HEADERS));
  }

  @Override
  public Integer call() throws Exception {
    try (JmeterDslRecorder recorder = buildRecorder()) {
      ChromeDriver driver = new ChromeDriver(buildDriverService(),
          buildChromeOptions(recorder.getProxy()));
      try {
        if (url != null) {
          driver.get(url);
        }
        awaitDriverClosed(driver);
      } finally {
        driver.quit();
      }
    }
    return 0;
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
    return ret.start();
  }

  private ChromeDriverService buildDriverService() {
    ChromeDriverService ret = new Builder().build();
    ret.sendOutputTo(NullOutputStream.NULL_OUTPUT_STREAM);
    return ret;
  }

  private ChromeOptions buildChromeOptions(String proxyHost) {
    ChromeOptions ret = new ChromeOptions();
    Proxy proxy = new Proxy();
    proxy.setHttpProxy(proxyHost);
    proxy.setSslProxy(proxyHost);
    ret.setProxy(proxy);
    ret.setAcceptInsecureCerts(true);
    return ret;
  }

  private void awaitDriverClosed(ChromeDriver driver) throws InterruptedException {
    try {
      while (true) {
        driver.getWindowHandle();
        Thread.sleep(BROWSER_OPEN_POLL_PERIOD.toMillis());
      }
    } catch (NoSuchWindowException e) {
      LOG.debug("Detected window close", e);
    }
  }

  public static class YamlConfigProvider implements IDefaultValueProvider {

    private Map<String, Object> config;

    @Override
    public String defaultValue(ArgSpec argSpec) throws Exception {
      if (config == null) {
        OptionSpec configOption = argSpec.command().findOption(CONFIG_OPTION);
        Map<String, Object> configMap = loadConfig(configOption.getValue(),
            new File(configOption.defaultValue()));
        config = Optional.ofNullable(configMap.get(argSpec.command().name()))
            .map(c -> (Map<String, Object>) c)
            .orElse(Collections.emptyMap());
      }
      Object ret = config.get(buildConfigName(argSpec));
      if (ret == null) {
        return null;
      } else if (ret instanceof List) {
        return String.join(",", (List) ret);
      } else {
        return (String) ret;
      }
    }

    private Map<String, Object> loadConfig(File configFile, File defaultConfig) throws IOException {
      if (configFile == null) {
        if (!defaultConfig.exists()) {
          return Collections.emptyMap();
        }
        configFile = defaultConfig;
      }
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      return mapper.readValue(configFile, new TypeReference<Map<String, Object>>() {
      });
    }

    private String buildConfigName(ArgSpec argSpec) {
      return argSpec instanceof OptionSpec
          ? buildOptionConfigName((OptionSpec) argSpec)
          : argSpec.paramLabel().toLowerCase(Locale.US);
    }

    private String buildOptionConfigName(OptionSpec argSpec) {
      String optionName = argSpec.names()[0].substring(2);
      // start at 2 to skip -- prefix
      Matcher wordStartMatcher = Pattern.compile("-\\w")
          .matcher(optionName);
      StringBuilder ret = new StringBuilder();
      int position = 0;
      while (wordStartMatcher.find()) {
        ret.append(optionName, position, wordStartMatcher.start());
        // start at 1 to skip hyphen
        ret.append(wordStartMatcher.group().substring(1).toUpperCase(Locale.US));
        position = wordStartMatcher.end();
      }
      ret.append(optionName, position, optionName.length());
      return ret.toString();
    }

  }

}
