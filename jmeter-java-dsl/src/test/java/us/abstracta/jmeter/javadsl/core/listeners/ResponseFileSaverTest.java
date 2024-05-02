package us.abstracta.jmeter.javadsl.core.listeners;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.responseFileSaver;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class ResponseFileSaverTest extends JmeterDslTest {

  private static final String RESPONSE_FILE_PREFIX = "response";

  @Test
  public void shouldWriteFileWithResponseContentWhenResponseFileSaverInPlan(@TempDir Path tempDir)
      throws Exception {
    checkGeneratedResponseFile(buildResponseFileSaver(tempDir),
        tempDir.resolve("response1.unknown"));
  }

  private ResponseFileSaver buildResponseFileSaver(Path tempDir) {
    return responseFileSaver(tempDir.resolve(RESPONSE_FILE_PREFIX).toString());
  }

  private void checkGeneratedResponseFile(ResponseFileSaver responseFileSaver, Path filePath)
      throws IOException {
    String body = "TEST BODY";
    stubFor(any(anyUrl()).willReturn(aResponse().withBody(body)));
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
        ),
        responseFileSaver
    ).run();
    assertThat(filePath).hasContent(body);
  }

  @Test
  public void shouldWriteFileWithNoNumberWhenResponseFileSaverWithoutAutoNumber(
      @TempDir Path tempDir) throws Exception {
    checkGeneratedResponseFile(buildResponseFileSaver(tempDir).autoNumber(false),
        tempDir.resolve("response.unknown"));
  }

  @Test
  public void shouldWriteFileWithNoExtensionWhenResponseFileSaverWithoutAutoExtension(
      @TempDir Path tempDir) throws Exception {
    checkGeneratedResponseFile(buildResponseFileSaver(tempDir).autoFileExtension(false),
        tempDir.resolve("response1"));
  }

  @Test
  public void shouldWriteOneFileForEachResponseWhenResponseFileSaverInPlan(@TempDir Path tempDir)
      throws Exception {
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri)
        ),
        buildResponseFileSaver(tempDir)
    ).run();
    String[] responseFiles = tempDir.toFile().list((dir, name) -> name.startsWith(
        RESPONSE_FILE_PREFIX));
    assertThat(responseFiles).hasSize(TEST_ITERATIONS);
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithResponseFileSaver() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              responseFileSaver("response")
          )
      );
    }

    public DslTestPlan testPlanWithResponseFileSaverAndNonDefaultProperties() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              responseFileSaver("response")
                  .autoNumber(false)
                  .autoFileExtension(false)
          )
      );
    }

  }

}
