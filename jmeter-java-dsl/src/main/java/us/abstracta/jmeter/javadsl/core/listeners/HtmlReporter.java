package us.abstracta.jmeter.javadsl.core.listeners;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.dashboard.GenerationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SimpleDataWriter;
import us.abstracta.jmeter.javadsl.core.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.MultiLevelTestElement;

/**
 * Generates a nice HTML report at the end of test plan execution.
 *
 * @since 0.6
 */
public class HtmlReporter extends BaseTestElement implements MultiLevelTestElement {

  private final File reportDirectory;

  public HtmlReporter(String reportPath) throws IOException {
    super("Simple Data Writer", SimpleDataWriter.class);
    reportDirectory = new File(reportPath);
    if (reportDirectory.isFile()) {
      throw new FileAlreadyExistsException(reportPath);
    }
    if (reportDirectory.isDirectory() && !isEmptyDirectory(reportDirectory)) {
      throw new DirectoryNotEmptyException(reportPath);
    }
  }

  private boolean isEmptyDirectory(File reportDirectory) throws IOException {
    return !Files.newDirectoryStream(reportDirectory.toPath()).iterator().hasNext();
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

  private static class HtmlReportSummariser extends Summariser {

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
          JMeterUtils.setProperty(JMeter.JMETER_REPORT_OUTPUT_DIR_PROPERTY,
              new File(resultsFile.getParent()).getAbsolutePath());
          new ReportGenerator(resultsFile.getPath(), null).generate();
        } catch (GenerationException | ConfigurationException e) {
          throw new RuntimeException(e);
        }
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
