### Parallel requests

JMeter provides two main ways for running requests in parallel: thread groups and HTTP samplers downloading embedded resources in parallel. But in some cases is necessary to run requests in parallel which can't be properly modeled with previously mentioned scenarios. For such cases, you can use `paralleController` which allows using the [Parallel Controller plugin](https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/parallel/Parallel.md) to execute a given set of requests in parallel (while in a JMeter thread iteration step).

To use it, add the following dependency to your project:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-parallel</artifactId>
  <version>1.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-dashboard:1.19'
```
:::
::::

And use it, like in the following example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.parallel.ParallelController.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            parallelController(
                httpSampler("http://my.service/status"),
                httpSampler("http://my.service/poll"))
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: tip
By default, the controller has no limit on the number of parallel requests per JMeter thread. You can set a limit by using provided `maxThreads(int)` method. Additionally, you can opt to aggregate children's results in a parent sampler using `generateParentSample(boolean)` method, in a similar fashion to the transaction controller.
:::

::: tip
When requesting embedded resources of an HTML response, prefer using `downloadEmbeddedResources()` method in `httpSampler` instead. Likewise, when you just need independent parts of a test plan to execute in parallel, prefer using different thread groups for each part.
:::

Check [ParallelController](/jmeter-java-dsl-parallel/src/main/java/us/abstracta/jmeter/javadsl/parallel/ParallelController.java) for additional info.
