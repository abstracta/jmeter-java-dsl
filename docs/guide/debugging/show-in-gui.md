### Test plan review un JMeter GUI

A usual requirement for new DSL users that are used to Jmeter GUI, is to be able to review Jmeter DSL generated test plan in the familiar JMeter GUI. For this, you can use `showInGui()` method in a test plan to open JMeter GUI with the preloaded test plan.

This can be also used to debug the test plan, by adding elements (like view results tree, dummy samplers, debug post-processors, etc.) in the GUI and running the test plan.

Here is a simple example using the method:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service")
        )
    ).showInGui();
  }

}
```

Which ends up opening a window like this one:

![Test plan in JMeter GUI](./images/test-plan-gui.png)