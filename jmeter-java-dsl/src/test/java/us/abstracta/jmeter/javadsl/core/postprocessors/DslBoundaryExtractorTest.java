package us.abstracta.jmeter.javadsl.core.postprocessors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static us.abstracta.jmeter.javadsl.JmeterDsl.boundaryExtractor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslBoundaryExtractorTest extends JmeterDslTest {

  @Test
  public void shouldExtractVariableWhenBoundaryExtractorMatchesResponse() throws Exception {
    String path = "/boundary";
    String userBodyParameter = "user=";
    String user = "test";
    String bodyEnd = "\n";
    wiremockServer
        .stubFor(get(anyUrl()).willReturn(aResponse().withBody(userBodyParameter + user + bodyEnd)));
    String userQueryParameter = "?user=";
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + path)
                .children(
                    boundaryExtractor("EXTRACTED_USER", userBodyParameter, bodyEnd)
                ),
            httpSampler(
                wiremockUri + path + userQueryParameter + "${EXTRACTED_USER}")
        )
    ).run();

    wiremockServer
        .verify(getRequestedFor(urlEqualTo(path + userQueryParameter + user)));
  }

}
