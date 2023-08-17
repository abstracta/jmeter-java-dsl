## Auto Stop

As previously shown, it is quite easy to check after test plan execution if the collected metrics are the expected ones and fail/pass the test accordingly.

But, what if you want to stop your test plan as soon as the metrics deviate from expected ones? This could help avoiding unnecessary resource usage, especially when conducting tests at scale to avoid incurring additional costs.

With JMeter DSL you can easily define auto-stop conditions over collected metrics, that when met will stop the test plan and throw an exception that will make your test fail.

Here is an example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.core.listeners.AutoStopListener.AutoStopCondition.*;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, Duration.ofMinutes(1),
          httpSampler("http://my.service")
        ),
        autoStop()
          .when(errors().total().isGreaterThan(0)) // when any sample fails, then test plan will stop and an exception will be thrown pointing to this condition.
    ).run();
  }

}

```

Check [AutoStopListener](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/AutoStopListener.java) for details on available options for auto-stop conditions.

`autoStop` is inspired in [JMeter AutoStop Plugin](https://jmeter-plugins.org/wiki/AutoStop/), but provides a lot more flexibility.

::: tip
`autoStop` will only consider samples within its scope. 

If you place it as a test plan child, then it will evaluate metrics for all samples. If you place it as a thread group child, then it will evaluate metrics for samples of such thread group. If you place it as a controller child, then only samples within such controller. And, if you place it as a sampler child, it will only evaluate samples for that particular sampler.

Additionally, you can use the `samplesMatching(regex)` method to only evaluate metrics for a subset of samples within a given scope (eg: all samples with a label starting with `users`). 
:::

::: tip
You can add multiple `autoStop` elements within a test plan. The first one containing a condition that is met will trigger the auto-stop.

To identify which `autoStop` element triggered, you can specify a name, like `autoStop("login")`, and the associated name will be included in the exception thrown by `autoStop` when the test plan is stopped.

Additionally, you can specify several conditions on an `autoStop` element. When any of such conditions are met, then the test plan is stopped.
:::

::: tip
By default, `autoStop` will evaluate each condition for each sample and stop the test plan as soon as a condition is met.

This behavior is different from [JMeter AutoStop Plugin](https://jmeter-plugins.org/wiki/AutoStop/), which evaluates and resets aggregations (it only provides average aggregation) for every second. 

To change this behavior you can use the `every(Duration)` method (after specifying the aggregation method, eg `errors().perSecond().every(Duration.ofSeconds(5)))`) to specify that the condition should only be evaluated, and the aggregation reset, for every given period. 

**This is particularly helpful for some aggregations (like `mean`, `perSecond`, and `percent`) which may get "stuck" due to historical values collected for the metric.**

As an example to illustrate this issue, consider the scenario where after 10 minutes you get 10k requests with an average sample time of 1 second, but in the last 10 seconds you get 10 requests with an average of 10 seconds. In this scenario, the general average will not be much affected by the last seconds, but you would in any case want to stop the test plan since last seconds average has been way up the expected value. This is a clear scenario where you would like to use the `every()` method.
:::

::: tip
By default, `autoStop` will stop the test plan as soon as the condition is met, but in many cases it is better to wait for the condition to be met for some period of time, to avoid some intermittent or short-lived condition. To not stop the test plan until the condition holds for a given period of time, you can use `holdsFor(Duration)` at the end of your condition. 
:::

::: warning
`autoStop` will automatically work with `AzureEngine`. But no support has been implemented yet for `BlazeMeterEngine` or `OctoPerfEngine`. If you need such support, please create [an issue in the GitHub repository](https://github.com/abstracta/jmeter-java-dsl/issues).
:::
