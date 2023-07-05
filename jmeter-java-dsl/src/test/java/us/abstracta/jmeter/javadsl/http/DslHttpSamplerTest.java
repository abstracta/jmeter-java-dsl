package us.abstracta.jmeter.javadsl.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCache;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCookies;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpHeaders;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.transaction;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Pattern;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler.HttpClientImpl;
import us.abstracta.jmeter.javadsl.util.TestResource;

public class DslHttpSamplerTest extends JmeterDslTest {

  private static final String HEADER_NAME_1 = "name1";
  private static final String HEADER_VALUE_1 = "value1";
  private static final String HEADER_NAME_2 = "name2";
  private static final String HEADER_VALUE_2 = "value2";
  private static final String REDIRECT_PATH = "/redirect";
  private static final String PARAM1_NAME = "par+am1";
  private static final String PARAM1_VALUE = "MY+VALUE";
  private static final String PARAM2_NAME = "par+am2";
  private static final String PARAM2_VALUE = "OTHER+VALUE";
  private static final String MULTIPART_BOUNDARY_PATTERN = "[\\w-]+";
  private static final String CRLN = "\r\n";

  @Test
  public void shouldSendPostWithContentTypeToServerWhenHttpSamplerWithPost() throws Exception {
    ContentType contentType = ContentType.APPLICATION_JSON;
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri).post(JSON_BODY, contentType)
        )
    ).run();
    verify(postRequestedFor(anyUrl())
        .withHeader(HTTPConstants.HEADER_CONTENT_TYPE, equalTo(contentType.toString()))
        .withRequestBody(equalToJson(JSON_BODY)));
  }

  @Test
  public void shouldSendGetWithHostAndProtocolWhenHttpSampler() throws Exception {
    int serverPort = wiremockServer.getHttpPort();
    testPlan(
        threadGroup(1, 1,
            httpSampler("").protocol("http").host("localhost").port(serverPort)
        )
    ).run();
    verify(getRequestedFor(anyUrl()).withPort(serverPort));
  }

  @Test
  public void shouldFollowRedirectsByDefaultWhenHttpSampler() throws Exception {
    setupMockedRedirectionTo(REDIRECT_PATH);
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo(REDIRECT_PATH)));
  }

  private void setupMockedRedirectionTo(String redirectPath) {
    stubFor(get("/").willReturn(
        aResponse().withStatus(HttpStatus.SC_MOVED_PERMANENTLY)
            .withHeader("Location", wiremockUri + redirectPath)));
  }

  @Test
  public void shouldNotFollowRedirectsWhenHttpSamplerWithDisabledFollowRedirects()
      throws Exception {
    setupMockedRedirectionTo(REDIRECT_PATH);
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .followRedirects(false)
        )
    ).run();
    verify(0, getRequestedFor(urlPathEqualTo(REDIRECT_PATH)));
  }

  @Test
  public void shouldSendHeadersWhenHttpSamplerWithHeaders() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .method(HTTPConstants.POST)
                .header(HEADER_NAME_1, HEADER_VALUE_1)
                .header(HEADER_NAME_2, HEADER_VALUE_2)
        )
    ).run();
    verifyHeadersSentToServer();
  }

  private void verifyHeadersSentToServer() {
    verify(postRequestedFor(anyUrl())
        .withHeader(HEADER_NAME_1, equalTo(HEADER_VALUE_1))
        .withHeader(HEADER_NAME_2, equalTo(HEADER_VALUE_2)));
  }

  @Test
  public void shouldSendHeadersWhenHttpSamplerWithChildHeaders() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .method(HTTPConstants.POST)
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
                .method(HTTPConstants.POST)
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
                .post(s -> bodyPrefix + s.vars.getObject(countVarName), ContentType.TEXT_PLAIN)
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
    verify(postRequestedFor(anyUrl())
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
    verify(getRequestedFor(urlPathEqualTo("/" + 1)));
    verify(getRequestedFor(urlPathEqualTo("/" + 2)));
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
    verify(getRequestedFor(anyUrl()).withHeader("Cookie", equalTo("MyCookie=val")));
  }

  private void setupHttpResponseWithCookie() {
    stubFor(get(anyUrl())
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
    verify(getRequestedFor(anyUrl()).withoutHeader("Cookie"));
  }

  @Test
  public void shouldUseCachedResponseWhenSameRequestAndCacheableResponse() throws Exception {
    setupCacheableHttpResponse();
    testPlan(
        buildHeadersToFixHttpCaching(),
        threadGroup(1, 1,
            httpSampler(wiremockUri),
            httpSampler(wiremockUri)
        )
    ).run();
    verify(1, getRequestedFor(anyUrl()));
  }

  private void setupCacheableHttpResponse() {
    stubFor(get(anyUrl())
        .willReturn(aResponse().withHeader("Cache-Control", "max-age=600")));
  }

  /*
   need to set header for request header to match otherwise jmeter automatically adds this
   header while sending request and stores it in cache and when it checks in next request
   it doesn't match since same header is not yet set at check time.
   */
  private HttpHeaders buildHeadersToFixHttpCaching() {
    return httpHeaders().header("User-Agent", "jmeter-java-dsl");
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
    verify(2, getRequestedFor(anyUrl()));
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
    verify(2, getRequestedFor(anyUrl()).withoutHeader("Cookie"));
  }

  @Test
  public void shouldNotUseCacheWhenDisabled() throws Exception {
    setupCacheableHttpResponse();
    testPlan(
        buildHeadersToFixHttpCaching(),
        httpCache().disable(),
        threadGroup(1, 1,
            httpSampler(wiremockUri),
            httpSampler(wiremockUri)
        )
    ).run();
    verify(2, getRequestedFor(anyUrl()));
  }

  @Test
  public void shouldDownloadEmbeddedResourceWhenEnabled() throws Exception {
    String primaryUrl = "/primary";
    String resourceUrl = "/resource";
    stubFor(get(primaryUrl)
        .willReturn(HttpResponseBuilder.buildEmbeddedResourcesResponse(resourceUrl)));
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + primaryUrl)
                .downloadEmbeddedResources()
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo(resourceUrl)));
  }

  @Test
  public void shouldDownloadEmbeddedResourcesInParallelWhenEnabled() throws Exception {
    int responsesDelayMillis = 3000;
    stubFor(get(anyUrl())
        .willReturn(aResponse().withFixedDelay(responsesDelayMillis)));
    String primaryUrl = "/primary";
    stubFor(get(primaryUrl)
        .willReturn(HttpResponseBuilder.buildEmbeddedResourcesResponse("/resource1", "/resource2")
            .withFixedDelay(responsesDelayMillis)));
    String transactionLabel = "sample";
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            transaction(transactionLabel,
                httpSampler(wiremockUri + primaryUrl)
                    .downloadEmbeddedResources()
            )
        )
    ).run();
    assertThat(stats.byLabel(transactionLabel).sampleTime().max()).isLessThan(
        Duration.ofMillis(responsesDelayMillis * 3));
  }

  @Test
  public void shouldNotDownloadExcludedEmbeddedResourceWhenEnabled() throws Exception {
    String primaryUrl = "/primary";
    String resource1Url = "/resource1";
    String resource2Url = "/resource2";
    stubFor(get(primaryUrl)
        .willReturn(
            HttpResponseBuilder.buildEmbeddedResourcesResponse(resource1Url, resource2Url)));
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + primaryUrl)
                .downloadEmbeddedResourcesNotMatching(".*/resource2.*")
        )
    ).run();
    verify(exactly(0), getRequestedFor(urlPathEqualTo(resource2Url)));
    verify(getRequestedFor(urlPathEqualTo(resource1Url)));
  }

  @Test
  public void shouldDownloadOnlyMatchingEmbeddedResourceWhenEnabled() throws Exception {
    String primaryUrl = "/primary";
    String resource1Url = "/resource1";
    String resource2Url = "/resource2";
    stubFor(get(primaryUrl)
        .willReturn(
            HttpResponseBuilder.buildEmbeddedResourcesResponse(resource1Url, resource2Url)));
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + primaryUrl)
                .downloadEmbeddedResourcesMatching(".*/resource1.*")
        )
    ).run();
    verify(exactly(0), getRequestedFor(urlPathEqualTo(resource2Url)));
    verify(getRequestedFor(urlPathEqualTo(resource1Url)));
  }

  @Test
  public void shouldSendQueryParametersWhenGetRequestWithParameters() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .param(PARAM1_NAME, PARAM1_VALUE)
                .rawParam(PARAM2_NAME, PARAM2_VALUE)
        )
    ).run();
    verify(getRequestedFor(
        urlEqualTo("/?" + buildUrlEncodedParamsQuery())));
  }

  private String buildUrlEncodedParamsQuery() {
    return urlEncode(PARAM1_NAME) + "=" + urlEncode(PARAM1_VALUE) + "&" + PARAM2_NAME + "="
        + PARAM2_VALUE;
  }

  private String urlEncode(String val) {
    try {
      return URLEncoder.encode(val, StandardCharsets.ISO_8859_1.toString());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void shouldSendUrlEncodedFormWhenPostRequestWithParameters() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .method(HTTPConstants.POST)
                .param(PARAM1_NAME, PARAM1_VALUE)
                .rawParam(PARAM2_NAME, PARAM2_VALUE)
        )
    ).run();
    verify(postRequestedFor(anyUrl())
        .withHeader(HTTPConstants.HEADER_CONTENT_TYPE, equalTo(
            ContentType.APPLICATION_FORM_URLENCODED.withCharset(StandardCharsets.UTF_8).toString()))
        .withRequestBody(equalTo(buildUrlEncodedParamsQuery())));
  }

  @Test
  public void shouldSendMultiPartFormWhenPostRequestWithBodyParts() throws Exception {
    String part1Name = "part1";
    String part1Value = "value1";
    ContentType part1Encoding = ContentType.TEXT_PLAIN.withCharset(StandardCharsets.US_ASCII);
    String part2Name = "part2";
    TestResource part2File = testResource("jtls/custom-sample-jtl.xml");
    ContentType part2Encoding = ContentType.TEXT_XML;

    testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri)
                .method(HTTPConstants.POST)
                .bodyPart(part1Name, part1Value, part1Encoding)
                .bodyFilePart(part2Name, part2File.filePath(), part2Encoding)
        )
    ).run();
    verify(postRequestedFor(anyUrl())
        .withHeader(HTTPConstants.HEADER_CONTENT_TYPE,
            matching(ContentType.MULTIPART_FORM_DATA.withCharset((String) null) + "; boundary="
                + MULTIPART_BOUNDARY_PATTERN))
        .withRequestBody(matching(
            buildMultiPartBodyPattern(part1Name, part1Value, part1Encoding, part2Name, part2File,
                part2Encoding))));
  }

  private String buildMultiPartBodyPattern(String part1Name, String part1Value,
      ContentType part1Encoding, String part2Name, TestResource part2File,
      ContentType part2Encoding) throws IOException {
    String separatorPattern = "--" + MULTIPART_BOUNDARY_PATTERN;
    return separatorPattern + CRLN
        + Pattern.quote(buildBodyPart(part1Name, null, part1Value, part1Encoding, "8bit"))
        + separatorPattern + CRLN
        + Pattern.quote(
        buildBodyPart(part2Name, part2File.file().getName(), part2File.rawContents(), part2Encoding,
            "binary"))
        + separatorPattern + "--" + CRLN;
  }

  private String buildBodyPart(String name, String fileName, String value, ContentType contentType,
      String transferEncoding) {
    return HTTPConstants.HEADER_CONTENT_DISPOSITION + ": form-data; name=\"" + name + "\"" + (
        fileName != null ? "; filename=\"" + fileName + "\"" : "") + CRLN
        + org.apache.http.HttpHeaders.CONTENT_TYPE + ": " + contentType + CRLN
        + "Content-Transfer-Encoding: " + transferEncoding + CRLN
        + CRLN
        + value + CRLN;
  }

  @Test
  public void shouldSendRequestThroughProxyWhenProxyIsConfiguredInSampler() throws Exception {
    ProxyServer proxy = new ProxyServer();
    try {
      proxy.start();
      testPlan(
          threadGroup(1, 1,
              httpSampler(wiremockUri)
                  .proxy(proxy.url())
          )
      ).run();
      assertThat(proxy.proxiedRequest()).isTrue();
    } finally {
      proxy.stop();
    }
  }

  @Test
  public void shouldSendRequestThroughProxyWithAuthWhenProxyIsConfiguredInSamplerWithAuth()
      throws Exception {
    String username = "testUser";
    String password = "testPassword";
    ProxyServer proxy = new ProxyServer()
        .auth(username, password);
    try {
      proxy.start();
      testPlan(
          threadGroup(1, 1,
              httpSampler(wiremockUri)
                  .proxy(proxy.url(), username, password)
          )
      ).run();
      assertThat(proxy.proxiedRequest()).isTrue();
    } finally {
      proxy.stop();
    }
  }

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithSimpleHttpGet() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan testPlanWithNamedHttpGet() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("myRequest", "http://localhost")
          )
      );
    }

    public DslTestPlan testPlanWithSimpleHttpPost() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .post("{\"name\":\"John\"}", ContentType.APPLICATION_JSON)
          )
      );
    }

    public DslTestPlan testPlanWithSimpleHttpPostAndCustomContentType() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .post("{\"name\":\"John\"}", ContentType.create("my-content-type"))
          )
      );
    }

    public DslTestPlan testPlanWithSimpleHttpPut() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .method(HTTPConstants.PUT)
                  .contentType(ContentType.APPLICATION_JSON)
                  .body("{\"name\":\"John\"}")
          )
      );
    }

    public DslTestPlan testPlanWithCustomHttpMethod() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .method("${myMethod}")
          )
      );
    }

    public DslTestPlan testPlanWithHttpRequestAndHeaders() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .header("My-Header", "My-Value")
                  .header("Other-Header", "Other-Value")
          )
      );
    }

    public DslTestPlan testPlanWithHttpGetAndParams() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .param("param%201", "value%201")
                  .rawParam("param%202", "value%202")
          )
      );
    }

    public DslTestPlan testPlanWithHttpPostAndParams() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .method(HTTPConstants.POST)
                  .param("param%201", "value%201")
                  .rawParam("param%202", "value%202")
          )
      );
    }

    public DslTestPlan testPlanWithHttpPostAndBodyFile() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .method(HTTPConstants.POST)
                  .bodyFile("myFile.xml")
          )
      );
    }

    public DslTestPlan testPlanWithHttpPostAndMultiPart() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .method(HTTPConstants.POST)
                  .bodyPart("part1", "myPart", ContentType.TEXT_PLAIN)
                  .bodyPart("part2", "<nothing/>", ContentType.TEXT_XML)
                  .bodyFilePart("myFile", "myFile.txt", ContentType.TEXT_PLAIN)
                  .bodyFilePart("otherFile", "otherFile.xml", ContentType.TEXT_XML)
          )
      );
    }

    public DslTestPlan testPlanWithHttpPostAndStandardCharsetEncoding() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .post("{\"name\":\"John\"}", ContentType.APPLICATION_JSON)
                  .encoding(StandardCharsets.ISO_8859_1)
          )
      );
    }

    public DslTestPlan testPlanWithHttpPostAndCustomEncoding() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .post("{\"name\":\"John\"}", ContentType.APPLICATION_JSON)
                  .encoding(Charset.forName("UTF-32"))
          )
      );
    }

    public DslTestPlan testPlanWithHttpGetNotFollowingRedirects() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .followRedirects(false)
          )
      );
    }

    public DslTestPlan testPlanWithHttpGetAndEmbeddedResourcesDownload() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .downloadEmbeddedResources()
          )
      );
    }

    public DslTestPlan testPlanWithHttpGetAndMatchingEmbeddedResourcesDownload() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .downloadEmbeddedResourcesMatching(".*")
          )
      );
    }

    public DslTestPlan testPlanWithHttpGetAndNotMatchingEmbeddedResourcesDownload() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .downloadEmbeddedResourcesNotMatching(".*demo.*")
          )
      );
    }

    public DslTestPlan testPlanWithHttpGetAndJavaClientImpl() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .clientImpl(HttpClientImpl.JAVA)
          )
      );
    }

    public DslTestPlan testPlanWithHttpGetAndTimeouts() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .connectionTimeout(Duration.ofSeconds(5))
                  .responseTimeout(Duration.ofSeconds(10))
          )
      );
    }

    public DslTestPlan testPlanWithHttpGetAndProxyWithoutAuth() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .proxy("http://localhost:8181")
          )
      );
    }

    public DslTestPlan testPlanWithHttpGetAndProxyWithAuth() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
                  .proxy("http://localhost:8181", "user", "password")
          )
      );
    }

  }

}
