#### Proxy

Sometimes, due to company policies, some infrastructure requirement or just to further analyze or customize requests, for example, through the usage of tools like [fiddler](https://www.telerik.com/fiddler) and [mitmproxy](https://mitmproxy.org/), you need to specify a proxy server through which HTTP requests are sent to their final destination. This can be easily done with `proxy` method, like in the following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void test() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpSampler("https://myservice.com")
                .proxy("http://myproxy:8081")
        )
    ).run();
  }

}
```

::: tip
You can also specify proxy authentication parameters with `proxy(url, username, password)` method.
:::

::: tip
When you need to set a proxy for several samplers, use `httpDefaults().proxy` methods.
:::
