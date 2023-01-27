package us.abstracta.jmeter.javadsl.http;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCookies;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Nested;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.http.DslCookieManager.CookiePolicy;

public class DslCookieManagerTest {

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

  }

}
