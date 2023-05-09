### JMeter remote testing

JMeter already provides means to run a test on several machines controlled by one master/client machine. This is referred as [Remote Testing](http://jmeter.apache.org/usermanual/remote-test.html).

JMeter remote testing requires setting up nodes in server/slave mode (using `bin/jmeter-server` JMeter script) with a configured keystore (usually `rmi_keystore.jks`, generated with `bin/` JMeter script) which will execute a test plan triggered in a client/master node.

You can trigger such tests with the DSL using `DistributedJmeterEngine` as in the following example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.engines.DistributedJmeterEngine;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(200, Duration.ofMinutes(10),
            httpSampler("http://my.service")
        )
    ).runIn(new DistributedJmeterEngine("host1", "host2"));
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

This will run 200 users for 10 minutes on each server/slave (`host1` and `host2`) and aggregate all the results in returned stats.

::: warning
To be able to run the test you require the `rmi_keystore.jks` file in the working directory of the test. For the time being, we couldn't find a way to allow setting any arbitrary path for the file.
:::

::: warning
In general, prefer using BlazeMeter, OctoPerf or Azure options which avoid all the setup and maintenance costs of the infrastructure required by JMeter remote testing, also benefiting from other additional useful features they provide (like reporting capabilities).
:::

::: tip
[Here](/docs/guide/scale/distributed) is an example project using `docker-compose` that starts a JMeter server/slave and executes a test with it. If you want to do a similar setup, generate your own keystore and properly tune RMI remote server in server/slave.
:::

Check [DistributedJmeterEngine](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/engines/DistributedJmeterEngine.java) and [JMeter documentation](http://jmeter.apache.org/usermanual/remote-test.html) for proper setup and additional options.

