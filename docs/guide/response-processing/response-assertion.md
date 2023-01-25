### Check for expected response

By default, JMeter marks any HTTP request with a fail response code (4xx or 5xx) as failed, which allows you to easily identify when some request unexpectedly fails. But in many cases, this is not enough or desirable, and you need to check for the response body (or some other field) to contain (or not) a certain string.

This is usually accomplished in JMeter with the usage of Response Assertions, which provides an easy and fast way to verify that you get the proper response for each step of the test plan, marking the request as a failure when the specified condition is not met.

Here is an example of how to specify a response assertion in jmeter-java-dsl:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service")
                .children(
                    responseAssertion().containsSubstrings("OK")
                )
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [Response Assertion](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/assertions/DslResponseAssertion.java) for more details and additional options.

For more complex scenarios check [following section](./jsr223-post-processor#change-sample-result-statuses-with-custom-logic).
