package us.abstracta.jmeter.javadsl.core.controllers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static us.abstracta.jmeter.javadsl.JmeterDsl.forEachController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.ifController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.regexExtractor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class DslForEachControllerTest extends JmeterDslTest {

  @Test
  public void shouldExecuteWithIterationVarsWhenForEachControllerInPlan() throws Exception {
    setupServerResponse();
    String varsPrefix = "numbers";
    String iterationVarName = "numberVar";
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .children(
                    regexExtractor(varsPrefix, "(\\d+)")
                        .matchNumber(-1)
                ),
            forEachController(varsPrefix, iterationVarName,
                httpSampler(wiremockUri + "/${" + iterationVarName + "}")
            )
        )
    ).run();
    verifyRequestMadeForPath("/1");
    verifyRequestMadeForPath("/2");
    verifyRequestMadeForPath("/3");
  }

  private void setupServerResponse() {
    WireMock.stubFor(get(anyUrl()).willReturn(aResponse().withBody("1,2,3")));
  }

  private void verifyRequestMadeForPath(String path) {
    verify(WireMock.getRequestedFor(WireMock.urlPathEqualTo(path)));
  }

  @Test
  public void shouldExposeJMeterVariableWithControllerNameWhenForEachControllerInPlan()
      throws Exception {
    setupServerResponse();
    String loopName = "COUNT";
    String varsPrefix = "numbers";
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .children(
                    regexExtractor(varsPrefix, "(\\d+)")
                        .matchNumber(-1)
                ),
            forEachController(loopName, varsPrefix, "numberVar",
                httpSampler(wiremockUri + "/${__jm__" + loopName + "__idx}")
            )
        )
    ).run();

    verifyRequestMadeForPath("/0");
    verifyRequestMadeForPath("/1");
    verifyRequestMadeForPath("/2");
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan forEachControllerWithoutName() {
      return testPlan(
          threadGroup(1, 1,
              forEachController("items", "item",
                  httpSampler("http://localhost")
              )
          )
      );
    }

    public DslTestPlan forEachControllerWithName() {
      return testPlan(
          threadGroup(1, 1,
              forEachController("loop", "items", "item",
                  httpSampler("http://localhost")
              )
          )
      );
    }

  }

}
