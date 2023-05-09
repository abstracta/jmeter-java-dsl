### Java API performance testing

Sometimes JMeter provided samplers are not enough for testing a particular technology, custom code, or service that requires some custom code to interact with. For these cases, you might use `jsr223Sampler` which allows you to use custom logic to generate a sample result.

Here is an example for load testing a Redis server:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class TestRedis {

  @Test
  public void shouldGetExpectedSampleResultWhenJsr223SamplerWithLambdaAndCustomResponse()
      throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            jsr223Sampler("import redis.clients.jedis.Jedis\n"
                + "Jedis jedis = new Jedis('localhost', 6379)\n"
                + "jedis.connect()\n"
                + "SampleResult.connectEnd()\n"
                + "jedis.set('foo', 'bar')\n"
                + "return jedis.get(\"foo\")")
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofMillis(500));
  }

}
```

::::: tip
Remember to add any particular dependencies required by your code. For example, the above example requires this dependency:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>redis.clients</groupId>
  <artifactId>jedis</artifactId>
  <version>3.6.0</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'redis.clients:jedis:3.6.0'
```
:::
::::
:::::

You can also use Java lambdas instead of Groovy script to take advantage of IDEs auto-completion and Java type safety:

```java
jsr223Sampler(v -> {
    SampleResult result = v.sampleResult;
    Jedis jedis = new Jedis("localhost", 6379);
    jedis.connect();
    result.connectEnd();
    jedis.set("foo", "bar");
    result.setResponseData(jedis.get("foo"), StandardCharsets.UTF_8.name());
})
```

::: warning
Using java code (lambdas) will only work with embedded JMeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with BlazeMeter, OctoPerf or Azure). Use the first option to avoid such limitations.
:::

`jsr223Sampler` is very powerful, but also makes code and test plans harder to maintain (as with any custom code) compared to using JMeter built-in samplers. So, in general, prefer using JMeter-provided samplers if they are enough for the task at hand, and use `jsr223Sampler` sparingly.

Check [DslJsr223Sampler](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/java/DslJsr223Sampler.java) for more details and additional options.
