package us.abstracta.jmeter.javadsl.core.preprocessors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslJsr223PreProcessorTest extends JmeterDslTest {

  public static final String REQUEST_BODY = "put this in the body";

  @Test
  public void shouldUseBodyGeneratedByPreProcessorWhenTestPlanWithJsr223PreProcessor()
      throws Exception {
    testPlan(
        threadGroup(1, 1,
            JmeterDsl.
                httpSampler(wiremockUri)
                .post("${REQUEST_BODY}", MimeTypes.Type.TEXT_PLAIN)
                .children(
                    jsr223PreProcessor(
                        "vars.put('REQUEST_BODY', " + getClass().getName() + ".buildRequestBody())")
                )
        )
    ).run();
    wiremockServer.verify(postRequestedFor(anyUrl())
        .withRequestBody(equalTo(REQUEST_BODY)));
  }

  public static String buildRequestBody() {
    return REQUEST_BODY;
  }

  @Test
  public void shouldUseBodyGeneratedByPreProcessorWhenTestPlanWithJsr223PreProcessorWithLambdaScript()
      throws Exception {
    testPlan(
        threadGroup(1, 1,
            JmeterDsl.
                httpSampler(wiremockUri)
                .post("${REQUEST_BODY}", MimeTypes.Type.TEXT_PLAIN)
                .children(
                    jsr223PreProcessor(s -> s.vars.put("REQUEST_BODY", buildRequestBody()))
                )
        )
    ).run();
    wiremockServer.verify(postRequestedFor(anyUrl())
        .withRequestBody(equalTo(REQUEST_BODY)));
  }

  public static int bodyCount = 1;

    @Test
    public void shouldUseBodyAndHeaderGeneratedFunctionalRequest()
            throws Exception {
        testPlan(
                threadGroup(1, 2,
                        JmeterDsl.
                                httpSampler(wiremockUri)
                                .header("Header1", s -> "Value" + bodyCount)
                                .header("Header2", s -> "Value" + bodyCount)
                                .post(s -> "Body" + bodyCount, MimeTypes.Type.TEXT_PLAIN)
                        .children(jsr223PostProcessor(s -> bodyCount++))
                )
        ).run();
        wiremockServer.verify(postRequestedFor(anyUrl())
                .withHeader("Header1", equalTo("Value1"))
                .withHeader("Header2", equalTo("Value1"))
                .withRequestBody(equalTo("Body1")));
        wiremockServer.verify(postRequestedFor(anyUrl())
                .withHeader("Header1", equalTo("Value2"))
                .withHeader("Header2", equalTo("Value2"))
                .withRequestBody(equalTo("Body2")));
    }

    public static int urlCount = 1;

    @Test
    public void shouldUseUrlGeneratedFunctionalRequest()
            throws Exception {
        testPlan(
                threadGroup(1, 2,
                        JmeterDsl.
                                httpSampler(s -> wiremockUri + "/" + urlCount)
                                .method(HttpMethod.GET)
                                .children(jsr223PostProcessor(s -> urlCount++))
                )
        ).run();
        wiremockServer.verify(getRequestedFor(urlPathEqualTo("/1")));
        wiremockServer.verify(getRequestedFor(urlPathEqualTo("/2")));
    }

}
