package us.abstracta.jmeter.javadsl.http;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpCache;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.transaction;

import org.junit.jupiter.api.Nested;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class DslCacheManagerTest {

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithCacheDisabled() {
      return testPlan(
          httpCache().disable(),
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              httpSampler("http://mysite.com")
          )
      );
    }

    public DslTestPlan testPlanWithCacheAndHttpRequests() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan testPlanWithCacheAndNoHttpRequests() {
      return testPlan(
          httpCache(),
          threadGroup(1, 1)
      );
    }

    public DslTestPlan testPlanWithCacheAtDifferentLevelsAndConfigs() {
      return testPlan(
          threadGroup(1, 1,
              transaction("testCache",
                  httpCache()
              ),
              transaction("testSampler",
                  httpSampler("http://localhost")
              ),
              transaction("testDisabledCache",
                  httpCache().disable(),
                  httpSampler("http://localhost")
              ),
              transaction("testSamplerAndDisabledCacheAndSampler",
                  httpSampler("http://localhost")
                      .children(
                          httpCache().disable()
                      ),
                  httpSampler("http://localhost")
              )
          )
      );
    }

  }

}
