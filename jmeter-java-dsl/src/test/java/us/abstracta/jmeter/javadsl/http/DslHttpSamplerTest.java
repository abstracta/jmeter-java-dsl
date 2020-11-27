package us.abstracta.jmeter.javadsl.http;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.apache.jmeter.threads.JMeterVariables;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslHttpSamplerTest extends JmeterDslTest {

  private static final String HEADER_NAME_1 = "name1";
  private static final String HEADER_VALUE_1 = "value1";
  private static final String HEADER_NAME_2 = "name2";
  private static final String HEADER_VALUE_2 = "value2";

  @Test
  public void shouldSendPostWithContentTypeToServerWhenHttpSamplerWithPost() throws Exception {
    Type contentType = Type.APPLICATION_JSON;
    testPlan(
        threadGroup(1, 1,
            JmeterDsl.httpSampler(wiremockUri).post(JSON_BODY, contentType)
        )
    ).run();
    wiremockServer.verify(postRequestedFor(anyUrl())
        .withHeader(HttpHeader.CONTENT_TYPE.toString(), equalTo(contentType.toString()))
        .withRequestBody(equalToJson(JSON_BODY)));
  }

  @Test
  public void shouldSendHeadersWhenHttpSamplerWithHeaders() throws Exception {
    testPlan(
        threadGroup(1, 1,
            JmeterDsl.httpSampler(wiremockUri)
                .method(HttpMethod.POST)
                .header(HEADER_NAME_1, HEADER_VALUE_1)
                .header(HEADER_NAME_2, HEADER_VALUE_2)
        )
    ).run();
    verifyHeadersSentToServer();
  }

  private void verifyHeadersSentToServer() {
    wiremockServer.verify(postRequestedFor(anyUrl())
        .withHeader(HEADER_NAME_1, equalTo(HEADER_VALUE_1))
        .withHeader(HEADER_NAME_2, equalTo(HEADER_VALUE_2)));
  }

  @Test
  public void shouldSendHeadersWhenHttpSamplerWithChildHeaders() throws Exception {
    testPlan(
        threadGroup(1, 1,
            JmeterDsl.httpSampler(wiremockUri)
                .method(HttpMethod.POST)
                .children(
                    JmeterDsl.httpHeaders()
                        .header(HEADER_NAME_1, HEADER_VALUE_1)
                        .header(HEADER_NAME_2, HEADER_VALUE_2)
                )
        )
    ).run();
    verifyHeadersSentToServer();
  }

  @Test
  public void shouldSendHeadersWhenHttpSamplerAndHeadersAtThreadGroup() throws Exception {
    testPlan(
        threadGroup(1, 1,
            JmeterDsl.httpHeaders()
                .header(HEADER_NAME_1, HEADER_VALUE_1)
                .header(HEADER_NAME_2, HEADER_VALUE_2),
            JmeterDsl.httpSampler(wiremockUri)
                .method(HttpMethod.POST)
        )
    ).run();
    verifyHeadersSentToServer();
  }

  @Test
  public void shouldUseGeneratedBodyAndHeaderWhenRequestWithHeaderAndBodySuppliers()
      throws Exception {
    String headerValuePrefix = "value";
    String bodyPrefix = "body";
    String countVarName = "REQUEST_COUNT";
    testPlan(
        threadGroup(1, 2,
            JmeterDsl.
                httpSampler(wiremockUri)
                .children(jsr223PreProcessor(s -> incrementVar(countVarName, s.vars)))
                .header(HEADER_NAME_1, s -> headerValuePrefix + s.vars.getObject(countVarName))
                .header(HEADER_NAME_2, s -> headerValuePrefix + s.vars.getObject(countVarName))
                .post(s -> bodyPrefix + s.vars.getObject(countVarName), MimeTypes.Type.TEXT_PLAIN)
        )
    ).run();
    verifyDynamicRequestWithPrefixesAndCount(headerValuePrefix, bodyPrefix, 1);
    verifyDynamicRequestWithPrefixesAndCount(headerValuePrefix, bodyPrefix, 2);
  }

  private void incrementVar(String varName, JMeterVariables vars) {
    Integer countVarVal = (Integer) vars.getObject(varName);
    vars.putObject(varName, countVarVal != null ? countVarVal + 1 : 1);
  }

  private void verifyDynamicRequestWithPrefixesAndCount(String headerValuePrefix, String bodyPrefix,
      int count) {
    wiremockServer.verify(postRequestedFor(anyUrl())
        .withHeader(HEADER_NAME_1, equalTo(headerValuePrefix + count))
        .withHeader(HEADER_NAME_2, equalTo(headerValuePrefix + count))
        .withRequestBody(equalTo(bodyPrefix + count)));
  }

  @Test
  public void shouldUseUrlGeneratedFunctionalRequest()
      throws Exception {
    testPlan(
        threadGroup(1, 2,
            JmeterDsl.
                httpSampler(s -> {
                  String countVarName = "REQUEST_COUNT";
                  incrementVar(countVarName, s.vars);
                  return wiremockUri + "/" + s.vars.getObject(countVarName);
                })
        )
    ).run();
    wiremockServer.verify(getRequestedFor(urlPathEqualTo("/" + 1)));
    wiremockServer.verify(getRequestedFor(urlPathEqualTo("/" + 2)));
  }

}
