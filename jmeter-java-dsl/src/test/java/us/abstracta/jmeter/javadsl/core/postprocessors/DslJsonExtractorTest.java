package us.abstracta.jmeter.javadsl.core.postprocessors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsonExtractor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslJsonExtractorTest extends JmeterDslTest {

  @Test
  public void shouldExtractVariableWhenJsonExtractorMatchesResponse() throws Exception {
    String path = "/json";
    String user = "test";
    stubFor(get(anyUrl()).willReturn(aResponse().withBody("[{\"name\":\""+user+"\"}]}")));
    String userQueryParameter = "?user=";
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + path)
                .children(
                    jsonExtractor("EXTRACTED_USER", "[].name")
                ),
            httpSampler(
                wiremockUri + path + userQueryParameter + "${EXTRACTED_USER}")
        )
    ).run();

    verify(getRequestedFor(urlEqualTo(path + userQueryParameter + user)));
  }

}
