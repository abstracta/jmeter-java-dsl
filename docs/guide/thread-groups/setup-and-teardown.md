### Set up & tear down

When you need to run some custom logic before or after a test plan, the simplest approach is just adding plain java code to it, or using your test framework (eg: JUnit) provided features for this purpose. Eg:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @BeforeEach
  public void setup() {
    // my custom setup logic
  }

  @AfterEach
  public void setup() {
    // my custom setup logic
  }

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service")
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

But, in some cases you may need the logic to run inside the JMeter execution context (eg: set some JMeter properties), or, when the test plan runs at scale, to run in the same host where the test plan runs (for example to use some common file).

In such scenarios you can use provided `setupThreadGroup` & `teardownThreadGroup` like in the following example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        setupThreadGroup(
            httpSampler("http://my.service/tokens")
                .method(HTTPConstants.POST)
                .children(
                    jsr223PostProcessor("props.put('MY_TEST_TOKEN', prev.responseDataAsString)")
                )
        ),
        threadGroup(2, 10,
            httpSampler("http://my.service/products")
                .header("X-MY-TOKEN", "${__P(MY_TEST_TOKEN)}")
        ),
        teardownThreadGroup(
            httpSampler("http://my.service/tokens/${__P(MY_TEST_TOKEN)}")
                .method(HTTPConstants.DELETE)
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: tip
By default, JMeter automatically executes teardown thread groups when a test plan stops due to an unscheduled event like a sample error when a stop test action is configured in a thread group, invocation of `ctx.getEngine().askThreadsToStop()` in jsr223 element, etc. You can disable this behavior by using the testPlan `tearDownOnlyAfterMainThreadsDone` method, which might be helpful if the teardown
thread group has only to run on clean test plan completion.
:::

Check [DslSetupThreadGroup](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/threadgroups/DslSetupThreadGroup.java) and [DslTeardownThreadGroup](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/threadgroups/DslTeardownThreadGroup.java) for additional tips and details on the usage of these components.
