### Group requests

Sometimes, is necessary to be able to group requests which constitute different steps in a test. For example, to separate necessary requests to do a login from the ones used to add items to the cart and the ones to do a purchase. JMeter (and the DSL) provide Transaction Controllers for this purpose, here is an example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

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

If you don't want to generate additional sample results (and statistics), and want to group requests for example to apply a given timer, config, assertion, listener, pre- or post-processor, then you can use `simpleController` like in following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testTransactions() throws IOException {
    testPlan(
        threadGroup(2, 10,
            simpleController('login',
                httpSampler("http://my.service"),
                httpSampler("http://my.service/users"),
                responseAssertion()
                  .containsSubstrings("OK")
            )
        )
    ).run();
  }

}
```

You can even use `transactionController` and `simpleController` to easily modularize parts of your test plan into Java methods (or classes) like in this example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.controllers.DslTransactionController;

public class PerformanceTest {

  private DslTransactionController login(String baseUrl) {
    return transaction("login",
        httpSampler(baseUrl),
        httpSampler(baseUrl + "/login")
            .post("user=test&password=test", ContentType.APPLICATION_FORM_URLENCODED)
    );
  }

  private DslTransactionController addItemToCart(String baseUrl) {
    return transaction("addItemToCart",
        httpSampler(baseUrl + "/items"),
        httpSampler(baseUrl + "/cart/items")
            .post("{\"id\": 1}", ContentType.APPLICATION_JSON)
    );
  }

  @Test
  public void testTransactions() throws IOException {
    String baseUrl = "http://my.service";
    testPlan(
        threadGroup(2, 10,
            login(baseUrl),
            addItemToCart(baseUrl)
        )
    ).run();
  }

}
```