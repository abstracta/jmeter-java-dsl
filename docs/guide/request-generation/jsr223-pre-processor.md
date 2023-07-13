### Provide request parameters programmatically per request

So far we have seen a few ways to generate requests with information extracted from CSV or through a counter, but this is not enough for some scenarios. When you need more flexibility and power you can use `jsr223preProcessor` to specify your own logic to build each request.

Here is an example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service")
                .post("${REQUEST_BODY}", ContentType.TEXT_PLAIN)
                .children(
                    jsr223PreProcessor("vars.put('REQUEST_BODY', " + getClass().getName()
                        + ".buildRequestBody(vars))")
                )
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

  public static String buildRequestBody(JMeterVariables vars) {
    String countVarName = "REQUEST_COUNT";
    Integer countVar = (Integer) vars.getObject(countVarName);
    int count = countVar != null ? countVar + 1 : 1;
    vars.putObject(countVarName, count);
    return "MyBody" + count;
  }

}
```

You can also use a Java lambda instead of providing Groovy script, which benefits from Java type safety & IDEs code auto-completion and consumes less CPU:

```java
jsr223PreProcessor(s -> s.vars.put("REQUEST_BODY", buildRequestBody(s.vars)))
```

Or even use this shorthand:

```java
post(s -> buildRequestBody(s.vars), Type.TEXT_PLAIN)
```

::: warning
Even though using Java Lambdas has several benefits, they are also less portable. Check [this section](../response-processing/lambdas.md#lambdas) for more details.
:::

::: tip
`jsr223PreProcessor` is quite powerful. But, provided example can easily be achieved through the usage of [counter element](./counter#counter).  
:::

Check [DslJsr223PreProcessor](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/preprocessors/DslJsr223PreProcessor.java) & [DslHttpSampler](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/DslHttpSampler.java) for more details and additional options.
