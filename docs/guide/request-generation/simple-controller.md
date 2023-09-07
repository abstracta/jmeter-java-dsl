### Organization

Simple controllers offer a way of organizing your test plan into individual modules, 
controllers, assertions, samplers etc. can all be added, even more Simple Controllers for further organization,
simple controllers will run each child sequentially from top to bottom

Look at the bellow example of how to use a `simpleController`:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws Exception {
        TestPlanStats testPlanStats = testPlan(
                threadGroup(1, 1,
                        simpleController("Simple Controller",
                                httpSampler(wiremockUri),
                                dummySampler("Test")
                        )
                )
        ).run();
    }

}
```

Check [DslSimpleController](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/DslSimpleController.java) and [JMeter Component documentation](https://jmeter.apache.org/usermanual/component_reference.html#Simple_Controller) for more details.
