### Provide request parameters programmatically per request

With the standard DSL you can provide static values to request parameters, such as a body. However, you may also want to be able to modify your requests for each call. This is common in cases where your request creates something that must have unique values.

Here is an example of how to achieve this:

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

You can also use a Java lambda instead of providing Groovy script, which benefits from Java type safety & IDEs code auto-completion:

```java
jsr223PreProcessor(s -> s.vars.put("REQUEST_BODY", buildRequestBody(s.vars)))
```

Or even use this shorthand:

```java
post(s -> buildRequestBody(s.vars), Type.TEXT_PLAIN)
```

::: warning
Using java code (lambdas) will only work with embedded JMeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with BlazeMeter or OctoPerf). Use the first option to avoid such limitations.
:::

Check [DslJsr223PreProcessor](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/preprocessors/DslJsr223PreProcessor.java) & [DslHttpSampler](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/DslHttpSampler.java) for more details and additional options.
