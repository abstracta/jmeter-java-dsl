#### DataDog

Another option is using `jmeter-java-dsl-datadog` module which uses existing [jmeter-datadog-backend-listener plugin](https://github.com/DataDog/jmeter-datadog-backend-listener) to upload metrics to datadog which you can easily visualize and analize with [DataDog provided JMeter dashboard](https://app.datadoghq.com/integrations/jmeter?search=jmeter). Here is an example of what you get:

![datadog jmeter dashboard](./datadog.png)

To use the module, just include the dependency:

:::: code-group type:card
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-datadog</artifactId>
  <version>1.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
repositories {
    ...
    maven { url 'https://jitpack.io' }
}

dependencies {
    ...
    testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-datadog:1.19'
}
```
:::
::::

And use provided `datadogListener()` method like in this example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.datadog.DatadogBackendListener.*;

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
        ),
        datadogBackendListener(System.getenv("DATADOG_APIKEY"))
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: tip
If you use a DataDog instance in a site different than US1 (the default one), you can use `.site(DatadogSite)` method to select the proper site. 
:::

::: tip
You can use `.resultsLogs(true)` to send results samples as logs to DataDog to get more information in DataDog on each sample of the test plan (for example for tracing). Enabling this property requires additional network traffic, that may affect test plan execution, and costs on DataDog, so use it sparingly. 
:::

::: tip
You can use `.tags()` to add additional information to metrics sent to DataDog. Check [DataDog documentation](https://docs.datadoghq.com/getting_started/tagging/) for more details. 
:::

