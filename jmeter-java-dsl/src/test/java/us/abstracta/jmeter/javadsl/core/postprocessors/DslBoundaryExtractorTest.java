package us.abstracta.jmeter.javadsl.core.postprocessors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static us.abstracta.jmeter.javadsl.JmeterDsl.boundaryExtractor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslBoundaryExtractor.TargetField;
import us.abstracta.jmeter.javadsl.core.testelements.DslScopedTestElement.Scope;

public class DslBoundaryExtractorTest extends JmeterDslTest {

  public static final String BOUNDARY_PATH = "/boundary";
  public static final String USER_BODY_PARAMETER = "user=";
  public static final String USER = "test";
  public static final String USER_QUERY_PARAMETER = "?user=";
  public static final String VARIABLE_NAME = "EXTRACTED_USER";

  @Test
  public void shouldExtractVariableWhenBoundaryExtractorMatchesResponse() throws Exception {
    String bodyEnd = "\n";
    stubFor(get(anyUrl()).willReturn(aResponse().withBody(USER_BODY_PARAMETER + USER + bodyEnd)));
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + BOUNDARY_PATH)
                .children(
                    boundaryExtractor(VARIABLE_NAME, USER_BODY_PARAMETER, bodyEnd)
                ),
            httpSampler(
                wiremockUri + BOUNDARY_PATH + USER_QUERY_PARAMETER + "${" + VARIABLE_NAME + "}")
        )
    ).run();

    verify(getRequestedFor(urlEqualTo(BOUNDARY_PATH + USER_QUERY_PARAMETER + USER)));
  }

  @Test
  public void shouldExtractVariableWhenBoundaryExtractorMatchesResponseInHeader() throws Exception {
    stubFor(get(anyUrl()).willReturn(
        aResponse().withHeader("My-Header", USER_BODY_PARAMETER + USER)));
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + BOUNDARY_PATH)
                .children(
                    boundaryExtractor(VARIABLE_NAME, USER_BODY_PARAMETER, "\n")
                        .fieldToCheck(TargetField.RESPONSE_HEADERS)
                ),
            httpSampler(
                wiremockUri + BOUNDARY_PATH + USER_QUERY_PARAMETER + "${" + VARIABLE_NAME + "}")
        )
    ).run();
    verify(getRequestedFor(urlEqualTo(BOUNDARY_PATH + USER_QUERY_PARAMETER + USER)));
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan simpleBoundaryExtractor() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      boundaryExtractor("EXTRACTED_USER", "user=", "&")
                  )
          )
      );
    }

    public DslTestPlan completeBoundaryExtractor() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      boundaryExtractor("EXTRACTED_USER", "user=", "&")
                          .scope(Scope.ALL_SAMPLES)
                          .fieldToCheck(TargetField.REQUEST_HEADERS)
                          .matchNumber(0)
                          .defaultValue("NO_MATCH")
                  )
          )
      );
    }

    public DslTestPlan variableScopedBoundaryExtractor() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      boundaryExtractor("EXTRACTED_USER", "user=", "&")
                          .scopeVariable("otherVar")
                  )
          )
      );
    }

  }

}
