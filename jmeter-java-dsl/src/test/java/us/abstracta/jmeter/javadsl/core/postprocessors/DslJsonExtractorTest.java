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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.testelements.DslScopedTestElement.Scope;

public class DslJsonExtractorTest extends JmeterDslTest {

  @Test
  public void shouldExtractVariableWhenJsonExtractorMatchesResponse() throws Exception {
    String path = "/json";
    String user = "test";
    stubFor(get(anyUrl()).willReturn(aResponse().withBody("[{\"name\":\"" + user + "\"}]}")));
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

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan simpleJsonExtractor() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsonExtractor("EXTRACTED_USER", "[].name")
                  )
          )
      );
    }

    public DslTestPlan completeJsonExtractor() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsonExtractor("EXTRACTED_USER", "[].name")
                          .scope(Scope.ALL_SAMPLES)
                          .matchNumber(0)
                          .defaultValue("NO_MATCH")
                  )
          )
      );
    }

    public DslTestPlan variableScopedJsonExtractor() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsonExtractor("EXTRACTED_USER", "[].name")
                          .scopeVariable("otherVar")
                  )
          )
      );
    }

  }

}
