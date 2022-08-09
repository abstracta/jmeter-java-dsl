package us.abstracta.jmeter.javadsl.http;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpDefaults;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslHttpDefaultsTest extends JmeterDslTest {

  @Test
  public void shouldUseDefaultSettingsWhenHttpDefaultAndNoOverwrites() throws Exception {
    String primaryUrl = "/primary";
    String resourceUrl = "/resource";
    stubFor(get(primaryUrl)
        .willReturn(HttpResponseBuilder.buildEmbeddedResourcesResponse(resourceUrl)));
    testPlan(
        httpDefaults()
            .url(wiremockUri + primaryUrl)
            .downloadEmbeddedResources(),
        threadGroup(1, 1,
            httpSampler((String) null)
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo(resourceUrl)));
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
    verify(getRequestedFor(urlPathEqualTo(customPath)));
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
    verify(getRequestedFor(urlPathEqualTo("/")));
  }

  @Test
  public void shouldUseDefaultSettingsWhenHttpDefaultWithIndividualUrlComponents()
      throws Exception {
    String path = "/get";
    testPlan(
        httpDefaults()
            .protocol("http")
            .host("localhost")
            .port(wiremockServer.getHttpPort())
            .path(path),
        threadGroup(1, 1,
            httpSampler((String) null)
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo(path)));
  }

  @Test
  public void shouldSendRequestThroughProxyWhenProxyIsConfiguredInConfig() throws Exception {
    ProxyServer proxy = new ProxyServer();
    try {
      proxy.start();
      testPlan(
          httpDefaults()
              .proxy(proxy.url()),
          threadGroup(1, 1,
              httpSampler(wiremockUri)
          )
      ).run();
      assertThat(proxy.proxiedRequest()).isTrue();
    } finally {
      proxy.stop();
    }
  }

  @Test
  public void shouldSendRequestThroughProxyWithAuthWhenProxyIsConfiguredInConfigWithAuth()
      throws Exception {
    String username = "testUser";
    String password = "testPassword";
    ProxyServer proxy = new ProxyServer()
        .auth(username, password);
    try {
      proxy.start();
      testPlan(
          httpDefaults()
              .proxy(proxy.url(), username, password),
          threadGroup(1, 1,
              httpSampler(wiremockUri)
          )
      ).run();
      assertThat(proxy.proxiedRequest()).isTrue();
    } finally {
      proxy.stop();
    }
  }

  @Test
  public void shouldShowInGuiWhenShowInGui() {
    Robot robot = BasicRobot.robotWithNewAwtHierarchy();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      executor.submit(() -> {
        FrameFixture frame = WindowFinder.findFrame(JFrame.class)
            .withTimeout(10, TimeUnit.SECONDS)
            .using(robot);
        frame.requireVisible();
        frame.close();
      });
      httpDefaults().showInGui();
    } finally {
      executor.shutdownNow();
      robot.cleanUp();
    }
  }

}
