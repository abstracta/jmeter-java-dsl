### Log requests and responses

The main mechanism provided by JMeter (and jmeter-java-dsl) to get information about generated requests, responses, and associated metrics is through the generation of JTL files.

This can be easily achieved in jmeter-java-dsl by using provided `jtlWriter` like in this example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service")
        ),
        jtlWriter("target/jtls")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: tip
By default, `jtlWriter` will write the most used information to evaluate the performance of the tested service. If you want to trace all the information of each request you may use `jtlWriter` with `withAllFields()` option. Doing this will provide all the information at the cost of additional computation and resource usage (fewer resources for actual load testing). You can tune which fields to include or not with `jtlWriter` and only log what you need, check [JtlWriter](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/JtlWriter.java) for more details.
:::

::: tip
By default, `jtlWriter` will log every sample result, but in some cases you might want to log additional info when a sample result fails. In such scenarios you can use two `jtlWriter` instances like in this example:

```java
testPlan(
    threadGroup(2, 10,
        httpSampler("http://my.service")
    ),
    jtlWriter("target/jtls/success")
      .logOnly(SampleStatus.SUCCESS),
    jtlWriter("target/jtls/error")
      .logOnly(SampleStatus.ERROR)
      .withAllFields(true)
)
```
:::

::: tip
`jtlWriter` will automatically generate `.jtl` files applying this format: `<yyyy-MM-dd HH-mm-ss> <UUID>.jtl`.

If you need a specific file name, for example for later postprocessing logic (eg: using CI build ID), you can specify it by using `jtlWriter(directory, fileName)`.

When specifying the file name, make sure to use unique names, otherwise, the JTL contents may be appended to previous existing jtl files.
:::

An additional option, specially targeted towards logging sample responses, is `responseFileSaver` which automatically generates a file for each received response. Here is an example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service")
        ),
        responseFileSaver(Instant.now().toString().replace(":", "-") + "-response")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [ResponseFileSaver](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/ResponseFileSaver.java) for more details.

Finally, if you have more specific needs that are not covered by previous examples, you can use `jsr223PostProcessor` to define your own custom logic like this:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service")
                .children(jsr223PostProcessor(
                    "new File('traceFile') << \"${prev.sampleLabel}>>${prev.responseDataAsString}\\n\""))
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [DslJsr223PostProcessor](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslJsr223PostProcessor.java) for more details.
