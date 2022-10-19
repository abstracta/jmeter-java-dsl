package us.abstracta.jmeter.javadsl.core.listeners;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.htmlReporter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class HtmlReporterTest extends JmeterDslTest {

  @Test
  public void shouldWriteHtmlReportWhenHtmlReporterAtTestPlan(@TempDir Path reportDir)
      throws IOException {
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri)),
        buildHtmlReporter(reportDir)).run();
    assertDirectoryContainsReportIndex(reportDir);
  }

  private static void assertDirectoryContainsReportIndex(Path reportDir) {
    assertThat(reportDir.resolve("index.html").toFile().exists()).isTrue();
  }

  private static HtmlReporter buildHtmlReporter(Path reportDir) throws IOException {
    return htmlReporter(reportDir.getParent().toString(), reportDir.getFileName().toString());
  }

  @Test
  public void shouldWriteHtmlReportWhenHtmlReporterWithoutName(@TempDir Path reportDir) throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)),
        htmlReporter(reportDir.toString())).run();
    assertDirectoryContainsReportIndex(findFirstSubDirectory(reportDir));
  }

  private static Path findFirstSubDirectory(Path reportDir) {
    return reportDir.resolve(reportDir.toFile().list()[0]);
  }

  @Test
  public void shouldReportZeroOverallApdexWhenNoRequestsMatchThreshold(@TempDir Path reportDir)
      throws Exception {
    Duration threshold = Duration.ofMillis(10);
    stubFor(any(anyUrl())
        .willReturn(aResponse().withFixedDelay((int) threshold.multipliedBy(2).toMillis())));
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri)),
        buildHtmlReporter(reportDir)
            .apdexThresholds(threshold, threshold))
        .run();
    assertThat(extractApdex(reportDir, "overall")).isEqualTo(0.0);
  }

  private double extractApdex(Path reportDir, String apdexDataLabel) throws IOException {
    Pattern apdexPattern = Pattern.compile(
        "\"#apdexTable\".*?\"" + apdexDataLabel + "\":\\s*\\[?\\{\"data\":\\s*\\[([^,]+)");
    try (BufferedReader reader = new BufferedReader(
        new FileReader(reportDir.resolve("content/js/dashboard.js").toFile()))) {
      String line = reader.readLine();
      while (line != null) {
        Matcher matcher = apdexPattern.matcher(reader.readLine());
        if (matcher.find()) {
          return Double.parseDouble(matcher.group(1));
        }
        line = reader.readLine();
      }
    }
    throw new IllegalStateException("No apdex score found in report!");
  }

  @Test
  public void shouldReportZeroApdexWhenRequestDoesNotMatchTransactionThreshold(
      @TempDir Path reportDir) throws Exception {
    Duration threshold = Duration.ofMillis(10);
    stubFor(any(anyUrl())
        .willReturn(aResponse().withFixedDelay((int) threshold.multipliedBy(2).toMillis())));
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(SAMPLE_1_LABEL, wiremockUri)),
        buildHtmlReporter(reportDir)
            .transactionApdexThresholds(SAMPLE_1_LABEL, threshold, threshold))
        .run();
    assertThat(extractApdex(reportDir, "items")).isEqualTo(0.0);
  }

}
