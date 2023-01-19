#### Iterating over extracted values

A common use case is to iterate over a list of values extracted from a previous request and execute part of the plan for each extracted value. This can be easily done using `foreachController` like in the following example:

```java
package us.abstracta.jmeter.javadsl;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    String productsIdVarName = "PRODUCT_IDS";
    String productIdVarName = "PRODUCT_ID";
    String productsPath = "/products";
    TestPlanStats stats = testPlan(
        httpDefaults().url("http://my.service"),
        threadGroup(2, 10,
            httpSampler(productsPath)
                .children(
                    jsonExtractor(productsIdVarName, "[].id")
                        .matchNumber(-1)
                ),
            forEachController(productsIdVarName, productIdVarName,
                httpSampler(productsPath + "/${" + productIdVarName + "}")
            )
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: tip
JMeter automatically generates a variable `__jm__<loopName>__idx` with the current index of the for each iteration (starting with 0), which you can use in controller children elements if needed. The default name for the for each controller, when not specified, is `foreach`.
:::

Check [DslForEachController](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/DslForEachController.java) for more details.
