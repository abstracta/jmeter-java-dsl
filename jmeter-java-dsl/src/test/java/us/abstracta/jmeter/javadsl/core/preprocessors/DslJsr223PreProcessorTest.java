package us.abstracta.jmeter.javadsl.core.preprocessors;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

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


}
