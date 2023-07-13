#### Iterating while a condition is met

If at any time you want to execute a given part of a test plan, inside a thread iteration, while a condition is met, then you can use `whileController` (internally using [JMeter While Controller](https://jmeter.apache.org/usermanual/component_reference.html#While_Controller)) like in the following example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            whileController("${__groovy(vars['ACCOUNT_ID'] == null)}",
                httpSampler("http://my.service/accounts")
                    .post("{\"name\": \"John Doe\"}", ContentType.APPLICATION_JSON)
                    .children(
                        regexExtractor("ACCOUNT_ID", "\"id\":\"([^\"]+)\"")
                    )
            )
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

As with `ifController`, you can also use Java lambdas to benefit from IDE auto-completion and type safety and less CPU consumption. Eg:

```java
whileController(s -> s.vars.get("ACCOUNT_ID") == null,
    httpSampler("http://my.service/accounts")
      .post("{\"name\": \"John Doe\"}", Type.APPLICATION_JSON)
      .children(
        regexExtractor("ACCOUNT_ID", "\"id\":\"([^\"]+)\"")
      )
)
```

::: warning
Even though using Java Lambdas has several benefits, they are also less portable. Check [this section](../../response-processing/lambdas.md#lambdas) for more details.
:::

::: warning
JMeter evaluates while conditions before entering each iteration, and after exiting each iteration. Take this into consideration if the condition has side effects (eg: incrementing counters, altering some other state, etc).
:::

::: tip
JMeter automatically generates a variable `__jm__<loopName>__idx` with the current index of while iteration (starting with 0). Example:

```java
whileController("items", "${__groovy(vars.getObject('__jm__items__idx') < 4)}",
    httpSampler("http://my.service/items")
      .post("{\"name\": \"My Item\"}", Type.APPLICATION_JSON)
)
```

The default name for the while controller, when not specified, is `while`.
:::

Check [DslWhileController](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/DslWhileController.java) for more details.
