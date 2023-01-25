#### HTTP defaults

Whenever you need to use some repetitive value or common setting among HTTP samplers (and any part of the test plan) the preferred way (due to readability, debugability, traceability, and in some cases simplicity) is to create a Java variable or custom builder method.

For example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

public class PerformanceTest {

  @Test
  public void performanceTest() throws IOException {
    String host = "myservice.my";
    testPlan(
        threadGroup(10, 100,
            productCreatorSampler(host, "Rubber"),
            productCreatorSampler(host, "Pencil")
        )
    ).run();
  }

  private DslHttpSampler productCreatorSampler(String host, String productName) {
    return httpSampler("https://" + host + "/api/product")
        .post("{\"name\": \"" + productName + "\"}", ContentType.APPLICATION_JSON);
  }

}
```

In some cases though, it might be simpler to just use provided `httpDefaults` method, like in the following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void performanceTest() throws IOException {
    testPlan(
        httpDefaults()
            .url("https://myservice.my")
            .downloadEmbeddedResources(),
        threadGroup(10, 100,
            httpSampler("/products"),
            httpSampler("/cart")
        )
    ).run();
  }

}
```

Check [DslHttpDefaults](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/DslHttpDefaults.java) for additional details on available default options.
