#### Overriding URL protocol, host or port

In some cases, you might want to use a default base URL but some particular requests may require some part of the URL to be different (eg: protocol, host, or port).

The preferred way (due to maintainability, language & IDE provided features, traceability, etc) of doing this, as with defaults, is using java code. Eg:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void test() throws Exception {
    String protocol = "https://";
    String host = "myservice.com";
    String baseUrl = protocol + host;
    testPlan(
        threadGroup(1, 1,
            httpSampler(baseUrl + "/products"),
            httpSampler(protocol + "api." + host + "/cart"),
            httpSampler(baseUrl + "/stores")
        )
    ).run();
  }

}
```

But in some cases, this might be too verbose, or unnatural for users with existing JMeter knowledge. In such cases you can use provided methods (`protocol`, `host` & `port`) to just specify the part you want to modify for the sampler like in the following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void test() throws Exception {
    testPlan(
        threadGroup(1, 1,
            httpDefaults()
                .url("https://myservice.com"),
            httpSampler("/products"),
            httpSampler("/cart")
                .host("subDomain.myservice.com"),
            httpSampler("/stores")
        )
    ).run();
  }

}
```
