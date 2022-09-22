package us.abstracta.jmeter.javadsl.core.preprocessors;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class DslJsr223PreProcessorTest extends JmeterDslTest {

  public static final String REQUEST_BODY = "put this in the body";

  @Test
  public void shouldUseBodyGeneratedByPreProcessorWhenTestPlanWithJsr223PreProcessor()
      throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .post("${REQUEST_BODY}", ContentType.TEXT_PLAIN)
                .children(
                    jsr223PreProcessor(
                        "vars.put('REQUEST_BODY', " + getClass().getName() + ".buildRequestBody())")
                )
        )
    ).run();
    verify(postRequestedFor(anyUrl())
        .withRequestBody(equalTo(REQUEST_BODY)));
  }

  @Test
  public void shouldUseBodyGeneratedByPreProcessorWhenTestPlanWithJsr223PreProcessorWithLambdaScript()
      throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .post("${REQUEST_BODY}", ContentType.TEXT_PLAIN)
                .children(
                    jsr223PreProcessor(s -> s.vars.put("REQUEST_BODY", buildRequestBody()))
                )
        )
    ).run();
    verify(postRequestedFor(anyUrl())
        .withRequestBody(equalTo(REQUEST_BODY)));
  }

  public static String buildRequestBody() {
    return REQUEST_BODY;
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithJsr223PreProcessor() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsr223PreProcessor("println 'preRequest'")
                  )

          )
      );
    }

    public DslTestPlan testPlanWithJsr223PreProcessorAndNonDefaultSettings() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .children(
                      jsr223PreProcessor("myScript", "console.log(\"preRequest\")")
                          .language("javascript")
                  )

          )
      );
    }

  }

}
