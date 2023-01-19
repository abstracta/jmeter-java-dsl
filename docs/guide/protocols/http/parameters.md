#### Parameters

In many cases, you will need to specify some URL query string parameters or URL encoded form bodies. For these cases, you can use `param` method as in the following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void test() throws Exception {
    String baseUrl = "https://myservice.com/products";
    testPlan(
        threadGroup(1, 1,
            // GET https://myservice.com/products?name=iron+chair
            httpSampler("GetIronChair", baseUrl) 
                .param("name", "iron chair"),
            /*
             * POST https://myservice.com/products
             * Content-Type: application/x-www-form-urlencoded
             * 
             * name=wooden+chair
             */
            httpSampler("CreateWoodenChair", baseUrl)
                .method(HTTPConstants.POST) // POST 
                .param("name", "wooden chair")
            )
    ).run();
  }

}
```

::: tip
JMeter automatically URL encodes parameters, so you don't need to worry about special characters in parameter names or values.

If you want to use some custom encoding or have an already encoded value that you want to use, then you can use `encodedParam` method instead which does not apply any encoding to the parameter name or value, and send it as is.
:::
