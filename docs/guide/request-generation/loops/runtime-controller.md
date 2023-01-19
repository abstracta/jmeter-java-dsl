#### Iterating for a given period

In some scenarios you might want to execute a given logic until all the steps are executed or a given period of time has passed. In these scenarios you can use `runtimeController` which stops executing children elements when a specified time is reached.

Here is an example which makes requests to a page until token expires by using `runtimeController` in combination with `whileController`.

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
    Duration tokenExpiration = Duration.ofSeconds(5);
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service/token"),
            runtimeController(tokenExpiration,
                whileController("true",
                    httpSampler("http://my.service/accounts")
                )
            )
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [DslRuntimeController](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/DslRuntimeController.java) for more details.
