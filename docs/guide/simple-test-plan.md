## Simple HTTP test plan

To generate HTTP requests just use provided `httpSampler`.

The following example uses 2 threads (concurrent users) that send 10 HTTP GET requests each to `http://my.service`.

Additionally, it logs collected statistics (response times, status codes, etc.) to a file (for later analysis if needed) and checks that the response time 99 percentile is less than 5 seconds.

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service")
        ),
        //this is just to log details of each request stats
        jtlWriter("target/jtls")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: tip
When working with multiple samplers in a test plan, specify their names (eg: `httpSampler("home", "http://my.service")`) to easily check their respective statistics.
:::

::: tip
Set connection and response timeouts to avoid potential execution differences when running test plan in different machines. [Here](./protocols/http/timeouts#timeouts) are more details.
:::

::: tip
Since JMeter uses [log4j2](https://logging.apache.org/log4j/2.x/), if you want to control the logging level or output, you can use something similar to this [log4j2.xml](/jmeter-java-dsl/src/test/resources/log4j2.xml).
:::

::: tip
Keep in mind that you can use Java programming to modularize and create abstractions which allow you to build complex test plans that are still easy to read, use and maintain. [Here is an example](https://github.com/abstracta/jmeter-java-dsl/issues/26#issuecomment-953783407) of some complex abstraction built using Java features and the DSL.
:::

Check [HTTP performance testing](./protocols/http/index#http) for additional details while testing HTTP services.