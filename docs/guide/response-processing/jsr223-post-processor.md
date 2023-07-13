### Change sample result statuses with custom logic

Sometimes [response assertions](./response-assertion#check-for-expected-response) and JMeter default behavior are not enough, and custom logic is required. In such scenarios you can use `jsr223PostProcessor` as in this example where the 429 status code is not considered as a fail status code:

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
                    jsr223PostProcessor(
                        "if (prev.responseCode == '429') { prev.successful = true }")
                )
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

You can also use a Java lambda instead of providing Groovy script, which benefits from Java type safety & IDEs code auto-completion and consumes less CPU:

```java
jsr223PostProcessor(s -> {
    if ("429".equals(s.prev.getResponseCode())) {
      s.prev.setSuccessful(true);
    }
})
```

::: warning
Even though using Java Lambdas has several benefits, they are also less portable. Check [following section](./lambdas.md#lambdas) for more details.  
:::

Check [DslJsr223PostProcessor](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslJsr223PostProcessor.java) for more details and additional options.

::: warning
JSR223PostProcessor is a very powerful tool but is not the only, nor the best, alternative for many cases where JMeter already provides a better and simpler alternative. For instance, the previous example might be implemented with previously presented [Response Assertion](./response-assertion#check-for-expected-response).
:::

<!-- @include: lambdas.md -->
