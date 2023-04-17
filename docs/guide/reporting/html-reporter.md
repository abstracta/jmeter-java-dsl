### Generate HTML reports from test plan execution

After running a test plan you would usually like to visualize the results in a friendly way that eases the analysis of collected information.

One, and preferred way, to do that is through [previously mentioned alternatives](./real-time/index.md#real-time-metrics-visualization-and-historic-data-storage).

Another way might just be using [previously introduced](../simple-test-plan#simple-http-test-plan) `jtlWriter` and then loading the jtl file in JMeter GUI with one of JMeter provided listeners (like view results tree, summary report, etc.).

Another alternative is generating a standalone report for the test plan execution using jmeter-java-dsl provided `htmlReporter` like this:

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
        htmlReporter("reports")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: warning
`htmlReporter` will create one directory for each generated report by applying the following template: `<yyyy-MM-dd HH-mm-ss> <UUID>`.

If you need a particular name for the report directory, for example for postprocessing logic (eg: adding CI build ID), you can use `htmlReporter(reportsDirectory, name)` to specify the name.

Make sure when specifying the name, for it to be unique, otherwise report generation will fail after test plan execution.
:::

::: tip
Time graphs by default group metrics per minute, but you can change this with provided `timeGraphsGranularity` method.
:::
