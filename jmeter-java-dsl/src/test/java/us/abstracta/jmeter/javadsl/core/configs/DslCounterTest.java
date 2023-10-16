package us.abstracta.jmeter.javadsl.core.configs;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static us.abstracta.jmeter.javadsl.JmeterDsl.counter;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class DslCounterTest extends JmeterDslTest {

  @Test
  public void shouldUseIncrementalValuesInRequestWhenSharedCounterInRequest() throws Exception {
    int threads = 2;
    int iterations = 2;
    testPlan(
        threadGroup(threads, iterations,
            counter("USER_ID"),
            httpSampler(wiremockUri + "/${USER_ID}")
        )
    ).run();
    for (int i = 0; i < threads * iterations; i++) {
      verify(getRequestedFor(urlEqualTo("/" + i)));
    }
  }

  @Test
  public void shouldUseIncrementalValuesInRequestWhenCounterInRequest() throws Exception {
    int startingValue = 1;
    int increment = 2;
    int threads = 2;
    testPlan(
        threadGroup(threads, 3,
            counter("USER_ID")
                .startingValue(startingValue)
                .increment(increment)
                .maximumValue(startingValue + increment)
                .perThread(true),
            httpSampler(wiremockUri + "/${USER_ID}")
        )
    ).run();
    verify(threads * 2, getRequestedFor(urlEqualTo("/" + startingValue)));
    verify(threads, getRequestedFor(urlEqualTo("/" + (startingValue + increment))));
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithDefaultCounter() {
      return testPlan(
          counter("MY_COUNTER")
      );
    }

    public DslTestPlan testPlanWithCustomizedCounter() {
      return testPlan(
          counter("MY_COUNTER")
              .startingValue(1)
              .increment(2)
              .maximumValue(10)
              .perThread(true)
      );
    }

  }

}
