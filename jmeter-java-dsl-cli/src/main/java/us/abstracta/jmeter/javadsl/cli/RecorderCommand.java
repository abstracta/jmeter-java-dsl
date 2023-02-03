package us.abstracta.jmeter.javadsl.cli;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
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
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import us.abstracta.jmeter.javadsl.cli.recorder.JmeterDslRecorder;

@Command(name = "recorder", header = "Record a JMeter DSL test plan using a browser")
public class RecorderCommand implements Callable<Integer> {

  private static final Logger LOG = LoggerFactory.getLogger(RecorderCommand.class);
  private static final Duration BROWSER_OPEN_POLL_PERIOD = Duration.ofMillis(500);

  @Option(names = {"--workdir"}, defaultValue = "recording",
      description = {"Directory where logs (eg: jtl files) and other relevant data is stored.",
          "This directory contains useful information for reviewing recording details to tune "
              + "recorded test plan, or to trace issues."})
  private String workdir;

  @ArgGroup(validate = false, heading = "Requests filtering:%n")
  private RequestsFiltering requestsFiltering = new RequestsFiltering();

  private static class RequestsFiltering {

    private static final String URLS_MATCHING_OPTION = "--urls-matching";
    private static final String NOT_URLS_MATCHING_OPTION = "--not-urls-matching";
    private static final String IGNORE_DEFAULT_URLS_FILTER_OPTION = "--ignore-default-urls-filter";

    @Option(names = {URLS_MATCHING_OPTION}, paramLabel = "REGEX", split = ",",
        description = {
            "Regular expressions which specify to only record requests with matching URLs.",
            "Usually you would like to use a regex that matches the domain of the service under "
                + "test to avoid generating load to external servers. Eg: " + URLS_MATCHING_OPTION
                + "=[^?]*abstracta.us.*",
            "These regexes will be used in combination with " + NOT_URLS_MATCHING_OPTION
                + " and default URLs filter.",
            "Check " + IGNORE_DEFAULT_URLS_FILTER_OPTION
                + " for more details on default URLs filter."})
    private List<String> urlsMatching = Collections.emptyList();

    @Option(names = {NOT_URLS_MATCHING_OPTION}, paramLabel = "REGEX", split = ",",
        description = {
            "Regular expressions which specify to NOT record requests with matching URLs.",
            "These regexes will be used in combination with " + URLS_MATCHING_OPTION
                + " and default URLs filter.",
            "Check " + IGNORE_DEFAULT_URLS_FILTER_OPTION
                + " for more details on default URLs filter."})
    private List<String> notUrlsMatching = Collections.emptyList();

    @Option(names = {IGNORE_DEFAULT_URLS_FILTER_OPTION},
        description = {"Specifies not to use the default URL filter.",
            "This option might be helpful when you want a recorded test plan that also exercises "
                + "downloading static resources/assets (like images, styles, etc).",
            "The regular expression used for default filtering is: "
                + JmeterDslRecorder.DEFAULT_EXCLUDED_URLS})
    private boolean ignoreDefaultUrlsFilter;
  }

  @ArgGroup(validate = false, heading = "Headers filtering:%n")
  private HeadersFiltering headersFiltering = new HeadersFiltering();

  private static class HeadersFiltering {

    private static final String NOT_HEADERS_MATCHING_OPTION = "--not-headers-matching";
    private static final String IGNORE_DEFAULT_HEADERS_FILTER_OPTION =
        "--ignore-default-headers-filter";

    @Option(names = {NOT_HEADERS_MATCHING_OPTION}, paramLabel = "REGEX", split = ",",
        description = {
            "Regular expressions which specify to ignore matching headers from recording.",
            "E.g.: " + NOT_HEADERS_MATCHING_OPTION + "=(?i)sec-",
            "These regexes will be used in combination with default headers filter.",
            "Check " + IGNORE_DEFAULT_HEADERS_FILTER_OPTION
                + " for more details on default headers filter."})
    private List<String> notHeadersMatching = Collections.emptyList();

    @Option(names = {IGNORE_DEFAULT_HEADERS_FILTER_OPTION},
        description = {"Specifies not to use the default headers filter.",
            "This option might be helpful when you want a to record all headers which require "
                + "specific values by the service under test (eg: User-Agent, Referer, etc).",
            "These are the headers that are ignored by default: ${sys:defaultHeadersFilter}"})
    private boolean ignoreDefaultHeadersFilter;
  }

  @Parameters(paramLabel = "URL", description = {"Initial URL to start the recording",
      "E.g.: https://abstracta.us"}, arity = "0..1")
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
    if (requestsFiltering.ignoreDefaultUrlsFilter) {
      ret.clearUrlsFilter();
    }
    ret.urlsMatching(requestsFiltering.urlsMatching);
    ret.notUrlsMatching(requestsFiltering.notUrlsMatching);
    if (headersFiltering.ignoreDefaultHeadersFilter) {
      ret.clearHeadersFilter();
    }
    ret.notHeadersMatching(headersFiltering.notHeadersMatching);
    return ret.start();
  }

  private ChromeDriverService buildDriverService() {
    ChromeDriverService ret = new Builder()
        // .withVerbose(true)
        .withSilent(true)
        .build();
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

}
