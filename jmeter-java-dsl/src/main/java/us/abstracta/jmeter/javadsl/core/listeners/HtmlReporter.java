package us.abstracta.jmeter.javadsl.core.listeners;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
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

  public HtmlReporter(String reportsDirectoryPath, String name) throws IOException {
    super("Simple Data Writer", SimpleDataWriter.class);
    File reportsDirectory = new File(reportsDirectoryPath);
    reportsDirectory.mkdirs();
    reportDirectory = reportsDirectory.toPath()
        .resolve(name != null ? name : new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(
            Instant.now()) + " " + UUID.randomUUID())
        .toFile();
  }

  private boolean isEmptyDirectory(File reportDirectory) throws IOException {
    try (DirectoryStream<Path> dirContentsStream = Files.newDirectoryStream(
        reportDirectory.toPath())) {
      return !dirContentsStream.iterator().hasNext();
    }
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

  @Override
  public TestElement buildTestElement() {
    if (!reportDirectory.exists()) {
      reportDirectory.mkdirs();
    }
    File resultsFile = new File(reportDirectory, "report.jtl");
    HtmlReportSummariser reporter = new HtmlReportSummariser(resultsFile, apdexThresholds,
        labelApdexThresholds);
    ResultCollector logger = new AutoFlushingResultCollector(reporter);
    logger.setFilename(resultsFile.getPath());

    return logger;
  }

  private static class HtmlReportSummariser extends Summariser {

    private final File resultsFile;
    private final ApdexThresholds apdexThresholds;
    private final Map<String, ApdexThresholds> labelApdexThresholds;
    private final AtomicInteger hostsCount = new AtomicInteger(0);

    private HtmlReportSummariser(File resultsFile, ApdexThresholds apdexThresholds,
        Map<String, ApdexThresholds> labelApdexThresholds) {
      this.resultsFile = resultsFile;
      this.apdexThresholds = apdexThresholds;
      this.labelApdexThresholds = labelApdexThresholds;
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

}
