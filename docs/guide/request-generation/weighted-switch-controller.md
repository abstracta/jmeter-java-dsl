### Switch between test plan parts with a given probability

In some cases, you need to switch in a test plan between different behaviors assigning to them different probabilities. The main difference between this need and the previous one is that in each iteration you have to execute one of the parts, while in the previous case you might get multiple or no part executed on a given iteration.

For this scenario you can use `weightedSwitchCotroller`, like in this example:

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
            weightedSwitchController()
                .child(30, httpSampler("https://myservice/1")) // will run 30/(30+20)=60% of the iterations
                .child(20, httpSampler("https://myservice/2")) // will run 20/(30+20)=40% of the iterations
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

[DslWeightedSwitchController](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/DslWeightedSwitchController.java) for more details.
