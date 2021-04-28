package us.abstracta.jmeter.javadsl.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static us.abstracta.jmeter.javadsl.JmeterDsl.htmlReporter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jtlWriter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

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
                .post(JSON_BODY, Type.APPLICATION_JSON)
                .children(
                    jtlWriter(resultsFilePath.toString())
                ),
            httpSampler(wiremockUri)
        )
    ).run();
    assertFileMatchesTemplate(resultsFilePath, "/default-jtl.template.csv");
  }

  private void assertFileMatchesTemplate(Path resultsFilePath, String templateName)
      throws IOException, URISyntaxException {
    FileTemplateAssert.assertThat(resultsFilePath)
        .matches(new File(Resources.getResource(getClass(), templateName).toURI()).toPath());
  }

  @Test
  public void shouldWriteAllSampleFieldsWhenJtlWithAllFieldsEnabled(@TempDir Path tempDir)
      throws Exception {
    Path resultsFilePath = tempDir.resolve(RESULTS_JTL);
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .post(JSON_BODY, Type.APPLICATION_JSON)
                .children(
                    jtlWriter(resultsFilePath.toString())
                        .withAllFields(true)
                ),
            httpSampler(wiremockUri)
        )
    ).run();
    assertFileMatchesTemplate(resultsFilePath, "/complete-jtl.template.xml");
  }

}
