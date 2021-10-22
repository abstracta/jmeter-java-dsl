package us.abstracta.jmeter.javadsl.core;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xmlunit.assertj.XmlAssert;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class JmeterDslCoreTest extends JmeterDslTest {

  private static final String TEST_PLAN_RESOURCE_PATH = "/test-plan.jmx";

  @Test
  public void shouldSendRequestsToServerWhenSimpleHttpTestPlan() throws IOException {
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri)
        )
    ).run();
    verifyRequestsSentToServer(TEST_ITERATIONS);
  }

  private void verifyRequestsSentToServer(int testIterations) {
    wiremockServer.verify(testIterations, getRequestedFor(anyUrl()));
  }

  @Test
  public void shouldTakeExpectedDurationWhenThreadGroupWithConfiguredDuration() throws IOException {
    Duration duration = Duration.ofSeconds(10);
    TestPlanStats stats = testPlan(
        threadGroup(1, duration,
            httpSampler(wiremockUri)
        )
    ).run();
    // we use some threshold in case is not exact due to delays in starting.
    Duration threshold = Duration.ofSeconds(5);
    assertThat(stats.overall().elapsedTime()).isGreaterThan(duration.minus(threshold));
  }

  @Test
  public void shouldTakeAtLeastRampTimesRunningTestWhenThreadGroupWithRampUpAndDown()
      throws IOException {
    Duration duration = Duration.ofSeconds(5);
    Stopwatch time = Stopwatch.createStarted();
    testPlan(
        threadGroup()
            .rampTo(3, duration)
            .rampTo(0, duration)
            .children(httpSampler(wiremockUri))
    ).run();
    assertThat(time.elapsed()).isGreaterThan(duration.multipliedBy(2));
  }

  @Test
  public void shouldSendDoubleRequestsToServerWhenTwoSamplersTestPlan() throws IOException {
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri),
            httpSampler(wiremockUri)
        )
    ).run();
    verifyRequestsSentToServer(TEST_ITERATIONS * 2);
  }

  @Test
  public void shouldGetLabeledAndOverallRequestsCountWhenRunPlanWithMultipleSamplers()
      throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(SAMPLE_1_LABEL, wiremockUri),
            httpSampler(SAMPLE_2_LABEL, wiremockUri)
        )
    ).run();
    assertThat(extractCounts(stats)).isEqualTo(buildExpectedTotalCounts());
  }

  @Test
  public void shouldSaveTestPlanToJmxWhenSave(@TempDir Path tempDir) throws IOException {
    Path filePath = tempDir.resolve("output.jmx");
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler("http://localhost")
                .post(JSON_BODY, Type.APPLICATION_JSON),
            httpSampler("http://localhost")
        ),
        jtlWriter("results.jtl")
    ).saveAsJmx(filePath.toString());
    XmlAssert.assertThat(getFileContents(filePath))
        .and(getResourceContents(TEST_PLAN_RESOURCE_PATH))
        .areIdentical();
  }

  @Test
  public void shouldSendExpectedRequestsWhenLoadPlanFromJmx(@TempDir Path tmpDir)
      throws IOException {
    File jmxFile = tmpDir.resolve("test-plan.jxm").toFile();
    copyJmxWithDynamicFieldsTo(jmxFile);
    DslTestPlan.fromJmx(jmxFile.getPath()).run();
    wiremockServer.verify(postRequestedFor(anyUrl())
        .withHeader(HttpHeader.CONTENT_TYPE.toString(), equalTo(Type.APPLICATION_JSON.asString()))
        .withRequestBody(equalToJson(JSON_BODY)));
  }

  private void copyJmxWithDynamicFieldsTo(File jmxFile) throws IOException {
    String jmxFileContents = getResourceContents(TEST_PLAN_RESOURCE_PATH)
        .replace("http://localhost", wiremockUri)
        .replace("results.jtl", new File(jmxFile.getParent(), "results.jtl").getPath());
    try (FileWriter fw = new FileWriter(jmxFile)) {
      fw.write(jmxFileContents);
    }
  }

}
