### Execute part of a test plan part a fraction of the times

In some cases, you may want to execute a given part of the test plan not in every iteration, and only for a given percent of times, to emulate certain probabilistic nature of the flow the users execute.

In such scenarios, you may use `percentController`, which uses JMeter Throughput Controller to achieve exactly that.

Here is an example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            percentController(40, // run this 40% of the times
                httpSampler("http://my.service/status"),
                httpSampler("http://my.service/poll")),
            percentController(70, // run this 70% of the times
                httpSampler("http://my.service/items"))
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [PercentController](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/PercentController.java) for more details.
