#### Graphite

In a similar fashion to InfluxDB, you can use Graphite and Grafana. Here is an example test plan using the `graphiteListener`:

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
        ),
        graphiteListener("localhost:2004")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

As in the InfluxDB scenario, you can try it locally by running `docker-compose up` (previously [installing Docker](https://docs.docker.com/get-docker/) in your machine) inside [this directory](/docs/guide/reporting/real-time/graphite). After containers are started, you can follow the same steps as in the InfluxDB scenario.

::: warning
Use the provided `docker-compose` settings for local tests only. It uses weak credentials and is not properly configured for production purposes.
:::

::: warning
`graphiteListener` is configured to use Pickle Protocol, and port 2004, by default. This is more efficient than text plain protocol, which is the one used by default by JMeter.
:::
