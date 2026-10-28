package us.abstracta.jmeter.javadsl.http;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCookie;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCookies;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.http.DslCookieManager.Cookie;
import us.abstracta.jmeter.javadsl.http.DslCookieManager.CookiePolicy;

public class DslCookieManagerTest extends JmeterDslTest {

  private static final String DOMAIN = "localhost";

  @Test
  public void shouldSendCustomCookieOnlyAfterItIsSetWhenConfiguredAtThreadGroupLevel()
      throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            httpSampler(wiremockUri + "/step1"),
            httpCookies()
                .add(httpCookie("MyCookie", "val", DOMAIN)),
            httpSampler(wiremockUri + "/step2")
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo("/step1")).withoutHeader("Cookie"));
    verify(getRequestedFor(urlPathEqualTo("/step2"))
        .withHeader("Cookie", equalTo("MyCookie=val")));
    assertThat(stats.overall().samplesCount()).isEqualTo(2);
  }

  @Test
  public void shouldSendCustomCookieToAllRequestsWhenConfiguredAtTestPlanLevel() throws Exception {
    testPlan(
        httpCookies()
            .add(httpCookie("MyCookie", "val", DOMAIN)),
        threadGroup(1, 1,
            httpSampler(wiremockUri + "/step1"),
            httpSampler(wiremockUri + "/step2")
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo("/step1"))
        .withHeader("Cookie", equalTo("MyCookie=val")));
    verify(getRequestedFor(urlPathEqualTo("/step2"))
        .withHeader("Cookie", equalTo("MyCookie=val")));
  }

  @Test
  public void shouldResetCustomCookiesEachIterationWhenDefaultClearingEnabled() throws Exception {
    testPlan(
        threadGroup(1, 2,
            httpSampler(wiremockUri + "/step1"),
            httpCookies()
                .add(httpCookie("MyCookie", "val", DOMAIN)),
            httpSampler(wiremockUri + "/step2")
        )
    ).run();
    verify(2, getRequestedFor(urlPathEqualTo("/step1")).withoutHeader("Cookie"));
    verify(2, getRequestedFor(urlPathEqualTo("/step2"))
        .withHeader("Cookie", equalTo("MyCookie=val")));
  }

  @Test
  public void shouldKeepCustomCookiesBetweenIterationsWhenClearingDisabledAtThreadGroupLevel()
      throws Exception {
    testPlan(
        threadGroup(1, 2,
            httpCookies()
                .clearCookiesBetweenIterations(false),
            httpSampler(wiremockUri + "/step1"),
            httpCookies()
                .add(httpCookie("MyCookie", "val", DOMAIN)),
            httpSampler(wiremockUri + "/step2")
        )
    ).run();
    verify(1, getRequestedFor(urlPathEqualTo("/step1")).withoutHeader("Cookie"));
    verify(1, getRequestedFor(urlPathEqualTo("/step1"))
        .withHeader("Cookie", equalTo("MyCookie=val")));
  }

  @Test
  public void shouldSendMultipleCookiesWhenConfigured() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpCookies()
                .add(
                    httpCookie("c1", "v1", DOMAIN),
                    httpCookie("c2", "v2", DOMAIN)
                ),
            httpSampler(wiremockUri + "/test")
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo("/test"))
        .withHeader("Cookie", containing("c1=v1"))
        .withHeader("Cookie", containing("c2=v2")));
  }

  @Test
  public void shouldSendCookieWithGroovySpecialCharsWhenConfiguredAtThreadGroupLevel()
      throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpCookies()
                .add(httpCookie("MyCookie", "it's$1", DOMAIN)),
            httpSampler(wiremockUri + "/test")
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo("/test"))
        .withHeader("Cookie", equalTo("MyCookie=it's$1")));
  }

  @Test
  public void shouldSendCookieWhenNotYetExpired() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpCookies()
                .add(httpCookie("ValidCookie", "val", DOMAIN)
                    .expires(Instant.now().plusSeconds(60))),
            httpSampler(wiremockUri + "/test")
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo("/test"))
        .withHeader("Cookie", equalTo("ValidCookie=val")));
  }

  @Test
  public void shouldNotSendCookieWhenExpiredAtThreadGroupLevel() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpCookies()
                .add(httpCookie("ExpiredCookie", "val", DOMAIN)
                    .expires(Instant.now().minusSeconds(60))),
            httpSampler(wiremockUri + "/test")
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo("/test")).withoutHeader("Cookie"));
  }

  @Test
  public void shouldNotSendCookieWhenExpiredAtTestPlanLevel() throws Exception {
    testPlan(
        httpCookies()
            .add(httpCookie("ExpiredCookie", "val", DOMAIN)
                .expires(Instant.now().minusSeconds(60))),
        threadGroup(1, 1,
            httpSampler(wiremockUri + "/test")
        )
    ).run();
    verify(getRequestedFor(urlPathEqualTo("/test")).withoutHeader("Cookie"));
  }

  @Test
  public void shouldThrowExceptionWhenAddingCookiesToDisabledCookieManager() {
    assertThatThrownBy(() -> testPlan(
        threadGroup(1, 1,
            httpCookies()
                .disable()
                .add(httpCookie("MyCookie", "val", DOMAIN)),
            httpSampler(wiremockUri + "/test")
        )
    ).run()).isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void shouldThrowExceptionWhenAddingCookiesWhereCookiesAreDisabled() {
    assertThatThrownBy(() -> testPlan(
        httpCookies().disable(),
        threadGroup(1, 1,
            httpCookies()
                .add(httpCookie("MyCookie", "val", DOMAIN)),
            httpSampler(wiremockUri + "/test")
        )
    ).run()).isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void shouldGetSameCookieWhenConvertToAndFromJMeterCookie() {
    Cookie cookie = httpCookie("name", "val", "example.com")
        .path("/path")
        .secure(true)
        .expires(Instant.ofEpochSecond(1700000000L));
    assertThat(Cookie.fromJMeterCookie(cookie.toJMeterCookie())).isEqualTo(cookie);
  }

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithCookiesDisabled() {
      return testPlan(
          httpCookies().disable(),
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              httpSampler("http://mysite.com")
          )
      );
    }

    public DslTestPlan testPlanWithCookiesAndHttpRequests() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan testPlanWithCookiesAndNoHttpRequests() {
      return testPlan(
          httpCookies(),
          threadGroup(1, 1
          )
      );
    }

    public DslTestPlan testPlanWithCookiePolicy() {
      return testPlan(
          httpCookies()
              .cookiePolicy(CookiePolicy.NETSCAPE),
          threadGroup(1, 1
          )
      );
    }

    public DslTestPlan testPlanWithClearIterationsOff() {
      return testPlan(
          httpCookies()
              .clearCookiesBetweenIterations(false),
          threadGroup(1, 1
          )
      );
    }

    public DslTestPlan testPlanWithCustomCookies() {
      return testPlan(
          httpCookies()
              .add(
                  httpCookie("c1", "v1", "example.com")
                      .path("/api")
                      .secure(true)
                      .expires(Instant.ofEpochSecond(4102444800L)),
                  httpCookie("c2", "v2", "example.com")
              ),
          threadGroup(1, 1
          )
      );
    }

  }

}
