package us.abstracta.jmeter.javadsl.core.listeners;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.report.dashboard.GenerationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SimpleDataWriter;

/**
 * Generates a nice HTML report at the end of test plan execution.
 *
 * @since 0.6
 */
public class HtmlReporter extends BaseListener {

  protected File reportDirectory;
  protected final ApdexThresholds apdexThresholds = new ApdexThresholds();
  protected final Map<String, ApdexThresholds> labelApdexThresholds = new HashMap<>();
  private Duration granularity;

  public HtmlReporter(String reportsDirectoryPath, String name) {
    super("Simple Data Writer", SimpleDataWriter.class);
    File reportsDirectory = new File(reportsDirectoryPath);
    reportsDirectory.mkdirs();
    reportDirectory = reportsDirectory.toPath().resolve(name != null ? name : buildReportName())
        .toFile();
  }

  private static String buildReportName() {
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh-mm-ss")
        .withZone(ZoneId.systemDefault());
    return timeFormatter.format(Instant.now()) + " " + UUID.randomUUID();
  }

  public static class ApdexThresholds {

    public Duration satisfied;
    public Duration tolerated;

    private ApdexThresholds() {
    }

    public ApdexThresholds(Duration satisfied, Duration tolerated) {
      this.satisfied = satisfied;
      this.tolerated = tolerated;
    }

  }

  /**
   * Allows specifying the granularity for time graphs.
   * <p>
   * This is handy if you need to get more or less detail presented in time graphs.
   *
   * @param granularity specifies the granularity to be set. When not specified, the default value
   *                    is 1 minute. Due to <a
   *                    href="https://bz.apache.org/bugzilla/show_bug.cgi?id=60149">existing
   *                    bug</a>, set this value to a duration greater than 1 second to avoid issues
   *                    with TPS graphs.
   * @return the HtmlReporter for further configuration and usage.
   * @since 1.9
   */
  public HtmlReporter timeGraphsGranularity(Duration granularity) {
    this.granularity = granularity;
    return this;
  }

  /**
   * Allows to configure general Apdex thresholds for all requests.
   * <p>
   * Apdex allows to evaluate user satisfaction according to response times. You may find more about
   * this <a href="https://en.wikipedia.org/wiki/Apdex">here</a>.
   *
   * @param satisfiedThreshold specifies the satisfaction threshold. When not specified this value
   *                           defaults to 1500.
   * @param toleratedThreshold specifies the tolerance threshold. When not specified this value
   *                           defaults to 3000.
   * @return the HtmlReporter for further configuration and usage.
   * @since 0.59
   */
  public HtmlReporter apdexThresholds(Duration satisfiedThreshold, Duration toleratedThreshold) {
    this.apdexThresholds.satisfied = satisfiedThreshold;
    this.apdexThresholds.tolerated = toleratedThreshold;
    return this;
  }

  /**
   * Allows to configure a particular sample label Apdex thresholds.
   * <p>
   * You can use it to control the apdex thresholds for a set of samplers or transactions that share
   * same label.
   *
   * @param sampleLabelRegex   regular expression used to match sample labels to apply the
   *                           thresholds to.
   * @param satisfiedThreshold specifies the satisfaction threshold. When not specified, the general
   *                           apdex thresholds for all requests are applied.
   * @param toleratedThreshold specifies the tolerance threshold. When not specified, the general
   *                           apdex thresholds for all requests are applied.
   * @return the HtmlReporter for further configuration and usage.
   * @see #apdexThresholds(Duration, Duration)
   * @since 0.59
   */
  public HtmlReporter transactionApdexThresholds(String sampleLabelRegex,
      Duration satisfiedThreshold, Duration toleratedThreshold) {
    labelApdexThresholds.put(sampleLabelRegex,
        new ApdexThresholds(satisfiedThreshold, toleratedThreshold));
    return this;
  }

  @Override
  public TestElement buildTestElement() {
    if (!reportDirectory.exists()) {
      reportDirectory.mkdirs();
    }
    File resultsFile = new File(reportDirectory, "report.jtl");
    HtmlReportSummariser reporter = new HtmlReportSummariser(resultsFile);
    ResultCollector logger = new AutoFlushingResultCollector(reporter);
    logger.setFilename(resultsFile.getPath());
    return logger;
  }

  private class HtmlReportSummariser extends Summariser {

    private final File resultsFile;
    private final AtomicInteger hostsCount = new AtomicInteger(0);

    private HtmlReportSummariser(File resultsFile) {
      this.resultsFile = resultsFile;
    }

    @Override
    public void testStarted(String host) {
      super.testStarted(host);
      hostsCount.incrementAndGet();
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
      // same as previous method
    }

    @Override
    public void testEnded(String host) {
      // verify that all remote hosts have ended before generating report
      if (hostsCount.decrementAndGet() <= 0) {
        try {
          configureApdexThresholds();
          configureGranularity();
          JMeterUtils.setProperty(JMeter.JMETER_REPORT_OUTPUT_DIR_PROPERTY,
              new File(resultsFile.getParent()).getAbsolutePath());
          new ReportGenerator(resultsFile.getPath(), null).generate();
        } catch (GenerationException | ConfigurationException e) {
          throw new RuntimeException(e);
        }
      }
    }

    private void configureApdexThresholds() {
      if (apdexThresholds.satisfied != null) {
        JMeterUtils.setProperty(ReportGeneratorConfiguration.REPORT_GENERATOR_KEY_PREFIX
            + ".apdex_satisfied_threshold", "" + apdexThresholds.satisfied.toMillis());
      }
      if (apdexThresholds.tolerated != null) {
        JMeterUtils.setProperty(ReportGeneratorConfiguration.REPORT_GENERATOR_KEY_PREFIX
            + ".apdex_tolerated_threshold", "" + apdexThresholds.tolerated.toMillis());
      }
      String transactionsApdex = labelApdexThresholds.entrySet().stream()
          .map(e -> e.getKey() + ":" + e.getValue().satisfied.toMillis() + "|"
              + e.getValue().tolerated.toMillis())
          .collect(Collectors.joining(";"));
      if (!transactionsApdex.isEmpty()) {
        JMeterUtils.setProperty(ReportGeneratorConfiguration.REPORT_GENERATOR_KEY_PREFIX
            + ".apdex_per_transaction", "" + transactionsApdex);
      }
    }

    private void configureGranularity() {
      if (granularity != null) {
        JMeterUtils.setProperty("jmeter.reportgenerator.overall_granularity",
            String.valueOf(granularity.toMillis()));
      }
    }

  }

  /*
   this class is required to assure (even in remote execution) that file is written before
   generating report
   */
  public static class AutoFlushingResultCollector extends ResultCollector {

    public AutoFlushingResultCollector() {
    }

    public AutoFlushingResultCollector(Summariser summer) {
      super(summer);
    }

    @Override
    public void testEnded(String host) {
      flushFile();
      super.testEnded(host);
    }

  }

}
