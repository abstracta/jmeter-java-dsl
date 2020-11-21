package us.abstracta.jmeter.javadsl.core.postprocessors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.regexExtractor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslRegexExtractorTest extends JmeterDslTest {

  @Test
  public void shouldReportNoFailureWhenJsr223PostProcessorModifiesFailedRequest() throws Exception {
    String regexTestEndpoint = "/regex";
    String userBodyParameter = "user=";
    String user = "test";
    wiremockServer
        .stubFor(get(anyUrl()).willReturn(aResponse().withBody(userBodyParameter + user)));
    String userQueryParameter = "?user=";
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + regexTestEndpoint)
                .children(
                    regexExtractor("EXTRACTED_USER", userBodyParameter + "(.*)")
                ),
            httpSampler(
                wiremockUri + regexTestEndpoint + userQueryParameter + "${EXTRACTED_USER}")
        )
    ).run();

    wiremockServer
        .verify(getRequestedFor(urlEqualTo(regexTestEndpoint + userQueryParameter + user)));
  }

}
