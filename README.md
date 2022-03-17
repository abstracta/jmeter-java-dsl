![logo](/docs/.vuepress/public/logo.svg)

Simple Java API to run performance tests, using [JMeter] as engine, in a Git and programmers friendly way.

If you like this project, **please give it a star :star:!** This helps the project be more visible, gain relevance and encourages us to invest more effort in new features.

## Usage

If you use [maven](https://maven.apache.org/what-is-maven.html), just include following dependency:

```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl</artifactId>
  <version>0.49.2</version>
  <scope>test</scope>
</dependency>
``` 

Here is a simple example test in [JUnit 5](https://junit.org/junit5/)+ with 2 threads/users iterating 10 times each to send HTTP POST requests with a JSON body to `http://my.service`:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service")
          .post("{\"name\": \"test\"}", ContentType.APPLICATION_JSON)
      ),
      //this is just to log details of each request stats
      jtlWriter("test" + Instant.now().toString().replace(":", "-") + ".jtl")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

> This example also uses [AssertJ](https://joel-costigliola.github.io/assertj/assertj-core-quick-start.html) for assertions, but you can use whatever assertion library you chose.

More examples can be found in [tests](jmeter-java-dsl/src/test/java/us/abstracta/jmeter/javadsl)

[Here](https://github.com/abstracta/jmeter-java-dsl-sample) is a sample project for reference or for starting new projects from scratch.

> **Tip 1:** Check [jmx2dsl](https://abstracta.github.io/jmeter-java-dsl/guide/#dsl-code-generation-from-jmx-file) provided tool, which allows easy conversion of JMX files to JMeter DSL code.
> 
> **Tip 2:** Since JMeter uses [log4j2](https://logging.apache.org/log4j/2.x/), if you want to control logging level or output, you can use something similar to the tests included [log4j2.xml](jmeter-java-dsl/src/test/resources/log4j2.xml).
>
> **Tip 3:** When working with multiple samplers in a test plan, specify their names to easily check their respective statistics.

**Check [here](https://abstracta.github.io/jmeter-java-dsl/) for details on some interesting use cases**, like running tests at scale in [BlazeMeter](https://www.blazemeter.com/), saving and loading test plans from JMX, publishing test metrics to [InfluxDB](https://www.influxdata.com/products/influxdb-overview/) (and visualizing them from [Grafana](https://grafana.com/)), and general usage guides.

## Why?

Check more about the motivation and analysis of alternatives [here](https://abstracta.github.io/jmeter-java-dsl/motivation/)

## Contributing & Requesting features

Currently, the project only covers the basic, but most used, features when implementing JMeter performance tests. 
The idea is to evaluate if the community (you) is interested in using the library, and if so, implement new features as the community request them, covering at some point most of JMeter (and plugins) features. 
In order to accomplish this, we need you to **please create an issue for any particular feature or need that you have**.

We would also really appreciate pull requests. Check the [CONTRIBUTING](CONTRIBUTING.md) guide for an explanation of main library components and how can you extend the library.

[JMeter]: http://jmeter.apache.org/
