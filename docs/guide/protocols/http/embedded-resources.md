#### Embedded resources

Sometimes you may need to reproduce a browser behavior, downloading for a given URL all associated resources (images, frames, etc.).

jmeter-java-dsl allows you to easily reproduce this scenario by using the `downloadEmbeddedResources` method in `httpSampler` like in the following example:

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
        threadGroup(5, 10,
            httpSampler("http://my.service/")
                .downloadEmbeddedResources()
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

This will make JMeter automatically parse the HTTP response for embedded resources, download them and register embedded resources downloads as sub-samples of the main sample.

Check [JMeter documentation](https://jmeter.apache.org/usermanual/component_reference.html#HTTP_Request) for additional details on downloaded embedded resources.

::: tip
You can use `downloadEmbeddedResourcesNotMatching(urlRegex)` and `downloadEmbeddedResourcesMatching(urlRegex)` methods if you need to ignore, or only download, some embedded resources requests. For example, when some requests are not related to the system under test.
:::

::: warning
The DSL, unlike JMeter, uses by default concurrent download of embedded resources (with up to 6 parallel downloads), which is the most used scenario to emulate browser behavior.
:::

::: warning
Using `downloadEmbeddedResources` doesn't allow to download all resources that a browser could download, since it does not execute any JavaScript. For instance, resources URLs solved through JavaScript or direct JavaScript requests will not be requested. Even with this limitation, in many cases just downloading "static" resources is a good enough solution for performance testing. 
:::
