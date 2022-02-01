package us.abstracta.jmeter.javadsl.core.postprocessors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.regexExtractor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslRegexExtractor.TargetField;

public class DslRegexExtractorTest extends JmeterDslTest {

  public static final String REGEX_PATH = "/regex";
  public static final String USER_BODY_PARAMETER = "user=";
  public static final String USER = "test";
  public static final String USER_QUERY_PARAMETER = "?user=";
  public static final String VARIABLE_NAME = "EXTRACTED_USER";

  @Test
  public void shouldExtractVariableWhenRegexExtractorMatchesResponse() throws Exception {
    stubFor(get(anyUrl()).willReturn(aResponse().withBody(USER_BODY_PARAMETER + USER)));
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + REGEX_PATH)
                .children(
                    regexExtractor(VARIABLE_NAME, USER_BODY_PARAMETER + "(.*)")
                ),
            httpSampler(
                wiremockUri + REGEX_PATH + USER_QUERY_PARAMETER + "${" + VARIABLE_NAME + "}")
        )
    ).run();

    verify(getRequestedFor(urlEqualTo(REGEX_PATH + USER_QUERY_PARAMETER + USER)));
  }

  @Test
  public void shouldExtractVariableWhenRegexExtractorMatchesResponseInHeader() throws Exception {
    stubFor(
        get(anyUrl()).willReturn(aResponse().withHeader("My-Header", USER_BODY_PARAMETER + USER)));
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + REGEX_PATH)
                .children(
                    regexExtractor(VARIABLE_NAME, USER_BODY_PARAMETER + "(.*)")
                        .fieldToCheck(DslRegexExtractor.TargetField.RESPONSE_HEADERS)
                ),
            httpSampler(
                wiremockUri + REGEX_PATH + USER_QUERY_PARAMETER + "${" + VARIABLE_NAME + "}")
        )
    ).run();

    verify(getRequestedFor(urlEqualTo(REGEX_PATH + USER_QUERY_PARAMETER + USER)));
  }

}
