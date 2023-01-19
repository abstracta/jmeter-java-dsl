## Test resources

When working with tests in maven projects, even gradle in some scenarios, it is usually necessary to use files hosted in `src/test/resources`. For example CSV files for `csvDataSet`, a file to be used by an `httpSampler`, some JSON for comparison, etc. The DSL provides `testResource` as a handy shortcut for such scenarios. Here is a simple example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testProperties() throws IOException {
    testPlan(
        csvDataSet(testResource("users.csv")), // gets users info from src/test/resources/users.csv
        threadGroup(1, 1,
            httpSampler("http://myservice.test/users/${USER_ID}")
        )
    ).run();
  }

}
```

Check [TestResource](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/util/TestResource.java) for some further details.

