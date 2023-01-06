package us.abstracta.jmeter.javadsl.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.StringTemplateAssert;
import us.abstracta.jmeter.javadsl.core.listeners.JtlWriter.SampleStatus;

public class JtlWriterTest extends JmeterDslTest {

  private static final String RESULTS_JTL = "results.jtl";

  @Test
  public void shouldWriteResultsToFileWhenJtlWriterWithoutNameAtTestPlan(@TempDir Path tempDir) throws Exception {
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri)
        ),
        jtlWriter(tempDir.toString())
    ).run();
    assertResultsFileResultsCount(findJtlFileInDirectory(tempDir), TEST_ITERATIONS);
  }

  private Path findJtlFileInDirectory(Path tempDir) {
    return tempDir.resolve(tempDir.toFile().list((dir, name) -> name.endsWith(".jtl"))[0]);
  }

  @Test
  public void shouldWriteAllThreadGroupsResultsToFileWhenJtlWriterAtTestPlan(@TempDir Path tempDir)
      throws IOException {
    Path resultsFilePath = tempDir.resolve(RESULTS_JTL);
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri),
            httpSampler(wiremockUri)
        ),
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri)
        ),
        buildJtlWriter(resultsFilePath)
    ).run();
    assertResultsFileResultsCount(resultsFilePath, TEST_ITERATIONS * 3);
  }

  private static JtlWriter buildJtlWriter(Path resultsFilePath) {
    return jtlWriter(resultsFilePath.getParent().toString(),
        resultsFilePath.getFileName().toString());
  }

  private void assertResultsFileResultsCount(Path resultsFilePath, int resultsCount)
      throws IOException {
    // we add one more expected line due to headers line
    assertThat(Files.readAllLines(resultsFilePath).size()).isEqualTo(resultsCount + 1);
  }

  @Test
  public void shouldWriteContainingThreadGroupResultsToFileWhenJtlWriterAtThreadGroup(
      @TempDir Path tempDir) throws IOException {
    Path resultsFilePath = tempDir.resolve(RESULTS_JTL);
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri),
            httpSampler(wiremockUri),
            buildJtlWriter(resultsFilePath)
        ),
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri)
        )
    ).run();
    assertResultsFileResultsCount(resultsFilePath, TEST_ITERATIONS * 2);
  }

  @Test
  public void shouldWriteContainingSamplerResultsToFileWhenJtlWriterAtSampler(
      @TempDir Path tempDir) throws IOException {
    Path resultsFilePath = tempDir.resolve(RESULTS_JTL);
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri)
                .children(
                    buildJtlWriter(resultsFilePath)
                ),
            httpSampler(wiremockUri)
        )
    ).run();
    assertResultsFileResultsCount(resultsFilePath, TEST_ITERATIONS);
  }

  @Test
  public void shouldWriteDefaultSampleFieldsWhenJtlWithDefaultSettings(@TempDir Path tempDir)
      throws Exception {
    Path resultsFilePath = tempDir.resolve(RESULTS_JTL);
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .post(JSON_BODY, ContentType.APPLICATION_JSON)
                .children(
                    buildJtlWriter(resultsFilePath)
                ),
            httpSampler(wiremockUri)
        )
    ).run();
    assertFileMatchesTemplate(resultsFilePath, "jtls/default-jtl.template.csv");
  }

  private void assertFileMatchesTemplate(Path resultsFilePath, String templateName)
      throws IOException {
    StringTemplateAssert.assertThat(resultsFilePath)
        .matches(testResource(templateName));
  }

  @Test
  public void shouldWriteAllSampleFieldsWhenJtlWithAllFieldsEnabled(@TempDir Path tempDir)
      throws Exception {
    Path resultsFilePath = tempDir.resolve(RESULTS_JTL);
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .post(JSON_BODY, ContentType.APPLICATION_JSON)
                .children(
                    buildJtlWriter(resultsFilePath)
                        .withAllFields(true)
                ),
            httpSampler(wiremockUri)
        )
    ).run();
    assertFileMatchesTemplate(resultsFilePath, "jtls/complete-jtl.template.xml");
  }

  @Test
  public void shouldJtlIncludeCustomVariableWhenJtlWithCustomVariableDefined(@TempDir Path tempDir)
      throws Exception {
    Path resultsFilePath = tempDir.resolve(RESULTS_JTL);
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .children(
                    jsr223PostProcessor("vars.put('my_var', 'my_val')"),
                    buildJtlWriter(resultsFilePath)
                        .withVariables("my_var")
                )
        )
    ).run();
    assertFileMatchesTemplate(resultsFilePath, "jtls/jtl-with-custom-variable.template.csv");
  }

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithJtlWriterAndDefaultSaves() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          ),
          jtlWriter("", "results.jtl")
      );
    }

    public DslTestPlan testPlanWithJtlWriterOnlyLoggingSuccessSampleResults() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          ),
          jtlWriter("", "results.jtl")
              .logOnly(SampleStatus.SUCCESS)
      );
    }

    public DslTestPlan testPlanWithJtlWriterOnlyLoggingErrorSampleResults() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          ),
          jtlWriter("", "results.jtl")
              .logOnly(SampleStatus.ERROR)
      );
    }

    public DslTestPlan testPlanWithJtlWriterSavingAllFields() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          ),
          jtlWriter("target", "results.jtl")
              .withAllFields()
      );
    }

    public DslTestPlan testPlanWithJtlWriterAndSavingNonDefaultFields() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          ),
          jtlWriter("", "results.jtl")
              .saveAsXml(true)
              .withElapsedTime(false)
              .withResponseMessage(false)
              .withSuccess(false)
              .withSentByteCount(false)
              .withResponseFilename(true)
              .withEncoding(true)
              .withIdleTime(false)
              .withResponseHeaders(true)
              .withAssertionResults(false)
              .withFieldNames(false)
              .withLabel(false)
              .withThreadName(false)
              .withAssertionFailureMessage(false)
              .withActiveThreadCounts(false)
              .withLatency(false)
              .withSampleAndErrorCounts(true)
              .withRequestHeaders(true)
              .withResponseData(true)
              .withTimeStamp(false)
              .withResponseCode(false)
              .withDataType(false)
              .withReceivedByteCount(false)
              .withUrl(false)
              .withConnectTime(false)
              .withHostname(true)
              .withSamplerData(true)
              .withSubResults(false)
      );
    }

  }

}
