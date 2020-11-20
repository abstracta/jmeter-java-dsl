package us.abstracta.jmeter.javadsl.http;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
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

}
