### Conditionals

At some point, you will need to execute part of a test plan according to a certain condition (eg: a value extracted from a previous request). When you reach that point, you can use `ifController` like in the following example:

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
            httpSampler("http://my.service/accounts")
                .post("{\"name\": \"John Doe\"}", ContentType.APPLICATION_JSON)
                .children(
                    regexExtractor("ACCOUNT_ID", "\"id\":\"([^\"]+)\"")
                ),
            ifController("${__groovy(vars['ACCOUNT_ID'] != null)}",
                httpSampler("http://my.service/accounts/${ACCOUNT_ID}")
            )
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

You can also use a Java lambda instead of providing JMeter expression, which benefits from Java type safety & IDEs code auto-completion:

```java
ifController(s -> s.vars.get("ACCOUNT_ID") != null,
    httpSampler("http://my.service/accounts/${ACCOUNT_ID}")
)
```

::: warning
Using java code (lambdas) will only work with embedded JMeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with BlazeMeter, OctoPerf or Azure). Use the first option to avoid such limitations.
:::

Check [DslIfController](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/DslIfController.java) and [JMeter Component documentation](https://jmeter.apache.org/usermanual/component_reference.html#If_Controller) for more details.
