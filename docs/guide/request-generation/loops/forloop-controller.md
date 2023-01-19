#### Iterating a fixed number of times

In simple scenarios where you just want to execute a fixed number of times, within a thread group iteration, a given part of the test plan, you can just use `forLoopController` (which uses [JMeter Loop Controller component](https://jmeter.apache.org/usermanual/component_reference.html#Loop_Controller)) as in the following example:

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
            forLoopController(5,
                httpSampler("http://my.service/accounts")
            )
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

This will result in 10 * 5 = 50 requests to the given URL for each thread in the thread group.

::: tip
JMeter automatically generates a variable `__jm__<loopName>__idx` with the current index of for loop iteration (starting with 0) which you can use in children elements. The default name for the for loop controller, when not specified, is `for`.
:::

Check [ForLoopController](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/ForLoopController.java) for more details.

