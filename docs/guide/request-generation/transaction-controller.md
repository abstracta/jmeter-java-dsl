### Group requests

Sometimes, is necessary to be able to group requests which constitute different steps in a test. For example, to separate necessary requests to do a login from the ones used to add items to the cart and the ones to do a purchase. JMeter (and the DSL) provide Transaction Controllers for this purpose, here is an example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;

public class SaveTestPlanAsJMX {

  @Test
  public void testTransactions() throws IOException {
    testPlan(
        threadGroup(2, 10,
            transaction('login',
                httpSampler("http://my.service"),
                httpSampler("http://my.service/login")
                    .post("user=test&password=test", ContentType.APPLICATION_FORM_URLENCODED)
            ),
            transaction('addItemToCart',
                httpSampler("http://my.service/items"),
                httpSampler("http://my.service/cart/items")
                    .post("{\"id\": 1}", ContentType.APPLICATION_JSON)
            )
        )
    ).run();
  }

}
```

This will provide additional sample results for each transaction, which contain the aggregate metrics for containing requests, allowing you to focus on the actual flow steps instead of each particular request.
