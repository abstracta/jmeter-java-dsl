### View results tree

One option is using provided `resultsTreeVisualizer()` like in the following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    testPlan(
        threadGroup(1, 1,
            httpSampler("http://my.service")
        ),
        resultsTreeVisualizer()
    ).run();
  }

}
```

This will display the JMeter built-in View Results Tree element, which allows you to review request and response contents in addition to collected metrics (spent time, sent & received bytes, etc.) for each request sent to the server, in a window like this one:

![View Results Tree GUI](./images/view-results-tree.png)

::: tip
To debug test plans use a few iterations and threads to reduce the execution time and ease tracing by having less information to analyze.
:::

::: tip
When adding `resultsTreeVisualizer()` as a child of a thread group, it will only display sample results of that thread group. When added as a child of a sampler, it will only show sample results for that sampler. You can use this to only review certain sample results in your test plan.
:::

::: tip
**Remove `resultsTreeVisualizer()` from test plans when are no longer needed** (when debugging is finished). Leaving them might interfere with unattended test plan execution (eg: in CI) due to test plan execution not finishing until all visualizers windows are closed.
:::

::: warning
By default, View Results Tree only displays the last 500 sample results. If you need to display more elements, use provided `resultsLimit(int)` method which allows changing this value. Take into consideration that the more results are shown, the more memory that will require. So use this setting with care.
:::