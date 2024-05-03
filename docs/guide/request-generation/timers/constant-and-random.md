#### Emulate user delays between requests

Sometimes, is necessary to be able to properly replicate users' behavior, and in particular the time the users take between sending one request and the following one. For example, to simulate the time it will take to complete a purchase form. JMeter (and the DSL) provide a few alternatives for this.

If you just want to add 1 pause between two requests, you can use the `threadPause` method like in the following example:

```java
import java.io.IOException;
import java.time.Duration;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void test() throws IOException {
    testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service/items"),
            threadPause(Duration.ofSeconds(4)),
            httpSampler("http://my.service/cart/selected-items")
                .post("{\"id\": 1}", ContentType.APPLICATION_JSON)
        )
    ).run();
  }

}
```

Using `threadPause` is a good solution for adding individual pauses, but if you want to add pauses across several requests, or sections of test plan, then using a `constantTimer` or `uniformRandomTimer` is better. Here is an example that adds a delay of between 4 and 10 seconds for every request in the test plan:

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
            uniformRandomTimer(Duration.ofSeconds(4), Duration.ofSeconds(10)),
            transaction("addItemToCart",
                httpSampler("http://my.service/items"),
                httpSampler("http://my.service/cart/selected-items")
                    .post("{\"id\": 1}", ContentType.APPLICATION_JSON)
            ),
            transaction("checkout",
                httpSampler("http://my.service/cart/chekout"),
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

::: tip
As you may have noticed, timer order in relation to samplers, doesn't matter. Timers apply to all samplers in their scope, adding a pause after pre-processor executions and before the actual sampling. 
`threadPause` order, on the other hand, is relevant, and the pause will only execute when previous samplers in the same scope have run and before following samplers do.
:::

::: warning
`uniformRandomTimer` `minimum` and `maximum` parameters differ from the ones used by JMeter Uniform Random Timer element, to make it simpler for users with no JMeter background.

The generated JMeter test element uses the `Constant Delay Offset` set to `minimum` value, and the `Maximum random delay` set to `(maximum - minimum)` value.
:::
