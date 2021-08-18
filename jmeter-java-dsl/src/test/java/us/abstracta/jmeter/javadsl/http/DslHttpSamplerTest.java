package us.abstracta.jmeter.javadsl.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCache;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCookies;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpHeaders;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.apache.jmeter.threads.JMeterVariables;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslHttpSamplerTest extends JmeterDslTest {

  private static final String HEADER_NAME_1 = "name1";
  private static final String HEADER_VALUE_1 = "value1";
  private static final String HEADER_NAME_2 = "name2";
  private static final String HEADER_VALUE_2 = "value2";
  private static final String REDIRECT_PATH = "/redirect";

  @Test
  public void shouldSendPostWithContentTypeToServerWhenHttpSamplerWithPost() throws Exception {
    Type contentType = Type.APPLICATION_JSON;
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri).post(JSON_BODY, contentType)
        )
    ).run();
    wiremockServer.verify(postRequestedFor(anyUrl())
        .withHeader(HttpHeader.CONTENT_TYPE.toString(), equalTo(contentType.toString()))
        .withRequestBody(equalToJson(JSON_BODY)));
  }

  @Test
  public void shouldFollowRedirectsByDefaultWhenHttpSampler() throws Exception {
    setupMockedRedirectionTo(REDIRECT_PATH);
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
        )
    ).run();
    wiremockServer.verify(getRequestedFor(urlPathEqualTo(REDIRECT_PATH)));
  }

  private void setupMockedRedirectionTo(String redirectPath) {
    wiremockServer.stubFor(get("/").willReturn(
        aResponse().withStatus(HttpStatus.MOVED_PERMANENTLY_301)
            .withHeader("Location", wiremockUri + redirectPath)));
  }

  @Test
  public void shouldNotFollowRedirectsWhenHttpSamplerWithDisabledFollowRedirects() throws Exception {
    setupMockedRedirectionTo(REDIRECT_PATH);
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .followRedirects(false)
        )
    ).run();
    wiremockServer.verify(0, getRequestedFor(urlPathEqualTo(REDIRECT_PATH)));
  }

  @Test
  public void shouldSendHeadersWhenHttpSamplerWithHeaders() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
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
            httpSampler(wiremockUri)
                .method(HttpMethod.POST)
                .children(
                    httpHeaders()
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
            httpHeaders()
                .header(HEADER_NAME_1, HEADER_VALUE_1)
                .header(HEADER_NAME_2, HEADER_VALUE_2),
            httpSampler(wiremockUri)
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

  @Test
  public void shouldKeepCookiesWhenMultipleRequests() throws Exception {
    setupHttpResponseWithCookie();
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri),
            httpSampler(wiremockUri)
        )
    ).run();
    wiremockServer.verify(getRequestedFor(anyUrl()).withHeader("Cookie", equalTo("MyCookie=val")));
  }

  private void setupHttpResponseWithCookie() {
    wiremockServer.stubFor(get(anyUrl())
        .willReturn(aResponse().withHeader("Set-Cookie", "MyCookie=val")));
  }

  @Test
  public void shouldResetCookiesBetweenIterationsByDefault() throws Exception {
    setupHttpResponseWithCookie();
    testPlan(
        threadGroup(1, 2,
            httpSampler(wiremockUri)
        )
    ).run();
    wiremockServer.verify(getRequestedFor(anyUrl()).withoutHeader("Cookie"));
  }

  @Test
  public void shouldUseCachedResponseWhenSameRequestAndCacheableResponse() throws Exception {
    testPlan(
        buildHeadersToFixHttpCaching(),
        threadGroup(1, 1,
            httpSampler(wiremockUri),
            httpSampler(wiremockUri)
        )
    ).run();
    wiremockServer.verify(1, getRequestedFor(anyUrl()));
  }

  /*
   need to set header for request header to match otherwise jmeter automatically adds this
   header while sending request and stores it in cache and when it checks in next request
   it doesn't match since same header is not yet set at check time,
   */
  private HttpHeaders buildHeadersToFixHttpCaching() {
    return httpHeaders().header("User-Agent", "jmeter-java-dsl");
  }

  private void setupCacheableHttpResponse() {
    wiremockServer.stubFor(get(anyUrl())
        .willReturn(aResponse().withHeader("Vary", "max-age=600")));
  }

  @Test
  public void shouldResetCacheBetweenIterationsByDefault() throws Exception {
    setupCacheableHttpResponse();
    testPlan(
        buildHeadersToFixHttpCaching(),
        threadGroup(1, 2,
            httpSampler(wiremockUri)
        )
    ).run();
    wiremockServer.verify(2, getRequestedFor(anyUrl()));
  }

  @Test
  public void shouldNotKeepCookiesWhenDisabled() throws Exception {
    setupHttpResponseWithCookie();
    testPlan(
        httpCookies().disable(),
        threadGroup(1, 1,
            httpSampler(wiremockUri),
            httpSampler(wiremockUri)
        )
    ).run();
    wiremockServer.verify(2, getRequestedFor(anyUrl()).withoutHeader("Cookie"));
  }

  @Test
  public void shouldNotUseCacheWhenDisabled() throws Exception {
    wiremockServer.stubFor(get(anyUrl())
        .willReturn(aResponse().withHeader("Set-Cookie", "MyCookie=val")));
    testPlan(
        httpCache().disable(),
        threadGroup(1, 1,
            httpSampler(wiremockUri),
            httpSampler(wiremockUri)
        )
    ).run();
    wiremockServer.verify(2, getRequestedFor(anyUrl()));
  }

}
