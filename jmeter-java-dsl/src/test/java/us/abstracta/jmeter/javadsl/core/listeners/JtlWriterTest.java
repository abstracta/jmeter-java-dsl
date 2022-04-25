package us.abstracta.jmeter.javadsl.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.abstracta.jmeter.javadsl.JmeterDsl.htmlReporter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.util.TestResource;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.StringTemplate.StringTemplateAssert;

public class JtlWriterTest extends JmeterDslTest {

  private static final String RESULTS_JTL = "results.jtl";

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
        jtlWriter(resultsFilePath.toString())
    ).run();
    assertResultsFileResultsCount(resultsFilePath, TEST_ITERATIONS * 3);
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
            jtlWriter(resultsFilePath.toString())
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
                    jtlWriter(resultsFilePath.toString())
                ),
            httpSampler(wiremockUri)
        )
    ).run();
    assertResultsFileResultsCount(resultsFilePath, TEST_ITERATIONS);
  }

  @Test
  public void shouldThrowExceptionWhenCreatingJtlWriterAndFileAlreadyExists(@TempDir Path tempDir) {
    assertThrows(FileAlreadyExistsException.class, () -> {
      Path filePath = tempDir.resolve("test.txt");
      filePath.toFile().createNewFile();
      htmlReporter(filePath.toString());
    });
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
                    jtlWriter(resultsFilePath.toString())
                ),
            httpSampler(wiremockUri)
        )
    ).run();
    assertFileMatchesTemplate(resultsFilePath, "/default-jtl.template.csv");
  }

  private void assertFileMatchesTemplate(Path resultsFilePath, String templateName)
      throws IOException {
    StringTemplateAssert.assertThat(resultsFilePath)
        .matches(new TestResource(templateName));
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
                    jtlWriter(resultsFilePath.toString())
                        .withAllFields(true)
                ),
            httpSampler(wiremockUri)
        )
    ).run();
    assertFileMatchesTemplate(resultsFilePath, "/complete-jtl.template.xml");
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
                    jtlWriter(resultsFilePath.toString()).withVariables("my_var")
                )
        )
    ).run();
    assertFileMatchesTemplate(resultsFilePath, "/jtl-with-custom-variable.template.csv");
  }

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan simpleTestPlanWithJtlWriterAndDefaultSaves() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          ),
          jtlWriter("results.jtl")
      );
    }

    public DslTestPlan simpleTestPlanWithJtlWriterSavingAllFields() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          ),
          jtlWriter("results.jtl")
              .withAllFields(true)
      );
    }

    public DslTestPlan simpleTestPlanWithJtlWriterAndSavingNonDefaultFields() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          ),
          jtlWriter("results.jtl")
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
