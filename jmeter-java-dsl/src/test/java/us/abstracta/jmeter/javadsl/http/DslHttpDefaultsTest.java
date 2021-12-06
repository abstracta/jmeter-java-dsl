package us.abstracta.jmeter.javadsl.http;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpDefaults;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslHttpDefaultsTest extends JmeterDslTest {

  @Test
  public void shouldUseDefaultSettingsWhenHttpDefaultAndNoOverwrites() throws Exception {
    String primaryUrl = "/primary";
    String resourceUrl = "/resource";
    wiremockServer.stubFor(get(primaryUrl)
        .willReturn(HttpResponseBuilder.buildEmbeddedResourcesResponse(resourceUrl)));
    testPlan(
        httpDefaults()
            .url(wiremockUri + primaryUrl)
            .downloadEmbeddedResources(),
        threadGroup(1, 1,
            httpSampler((String) null)
        )
    ).run();
    wiremockServer.verify(getRequestedFor(urlPathEqualTo(resourceUrl)));
  }

  @Test
  public void shouldUseOverwrittenPathWhenHttpDefaultWithOverwrittenPath() throws Exception {
    String customPath = "/customPath";
    testPlan(
        httpDefaults()
            .url(wiremockUri + "/defaultPath"),
        threadGroup(1, 1,
            httpSampler(customPath)
        )
    ).run();
    wiremockServer.verify(getRequestedFor(urlPathEqualTo(customPath)));
  }

  @Test
  public void shouldUseOverwrittenUrlWhenHttpDefaultWithOverwrittenUrl() throws Exception {
    testPlan(
        httpDefaults()
            .url("http://mytest.my/local"),
        threadGroup(1, 1,
            httpSampler(wiremockUri)
        )
    ).run();
    wiremockServer.verify(getRequestedFor(urlPathEqualTo("/")));
  }

}
