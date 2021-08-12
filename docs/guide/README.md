---
sidebar: auto

---

# User guide

Here we share some tips and examples on how to use the DSL to tackle common use cases.

Provided examples use [JUnit 5](https://junit.org/junit5/) and [AssertJ](https://joel-costigliola.github.io/assertj/assertj-core-quick-start.html), but you can use other test & assertion libraries.

Explore the DSL in your preferred IDE to discover all available features, and consider reviewing [existing tests](../../jmeter-java-dsl/src/test/java/us/abstracta/jmeter/javadsl) for additional examples.

The DSL currently supports most common used cases, keeping it simple and avoiding investing development effort in features that might not be needed. If you identify any particular scenario (or JMeter feature) that you need and is not currently supported, or easy to use, **please let us know by [creating an issue](https://github.com/abstracta/jmeter-java-dsl/issues)** and we will try to implement it as soon as possible. Usually porting JMeter features is quite fast.

::: tip
If you like this project, **please give it a star ⭐ in [GitHub](https://github.com/abstracta/jmeter-java-dsl)!** This helps the project be more visible, gain relevance and encourages us to invest more effort in new features.
:::

For an intro to JMeter concepts and components you can check [JMeter official documentation](http://jmeter.apache.org/usermanual/get-started.html).

## Setup

To use the DSL just include it in your project:

:::: tabs type:card
::: tab Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl</artifactId>
  <version>0.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: tab Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl:0.19'
```
:::
::::

## Simple HTTP test plan

To generate HTTP requests just use provided `httpSampler`. 

Following example uses 2 threads (concurrent users) which send 10 HTTP GET requests each to `http://my.service`. 

Additionally, it logs collected statistics (response times, status codes, etc.) to a timestamped file (for later analysis if needed) and checks that the response time 99 percentile is less than 5 seconds.

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
      //this is just to log details of each request stats
      jtlWriter("test" + Instant.now().toString().replace(":", "-") + ".jtl")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

Check [JmeterDsl](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/JmeterDsl.java) and [DslHttpSampler](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/DslHttpSampler.java) for additional options.

::: tip
When working with multiple samplers in a test plan, specify their names to easily check their respective statistics.
:::

::: tip
jmeter-java-dsl automatically adds a cookie manager and cache manager for automatic HTTP cookie and caching handling, emulating browsers behavior. If you need to disable them you can use something like this:

```java
testPlan(
  httpCookies().disable(),
  httpCache().disable(),
  threadGroup(2, 10,
    httpSampler("http://my.service")
  )
)
```

Cookies and cache are automatically cleared in each thread iteration.
:::

::: tip
Since JMeter uses [log4j2](https://logging.apache.org/log4j/2.x/), if you want to control logging level or output, you can use something similar this [log4j2.xml](../../jmeter-java-dsl/src/test/resources/log4j2.xml).
:::

## Run test at scale in BlazeMeter

Running a load test from one machine is not always enough, since you are limited to the machine hardware capabilities. Sometimes, is necessary to run the test using a cluster of machines to be able to generate enough load for the system under test.

By including following module as dependency:

:::: tabs type:card
::: tab Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-blazemeter</artifactId>
  <version>0.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: tab Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-blazemeter:0.19'
```
:::
::::

You can easily run a JMeter test plan at scale in [BlazeMeter](https://www.blazemeter.com/) like this:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.blazemeter.BlazeMeterEngine;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws Exception {
    TestPlanStats stats = testPlan(
      // number of threads and iterations are in the end overwritten by BlazeMeter engine settings 
      threadGroup(2, 10,
        httpSampler("http://my.service")
      )
    ).runIn(new BlazeMeterEngine(System.getenv("BZ_TOKEN"))
      .testName("DSL test")
      .totalUsers(500)
      .holdFor(Duration.ofMinutes(10))
      .threadsPerEngine(100)
      .testTimeout(Duration.ofMinutes(20)));
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```
> This test is using `BZ_TOKEN`, a custom environment variable with `<KEY_ID>:<KEY_SECRET>` format, to get the BlazeMeter API authentication credentials.

Note that is as simple as [generating a BlazeMeter authentication token](https://guide.blazemeter.com/hc/en-us/articles/115002213289-BlazeMeter-API-keys-) and adding `.runIn(new BlazeMeterEngine(...))` to any existing jmeter-java-dsl test to get it running at scale in BlazeMeter. 

BlazeMeter will not only allow you to run the test at scale but also provides additional features like the nice real time reporting, historic data tracking, etc. Here is an example of how a test would look like in BlazMeter:

![blazemeter.png](./images/blazemeter.png) 

Check [BlazeMeterEngine](../../jmeter-java-dsl-blazemeter/src/main/java/us/abstracta/jmeter/javadsl/blazemeter/BlazeMeterEngine.java) for details on usage and available settings when running tests in BlazeMeter.

::: tip
In case you want to get debug logs for HTTP calls to BlazeMeter API, you can include following setting to an existing `log4j2.xml` configuration file: 
```xml
<Logger name="us.abstracta.jmeter.javadsl.blazemeter.BlazeMeterClient" level="DEBUG"/>
<Logger name="okhttp3" level="DEBUG"/>
```
:::

::: warning
If you use JSR223 Pre- or Post- processors with Java code (lambdas) instead of strings ([here](#change-sample-result-statuses-with-custom-logic) are some examples), or use one of the HTTP Sampler methods which receive a function as parameter (as in [here](#provide-request-parameters-programmatically-per-request)), then BlazeMeter execution won't work. You can migrate them to use `jsrPreProcessor` with string scripts instead. Check associated methods documentation for more details.
:::

## Test plan debugging

A usual requirement while building a test plan is to be able to debug for potential issues in configuration or behavior of service under test. With jmeter-java-dsl you have several options for this purpose.

### View Results Tree

One option is using provided `resultsTreeVisualizer()` like in following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    testPlan(
      threadGroup(1, 1,
        httpSampler("http://my.service")
      ),
      resultsTreeVisualizer() 
    ).run();
  }

}
```

This will display the JMeter built-in View Results Tree element, which allows you to review request and response contents in addition to collected metrics (spent time, sent & received bytes, etc.) for each request sent to server, in a window like this one:

![View Results Tree](./images/view-results-tree.png)

::: tip
To debug test plans use few iterations and threads to reduce the execution time and ease tracing by having less information to analyze.
:::

::: tip
When adding `resultsTreeVisualizer()` as child of a thread group, it will only display sample results of that thread group. When added as child of a sampler, it will only show sample results for that sampler. You can use this to only review certain sample results in your test plan. 
:::

::: tip
Remove `resultsTreeVisualizer()` from test plans when are no longer needed (when debugging is finished). Leaving them might interfere with unattended test plan execution due to test plan execution not finishing until all visualizers windows are closed.
:::

::: warning
By default, View Results Tree only display last 500 sample results. If you need to display more elements, use provided `resultsLimit(int)` method which allows changing this value. Take into consideration that the more results are shown, the more memory that will require. So use this setting with care.
:::

### Post processor breakpoints

Another alternative is using IDE builtin debugger by adding a `jsr223PostProcessor` with java code and adding a breakpoint to the post processor code. This does not only allow checking sample result information but also JMeter variables and properties values and sampler properties. 

Here is an example screenshot using this approach while debugging with an IDE:

![Post Processor Debugging](./images/post-processor-debugging.png)

::: tip
DSL provides following methods to ease results and variables visualization and debugging: `varsMap()`, `prevMap()`, `prevMetadata()`, `prevMetrics()`, `prevRequest()`, `prevResponse()`. Check [PostProcessorVars](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslJsr223PostProcessor.java) and [Jsr223ScriptVars](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslJsr223TestElement.java) for more details. 
:::

::: tip
Remove such post processors when no longer needed (when debugging is finished). Leaving them would generate errors when loading generated JMX test plan or running test plan in BlazeMeter, in addition to unnecessary processing time and resource usage.  
:::

### Debug JMeter code

You can even add break points to JMeter code in your IDE and debug the code line by line providing the greatest possible detail.

Here is an example screenshot debugging HTTP Sampler:

![JMeter HTTP Sampler Debugging](./images/jmeter-http-sampler-debugging.png)

::: tip
JMeter class in charge of executing threads logic is `org.apache.jmeter.threads.JMeterThread`. You can check classes used by each DSL provided test element by checking the DSL code.
:::


## Flexible threads configuration

jmeter-java-dsl provides two simple ways of creating thread groups which are used in most scenarios:

* specifying threads and number of iterations each thread should execute before ending test plan
* specifying threads and duration for which each thread should execute before test plan ends

This is how they look in code:

```java
threadGroup(10, 20) // 10 threads for 20 iterations each
threadGroup(10, Duration.ofSeconds(20)) // 10 threads for 20 seconds each
```

But, these options are not good when working with many threads or when trying to configure some complex test scenarios (like when doing incremental or peak tests).

When working with many threads, it is advisable to configure a ramp up period, to avoid starting all threads at once affecting performance metrics and generation.

You can easily configure a ramp up with the DSL like this:

```java
threadGroup().rampTo(10, Duration.ofSeconds(5)).holdIterating(20) // ramp to 10 threads for 5 seconds (1 thread every half second) and iterating each thread 20 times
threadGroup().rampToAndHold(10, Duration.ofSeconds(5), Duration.ofSeconds(20)) //similar as above but after ramping up holding execution for 20 seconds
```

Additionally, you can use and combine these same methods to configure more complex scenarios (incremental, peak, and any other types of tests) like following one:

```java
threadGroup()
    .rampToAndHold(10, Duration.ofSeconds(5), Duration.ofSeconds(20))
    .rampToAndHold(100, Duration.ofSeconds(10), Duration.ofSeconds(30))
    .rampTo(200, Duration.ofSeconds(10))
    .rampToAndHold(100, Duration.ofSeconds(10), Duration.ofSeconds(30))
    .rampTo(0, Duration.ofSeconds(5))
```

Which would translate in the following threads' timeline:

![ThreadGroup Chart](./images/complex-thread-group-chart.png)

Check [DslThreadGroup](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslThreadGroup.java) for more details.

::: tip
To visualize threads timeline, for complex thread group configurations like previous one, you can get a chart like previous one by using provided `DslThreadGroup.showThreadsTimeline()` method.
:::

::: tip
If you are a JMeter GUI user, you may even be interested in using provided `TestElement.showInGui()` method, which shows the JMeter test element GUI that could help you understand what will DSL execute in JMeter. You can use this method with any test element generated by the DSL (not just thread groups).

For example, for above test plan you would get a window like the following one:

![UltimateThreadGroup GUI](./images/ultimate-thread-group-gui.png)
:::

::: tip
When using multiple thread groups in a test plan, consider setting a name on them to properly identify associated requests in statistics & jtl results.
:::

## Log requests and responses

The main mechanism provided by JMeter (and jmeter-java-dsl) to get information about generated requests, responses and associated metrics is through the generation of JTL files.

This can be easily achieved in jmeter-java-dsl by using provided `jtlWriter` like in this example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service")
      ),
      jtlWriter("test" + Instant.now().toString().replace(":", "-") + ".jtl")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

By default, `jtlWriter` will write most used information to evaluate performance of tested service. If you want to trace all information of each request you may use `jtlWriter` with `withAllFields(true)` option. Doing this will provide all the information at the cost of additional computation and resources usage (less resources for actual load testing). You can tune which fields to include or not with `jtlWriter` and only log what you need, check [JtlWriter](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/JtlWriter.java) for more details. 

An additional option, specially targeted towards logging sample responses, is `responseFileSaver` which automatically generates a file for each received response. Here is an example:

```java
 import static org.assertj.core.api.Assertions.assertThat;
 import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
 
 import java.io.IOException;
 import java.time.Duration;
 import java.time.Instant;
 import org.eclipse.jetty.http.MimeTypes.Type;
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

Check [ResponseFileSaver](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/ResponseFileSaver.java) for more details.

Finally, if you have more specific needs that are not covered by previous examples, you can use `jsr223PostProcessor` to define you own custom logic like this:

```java
import static org.assertj.core.api.Assertions.assertThat;
 import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
 
 import java.io.IOException;
 import java.time.Duration;
 import java.time.Instant;
 import org.eclipse.jetty.http.MimeTypes.Type;
 import org.junit.jupiter.api.Test;
 import us.abstracta.jmeter.javadsl.core.TestPlanStats;
 
 public class PerformanceTest {
 
   @Test
   public void testPerformance() throws IOException {
     TestPlanStats stats = testPlan(
       threadGroup(2, 10,
         httpSampler("http://my.service")
           .children(jsr223PostProcessor("new File('traceFile') << \"${prev.sampleLabel}>>${prev.responseDataAsString}\\n\""))
       )
     ).run();
     assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
   }
   
 }
```

Check [DslJsr223PostProcessor](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslJsr223PostProcessor.java) for more details.

## Real-time metrics visualization and historic data storage

When running tests with JMeter (and in particular with jmeter-java-dsl) a usual requirement is to be able to store such test runs in a persistent database to later on review such metrics, and compare different test runs. Additionally, jmeter-java-dsl only provides some summary data of test run in the console while it is running, but, since it doesn't provide any sort of UI, doesn't allow to easily analyze such information as it can be done in JMeter GUI.

To overcome these limitations you can use provided support for publishing JMeter test run metrics to [InfluxDB](https://www.influxdata.com/products/influxdb-overview/), which allows keeping record of all run statistics and, through [Grafana](https://grafana.com/), get some nice dashboards like the following one:

![grafana](./influxdb/grafana.png)

This can be easily done using `influxDbListener`, an existing InfluxDB & Grafana server and using a dashboard like [this one](https://grafana.com/grafana/dashboards/4026).

Here is an example test plan:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service")
      ),
      influxDbListener("http://localhost:8086/write?db=jmeter")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

If you want to try it locally you can run `docker-compose up` (previously [installing Docker](https://docs.docker.com/get-docker/) in you machine) inside [this directory](../../docs/guide/influxdb). After containers are started, you can add previously mentioned Graphana dashboard in your Graphana at ([http://localhost:3000](http://localhost:3000)). Finally, run a performance test using the `influxDbListener` and you will be able to see the live results, and keep historic data. Cool, isn't it?!

::: warning
Use provided `docker-compose` settings for local tests only. It uses weak credentials and is not properly configured for production purposes.
:::

Check [InfluxDbBackendListener](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/InfluxDbBackendListener.java) for additional details and settings.

## Generate HTML reports from test plan execution

After running a test plan you would usually like to visualize the results in friendly way that eases analysis of collected information.

One, and preferred way, to do that is through [previously mentioned alternative](#real-time-metrics-visualization-and-historic-data-storage).

Another way, might just be using [previously introduced](#simple-http-test-plan) `jtlWriter` and then loading the jtl file in JMeter GUI with one of JMeter provided listeners (like view results tree, summary report, etc.).

Another alternative is generating a standalone report for the test plan execution using jmeter-java-dsl provided `htmlReporter` like this:

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
      htmlReporter("html-report-" + Instant.now().toString().replace(":", "-"))
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

::: warning
`htmlReporter` will throw an exception if provided directory path is a nonempty directory or file
:::

## Check for expected response

By default, JMeter marks any HTTP request with a fail response code (4xx or 5xx) as failed, which allows you to easily identify when some request unexpectedly fails. But in many cases this is not enough or desirable, and you need to check for response body (or some other field) to contain (or not) certain string. 

This is usually accomplished in JMeter with the usage of Response Assertions, which provide an easy and fast way to verify that you get the proper response for each step of the test plan, marking the request as failure when specified condition is not met. 

Here is an example on how to specify a response assertion in jmeter-java-dsl:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service")
          .children(
            responseAssertion().containsSubstrings("OK")
          )
      )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [Response Assertion](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/assertions/DslResponseAssertion.java) for more details and additional options.

For more complex scenarios check [following section](#change-sample-result-statuses-with-custom-logic).

## Change sample result statuses with custom logic

Sometimes [response assertions](#check-for-expected-response) and JMeter default behavior are not enough, and custom logic is required. In such scenarios you can use `jsr223PostProcessor` as in this example where 429 status code is not considered as a fail status code:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service")
          .children(
            jsr223PostProcessor("if (prev.responseCode == '429') { prev.successful = true }")
          )
      )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

You can also use a Java lambda instead of providing Groovy script, which benefits from Java type safety & IDEs code auto-completion:

```java
jsr223PostProcessor(s -> { 
  if ("429".equals(s.prev.getResponseCode())) { 
    s.prev.setSuccessful(true); 
  } 
})
```

::: warning
Using this last approach is currently only supported when using embedded JMeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with BlazeMeter).
:::

Check [DslJsr223PostProcessor](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslJsr223PostProcessor.java) for more details and additional options.

::: warning
JSR223PostProcessor is a very powerful tool, but is not the only, nor the best, alternative for many cases where JMeter already provides a better and simpler alternative. For instance, previously mentioned might be implemented with previously presented [Response Assertion](#check-for-expected-response).
:::

## Use part of a response in a following request (aka: correlation)

It is a usual requirement while creating a test plan for an application to be able to use part of a response (e.g.: a generated ID, token, etc.) in a subsequent request. This can be easily achieved using JMeter extractors and variables. Here is an example with jmeter-java-dsl:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service/accounts")
          .post("{\"name\": \"John Doe\"}", Type.APPLICATION_JSON)
          .children(
            regexExtractor("ACCOUNT_ID", "\"id\":\"([^\"]+)\"")
          ),
        httpSampler("http://my.service/accounts/${ACCOUNT_ID}")
      )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [DslRegexExtractor](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslRegexExtractor.java) for more details and additional options.

## Provide Request Parameters Programmatically per Request

With the standard DSL you can provide static values to request parameters, such as a body. However, you may also want to be able to modify your requests for each call. This is common in cases where your request creates something that must have unique values.

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.apache.jmeter.threads.JMeterVariables;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service")
          .post("${REQUEST_BODY}", Type.TEXT_PLAIN)
          .children(
            jsr223PreProcessor("vars.put('REQUEST_BODY', " + getClass().getName()+ ".buildRequestBody(vars))")
          )
      )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

  public static String buildRequestBody(JMeterVariables vars) {
    String countVarName = "REQUEST_COUNT";
    Integer countVar = (Integer) vars.getObject(countVarName);
    int count = countVar != null ? countVar + 1 : 1;
    vars.putObject(countVarName, count);
    return "MyBody" + count;
  }

}
```

You can also use a Java lambda instead of providing Groovy script, which benefits from Java type safety & IDEs code auto-completion:

```java
jsr223PreProcessor(s -> s.vars.put("REQUEST_BODY", buildRequestBody(s.vars)))
```

Or even use this shorthand:

```java
post(s -> buildRequestBody(s.vars), Type.TEXT_PLAIN)
```

::: warning
Using java code (lambdas) will only work with embedded JMeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with BlazeMeter). Use the first option to avoid such limitations.
:::

Check [DslJsr223PreProcessor](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/preprocessors/DslJsr223PreProcessor.java) & [DslHttpSampler](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/DslHttpSampler.java) for more details and additional options.

## Group requests

Sometimes, is necessary to be able to group requests which constitute different steps in a test. For example, separate requests necessary to do a login from the ones used to add items to cart, from the ones that do a purchase. JMeter (and the DSL) provide Transaction Controllers for this purpose, here is an example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;

public class SaveTestPlanAsJMX {

  @Test
  public void testTransactions() throws IOException {
    testPlan(
      threadGroup(2, 10,
        transaction('login',
          httpSampler("http://my.service"), 
          httpSampler("http://my.service/login")
            .post("user=test&password=test", Type.FORM_ENCODED)
        ), 
        transaction('addItemToCart',
          httpSampler("http://my.service/items"),
          httpSampler("http://my.service/cart/items")
            .post("{\"id\": 1}", Type.APPLICATION_JSON)  
        )
      )
    ).run();
  }
  
}
```

This will provide additional sample results for each transaction, which contain the aggregate metrics for containing requests, allowing to focus on the actual flow steps instead of each particular request.

## Emulate user delays between requests

Sometimes, is necessary to replicate users' behavior on the test plan, adding a timer between requests is one of the most used practices. For example, simulate the time it will take to complete a purchase form. JMeter (and the DSL) provide Uniform Random Timer for this purpose. Here is an example that adds a delay between four and ten seconds:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void testTransactions() throws IOException {
    testPlan(
      threadGroup(2, 10,
        transaction('addItemToCart',
          httpSampler("http://my.service/items"),
          httpSampler("http://my.service/cart/items")
            .post("{\"id\": 1}", Type.APPLICATION_JSON)  
        ),
        transaction('chekcout',
          httpSampler("http://my.service/cart/chekout"),
          uniformRandomTimer(4000, 10000),
          httpSampler("http://my.service/cart/checkout/userinfo")
              .post("{\"Name\": Dave, \"lastname\": Tester, \"Street\": 1483  Smith Road, \"City\": Atlanta}", Type.APPLICATION_JSON)
        )
      )
    ).run();
  }
  
}
```

## Save as JMX

In case you want to load a test plan in JMeter GUI, you can save it just invoking `saveAsJMX` method in the test plan as in following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class SaveTestPlanAsJMX {
  
  public static void main(String[] args) throws Exception {
    testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service")
      )
    ).saveAsJmx("dsl-test-plan.jmx");
  }
  
}
```

This can be helpful to share a Java DSL defined test plan with people not used to the DSL, or to use some JMeter feature (or plugin) that is not yet supported by the DSL (**but, we strongly encourage you to report it as an issue [here](https://github.com/abstracta/jmeter-java-dsl/issues)** so we can include such support into the DSL for the rest of the community).

::: warning
Take into consideration that currently there is no automatic way to migrate changes done in JMX to the Java DSL.
:::

::: warning
If you use JSR223 Pre- or Post- processors with Java code (lambdas) instead of strings, or use one of the HTTP Sampler methods which receive a function as parameter, then the exported JMX will not work in JMeter GUI. You can migrate them to use jsrPreProcessor with string scripts instead.
:::

## Run JMX file

jmeter-java-dsl also provides means to easily run a test plan from a JMX file either locally or in BlazeMeter (through [previously mentioned jmeter-java-dsl-blazemeter module](#run-test-at-scale-in-blazemeter)). Here is an example:

```java
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class RunJmxTestPlan {
  
  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = DslTestPlan.fromJmx("test-plan.jmx").run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
``` 

This can be used to just run existing JMX files, or when DSL has no support for some JMeter functionality or plugin, and you need to use JMeter GUI to build the test plan but still want to use jmeter-java-dsl to run the test plan embedded in Java test or code.

::: tip
When the JMX uses some custom plugin or JMeter protocol support, you might need to add required dependencies to be able to run the test in an embedded engine. For example, when running a TN3270 JMX test plan using RTE plugin you will need to add following repository and dependencies:
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
   ...
   <dependency>
     <groupId>com.github.Blazemeter</groupId>
     <artifactId>RTEPlugin</artifactId>
     <version>3.1</version>
     <scope>test</scope>
   </dependency>
   <dependency>
     <groupId>com.github.Blazemeter</groupId>
     <artifactId>dm3270</artifactId>
     <version>0.12.3-lib</version>
     <scope>test</scope>
   </dependency>
</dependencies>
```
:::
