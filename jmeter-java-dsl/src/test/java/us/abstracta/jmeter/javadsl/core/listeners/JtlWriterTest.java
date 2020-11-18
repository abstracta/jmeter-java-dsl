package us.abstracta.jmeter.javadsl.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class JtlWriterTest extends JmeterDslTest  {

  private static final String RESULTS_JTL = "results.jtl";

  @Test
  public void shouldWriteAllThreadGroupsResultsToFileWhenJtlWriterAtTestPlan(@TempDir Path tempDir)
      throws IOException {
    Path resultsFilePath = tempDir.resolve(RESULTS_JTL);
    JmeterDsl.testPlan(
        threadGroup(1, TEST_ITERATIONS,
            JmeterDsl.httpSampler(wiremockUri),
            JmeterDsl.httpSampler(wiremockUri)
        ),
        threadGroup(1, TEST_ITERATIONS,
            JmeterDsl.httpSampler(wiremockUri)
        ),
        JmeterDsl.jtlWriter(resultsFilePath.toString())
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
    JmeterDsl.testPlan(
        threadGroup(1, TEST_ITERATIONS,
            JmeterDsl.httpSampler(wiremockUri),
            JmeterDsl.httpSampler(wiremockUri),
            JmeterDsl.jtlWriter(resultsFilePath.toString())
        ),
        threadGroup(1, TEST_ITERATIONS,
            JmeterDsl.httpSampler(wiremockUri)
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
            JmeterDsl.httpSampler(wiremockUri)
                .children(
                    JmeterDsl.jtlWriter(resultsFilePath.toString())
                ),
            JmeterDsl.httpSampler(wiremockUri)
        )
    ).run();
    assertResultsFileResultsCount(resultsFilePath, TEST_ITERATIONS);
  }

}
