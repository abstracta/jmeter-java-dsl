#### Emulate user delays between requests

Sometimes, is necessary to replicate users' behavior on the test plan adding pauses between requests is one of the most used practices. For example, simulate the time it will take to complete a purchase form. JMeter (and the DSL) provide Constant & Uniform Random timers for this purpose. Here is an example that adds a delay of 3 seconds and another between 4 and 10 seconds:

```java
import java.io.IOException;
import java.time.Duration;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testTransactions() throws IOException {
    testPlan(
        threadGroup(2, 10,
            transaction("addItemToCart",
                httpSampler("http://my.service/items")
                    .children(
                        constantTimer(Duration.ofSeconds(3))
                    ),
                httpSampler("http://my.service/cart/selected-items")
                    .post("{\"id\": 1}", ContentType.APPLICATION_JSON)
            ),
            transaction("checkout",
                httpSampler("http://my.service/cart/chekout"),
                uniformRandomTimer(Duration.ofSeconds(4), Duration.ofSeconds(10)),
                httpSampler("http://my.service/cart/checkout/userinfo")
                    .post(
                        "{\"Name\": Dave, \"lastname\": Tester, \"Street\": 1483  Smith Road, \"City\": Atlanta}",
                        ContentType.APPLICATION_JSON)
            )
        )
    ).run();
  }

}
```

::: warning
Timers apply to all samplers in their scope, adding a pause after pre-processor executions and before the actual sampling. For example, in the previous example, pauses would be added before items, checkout, and also before user info (three pauses).
:::

::: warning
`uniformRandomTimer` `minimum` and `maximum` parameters differ from the ones used by JMeter Uniform Random Timer element, to make it simpler for users with no JMeter background.

The generated JMeter test element uses the `Constant Delay Offset` set to `minimum` value, and the `Maximum random delay` set to `(maximum - minimum)` value.
:::
