#### Execute only once in thread

In some cases, you only need to run part of a test plan once. For these need, you can use `onceOnlyController`. This controller will execute a part of the test plan only one time on the first iteration of each thread (using [JMeter Once Only Controller Component](https://jmeter.apache.org/usermanual/component_reference.html#Once_Only_Controller)).

You can use this, for example, for one-time authorization or for setting JMeter variables or properties.

Here is an example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;

public class DslOnceOnlyControllerTest extends JmeterDslTest {

  @Test
  public void shouldExecuteOnlyOneTimeWhenOnceOnlyControllerInPlan() throws Exception {
    testPlan(
        threadGroup(1, 10,
            onceOnlyController(
                httpSampler("http://my.service/login") // only runs once
                    .method(HTTPConstants.POST)
                    .header("Authorization", "Basic asdf=")
                    .children(
                        regexExtractor("AUTH_TOKEN", "authToken=(.*)")
                    )
            ),
            httpSampler("http://my.service/accounts") // runs ten times
                .header("Authorization", "Bearer ${AUTH_TOKEN}")
        )
    ).run();
  }

}
```

Check [DslOnceOnlyController](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/DslOnceOnlyController.java) for more details.
