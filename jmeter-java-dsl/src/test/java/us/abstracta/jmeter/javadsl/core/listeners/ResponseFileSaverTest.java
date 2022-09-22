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
  public void shouldWriteFileWithResponseContentWhenResponseFileSaverInPlan(@TempDir Path tempDir) throws Exception {
    String body = "TEST BODY";
    stubFor(any(anyUrl()).willReturn(aResponse().withBody(body)));
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
        ),
        responseFileSaver(tempDir.resolve(RESPONSE_FILE_PREFIX).toString())
    ).run();
    assertThat(tempDir.resolve("response1.unknown")).hasContent(body);
  }

  @Test
  public void shouldWriteOneFileForEachResponseWhenResponseFileSaverInPlan(@TempDir Path tempDir) throws Exception {
    testPlan(
        threadGroup(1, TEST_ITERATIONS,
            httpSampler(wiremockUri)
        ),
        responseFileSaver(tempDir.resolve(RESPONSE_FILE_PREFIX).toString())
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

  }

}
