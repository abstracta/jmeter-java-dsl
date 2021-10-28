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
  <version>0.28</version>
  <scope>test</scope>
</dependency>
```
:::
::: tab Gradle
```groovy
// this is required due to JMeter open issue: https://bz.apache.org/bugzilla/show_bug.cgi?id=64465
@CacheableRule
class JMeterRule implements ComponentMetadataRule {
    void execute(ComponentMetadataContext context) {
        context.details.allVariants {
            withDependencies {
                removeAll { it.group == "org.apache.jmeter" && it.name == "bom" }
            }
        }
    }
}

dependencies {
    ...
    testImplementation 'us.abstracta.jmeter:jmeter-java-dsl:0.28'
    components {
        withModule("org.apache.jmeter:ApacheJMeter_core", JMeterRule)
        withModule("org.apache.jmeter:ApacheJMeter_java", JMeterRule)
        withModule("org.apache.jmeter:ApacheJMeter", JMeterRule)
        withModule("org.apache.jmeter:ApacheJMeter_http", JMeterRule)
        withModule("org.apache.jmeter:ApacheJMeter_functions", JMeterRule)
        withModule("org.apache.jmeter:ApacheJMeter_components", JMeterRule)
        withModule("org.apache.jmeter:ApacheJMeter_config", JMeterRule)
        withModule("org.apache.jmeter:jorphan", JMeterRule)
    }
}
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

::: tip
When working with multiple samplers in a test plan, specify their names to easily check their respective statistics.
:::

::: tip
Since JMeter uses [log4j2](https://logging.apache.org/log4j/2.x/), if you want to control logging level or output, you can use something similar to this [log4j2.xml](../../jmeter-java-dsl/src/test/resources/log4j2.xml).
:::

Check [HTTP performance testing](#http-performance-testing) for additional details while testing HTTP services.

## Run test at scale in BlazeMeter

Running a load test from one machine is not always enough, since you are limited to the machine hardware capabilities. Sometimes, is necessary to run the test using a cluster of machines to be able to generate enough load for the system under test.

By including following module as dependency:

:::: tabs type:card
::: tab Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-blazemeter</artifactId>
  <version>0.28</version>
  <scope>test</scope>
</dependency>
```
:::
::: tab Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-blazemeter:0.28'
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

BlazeMeter will not only allow you to run the test at scale but also provides additional features like the nice real time reporting, historic data tracking, etc. Here is an example of how a test would look like in BlazeMeter:

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

## Advanced threads configuration

jmeter-java-dsl provides two simple ways of creating thread groups which are used in most scenarios:

* specifying threads and number of iterations each thread should execute before ending test plan
* specifying threads and duration for which each thread should execute before test plan ends

This is how they look in code:

```java
threadGroup(10, 20) // 10 threads for 20 iterations each
threadGroup(10, Duration.ofSeconds(20)) // 10 threads for 20 seconds each
```

But these options are not good when working with many threads or when trying to configure some complex test scenarios (like when doing incremental or peak tests).

### Thread ramps and holds

When working with many threads, it is advisable to configure a ramp up period, to avoid starting all threads at once affecting performance metrics and generation.

You can easily configure a ramp up with the DSL like this:

```java
threadGroup().rampTo(10, Duration.ofSeconds(5)).holdIterating(20) // ramp to 10 threads for 5 seconds (1 thread every half second) and iterating each thread 20 times
threadGroup().rampToAndHold(10, Duration.ofSeconds(5), Duration.ofSeconds(20)) //similar as above but after ramping up holding execution for 20 seconds
```

Additionally, you can use and combine these same methods to configure more complex scenarios (incremental, peak, and any other types of tests) like the following one:

```java
threadGroup()
  .rampToAndHold(10, Duration.ofSeconds(5), Duration.ofSeconds(20))
  .rampToAndHold(100, Duration.ofSeconds(10), Duration.ofSeconds(30))
  .rampTo(200, Duration.ofSeconds(10))
  .rampToAndHold(100, Duration.ofSeconds(10), Duration.ofSeconds(30))
  .rampTo(0, Duration.ofSeconds(5))
  .children(
    httpSampler("http://my.service")
  )
```

Which would translate in the following threads' timeline:

![Thread Group Timeline](./images/ultimate-thread-group-timeline.png)

Check [DslThreadGroup](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/DslThreadGroup.java) for more details.

::: tip
To visualize threads timeline, for complex thread group configurations like previous one, you can get a chart like previous one by using provided `DslThreadGroup.showTimeline()` method.
:::

::: tip
If you are a JMeter GUI user, you may even be interested in using provided `TestElement.showInGui()` method, which shows the JMeter test element GUI that could help you understand what will DSL execute in JMeter. You can use this method with any test element generated by the DSL (not just thread groups).

For example, for above test plan you would get a window like the following one:

![UltimateThreadGroup GUI](./images/ultimate-thread-group-gui.png)
:::

::: tip
When using multiple thread groups in a test plan, consider setting a name on them to properly identify associated requests in statistics & jtl results.
:::

### Throughput based thread group

Sometimes you want to focus just on the number of requests per second to generate and don't want to be concerned about how many concurrent threads/users, and pauses between requests, are needed. For these scenarios you can use `rpsThreadGroup` like in following example:

```java
rpsThreadGroup()
  .maxThreads(500)
  .rampTo(20, Duration.ofSeconds(10))
  .rampTo(10, Duration.ofSeconds(10))
  .rampToAndHold(1000, Duration.ofSeconds(5), Duration.ofSeconds(10))
  .children(
    httpSampler("http://my.service")
  )
```

This will internally use JMeter [Concurrency Thread Group](https://jmeter-plugins.org/wiki/ConcurrencyThreadGroup/) element in combination with [Throughput
Shaping Time](https://jmeter-plugins.org/wiki/ThroughputShapingTimer/).

::: tip
`rpsThreadGroup` will dynamically create and remove threads and add delays between requests to match the traffic to the expected RPS. You can also specify to control iterations per second (number of times the flow in the thread group runs per second) instead of threads by using `.counting(RpsThreadGroup.EventType.ITERATIONS)`.
:::

::: warning
When no `maxThreads` are specified, `rpsThreadGroup` will use as many threads as needed. In such scenarios, you might end with unexpected number of threads with associated CPU and Memory requirements, which may affect the performance test metrics. **You should always set maximum threads to use** to avoid such scenarios.

You can use following formula to calculate a value for `maxThreads`: `T*R`, being `T` the maximum RPS that you want to achieve and `R` the maximum expected response time (or iteration time if you use `.counting(RpsThreadGroup.EventType.ITERATIONS)`) in seconds.
:::

::: tip
As with default thread group, with `rpsThreadGroup` you can use `showTimeline` to get a chart of configured RPS profile for easy visualization. An example chart:

![RPS Thread Group Timeline](./images/rps-thread-group-timeline.png)
:::

Check [RpsThreadGroup](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/threadgroups/RpsThreadGroup.java) for more details.

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
**Remove `resultsTreeVisualizer()` from test plans when are no longer needed** (when debugging is finished). Leaving them might interfere with unattended test plan execution due to test plan execution not finishing until all visualizers windows are closed.
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


## Reporting

Once you have a test plan you would usually want to be able to analyze collected information. In this section we show you several ways to achieve this.

### Log requests and responses

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

By default, `jtlWriter` will write most used information to evaluate performance of tested service. If you want to trace all the information of each request you may use `jtlWriter` with `withAllFields(true)` option. Doing this will provide all the information at the cost of additional computation and resources usage (less resources for actual load testing). You can tune which fields to include or not with `jtlWriter` and only log what you need, check [JtlWriter](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/JtlWriter.java) for more details. 

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

### Real-time metrics visualization and historic data storage

When running tests with JMeter (and in particular with jmeter-java-dsl) a usual requirement is to be able to store such test runs in a persistent database to later on review such metrics, and compare different test runs. Additionally, jmeter-java-dsl only provides some summary data of test run in the console while it is running, but, since it doesn't provide any sort of UI, doesn't allow to easily analyze such information as it can be done in JMeter GUI.

To overcome these limitations you can use provided support for publishing JMeter test run metrics to [InfluxDB](https://www.influxdata.com/products/influxdb-overview/) or [Elasticsearch](https://www.elastic.co/what-is/elasticsearch), which allows keeping record of all run statistics and, through [Grafana](https://grafana.com/), get some nice dashboards like the following one:

![grafana](./influxdb/grafana.png)

#### InfluxDB

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

If you want to try it locally you can run `docker-compose up` (previously [installing Docker](https://docs.docker.com/get-docker/) in you machine) inside [this directory](../../docs/guide/influxdb). After containers are started, you can open Grafana at [http://localhost:3000](http://localhost:3000). Finally, run a performance test using the `influxDbListener` and you will be able to see the live results, and keep historic data. Cool, isn't it?!

::: warning
Use provided `docker-compose` settings for local tests only. It uses weak credentials and is not properly configured for production purposes.
:::

Check [InfluxDbBackendListener](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/InfluxDbBackendListener.java) for additional details and settings.

#### Elasticsearch

Another alternative is using provided `jmeter-java-dsl-elasticsearch-listener` module with Elasticsearch and Grafana servers using a dashboard like [this one](../../docs/guide/elasticsearch/grafana-provisioning/dashboards/jmeter.json).

To use the module, you will need to include following dependency in your project:

:::: tabs type:card
::: tab Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-elasticsearch-listener</artifactId>
  <version>0.28</version>
  <scope>test</scope>
</dependency>
```
:::
::: tab Gradle
Add this repository:
```groovy
maven { url 'https://jitpack.io' }
```

And the dependency:
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-elasticsearch-listener:0.28'
```

:::
::::

And use provided `elasticsearchListener()` method like in this example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.elasticsearch.listener.ElasticsearchBackendListener.*;

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
        elasticsearchListener("http://localhost:9200/jmeter")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: warning
This module uses [this JMeter plugin](https://github.com/delirius325/jmeter-elasticsearch-backend-listener) which, at its current version, has performance and dependency issues which might affect your project. [This](https://github.com/delirius325/jmeter-elasticsearch-backend-listener/pull/109) and [this](https://github.com/delirius325/jmeter-elasticsearch-backend-listener/pull/110) pull requests fix those issues, but until they are merged and released, you might face such issues.    
:::

In same fashion as InfluxDB, if you want to try it locally, you can run `docker-compose up` inside [this directory](../../docs/guide/elasticsearch) and follow similar steps [as described for InfluxDB](#influxdb) to visualize live metrics in Grafana.

::: warning
Use provided `docker-compose` settings for local tests only. It uses weak or no credentials and is not properly configured for production purposes.
:::

Check [ElasticsearchBackendListener](../../jmeter-java-dsl-elasticsearch-listener/src/main/java/us/abstracta/jmeter/javadsl/elasticsearch/listener/ElasticsearchBackendListener.java) for additional details and settings.

### Generate HTML reports from test plan execution

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

### Live built-in graphs and stats

Sometimes you want to get live statistics on the test plan and don't want to install additional tools, and are not concerned about keeping historic data. 

You can use `dashboardVisualizer` to get live charts and stats for quick review. 

To use it, you need to add following dependency:

:::: tabs type:card
::: tab Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-dashboard</artifactId>
  <version>0.28</version>
  <scope>test</scope>
</dependency>
```
:::
::: tab Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-dashboard:0.28'
```
:::
::::

And use it as you would with any of previously mentioned listeners (like `influxDbListener` and `jtlWriter`). 

Here is an example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.dashboard.DashboardVisualizer.*;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup("Group1")
          .rampToAndHold(10, Duration.ofSeconds(10), Duration.ofSeconds(10))
          .children(
            httpSampler("Sample 1", "http://my.service")
          ),
        threadGroup("Group2")
          .rampToAndHold(20, Duration.ofSeconds(10), Duration.ofSeconds(20))
          .children(
            httpSampler("Sample 2", "http://my.service/get")
          ),
        dashboardVisualizer()
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

The `dashboardVisualizer` will pop up a window like the following one, which you can use to trace statistics while the test plan runs:

![dashboard](./images/dashboard.png)

::: warning
The dashboard imposes additional resources (CPU & RAM) consumption on the machine generating the load test, which may affect the test plan execution and reduce the number of concurrent threads you may reach in your machine. In general, prefer using one of previously mentioned methods and use the dashboard just for local testing and quick feedback. 

**Remember removing it when is no longer needed in the test plan**
:::

::: warning
The test will not end until you close all pop up windows. This allows you to see the final charts and statistics of the plan before ending the test.
:::

::: tip
As with `jtlWriter` and `influxDbListener`, you can place `dashboardVisualizer` at different levels of test plan (at test plan level, at thread group level, as child of sampler, etc.), to only capture statistics of that particular part of the test plan.
:::

## Response Processing

### Check for expected response

By default, JMeter marks any HTTP request with a fail response code (4xx or 5xx) as failed, which allows you to easily identify when some request unexpectedly fails. But in many cases this is not enough or desirable, and you need to check for response body (or some other field) to contain (or not) certain string. 

This is usually accomplished in JMeter with the usage of Response Assertions, which provides an easy and fast way to verify that you get the proper response for each step of the test plan, marking the request as failure when specified condition is not met. 

Here is an example on how to specify a response assertion in jmeter-java-dsl:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

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

### Change sample result statuses with custom logic

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

### Use part of a response in a following request (aka: correlation)

It is a usual requirement while creating a test plan for an application to be able to use part of a response (e.g.: a generated ID, token, etc.) in a subsequent request. This can be easily achieved using JMeter extractors and variables. 

#### Regular Expression Extractor

Here is an example with jmeter-java-dsl using regular expressions:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

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

#### Boundary Extractors

Regular expressions are quite powerful and flexible, but also are complex and performance might not be optimal in some scenarios. When you know that desired extraction is always surrounded by some specific text that never varies, then you can use `boundaryExtractor` which is simpler and in many cases more performant:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service/accounts")
          .post("{\"name\": \"John Doe\"}", Type.APPLICATION_JSON)
          .children(
            boundaryExtractor("ACCOUNT_ID", "\"id\":\"", "\"")
          ),
        httpSampler("http://my.service/accounts/${ACCOUNT_ID}")
      )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [DslBoundaryExtractor](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslBoundaryExtractor.java) for more details and additional options.

#### JSON Extractor

When the response of a request is JSON, then you can use `jsonExtractor` by using [JMESPath query](https://jmespath.org/) like in following example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service/accounts")
          .post("{\"name\": \"John Doe\"}", Type.APPLICATION_JSON)
          .children(
            jsonExtractor("ACCOUNT_ID", "id")
          ),
        httpSampler("http://my.service/accounts/${ACCOUNT_ID}")
      )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: warning
Be aware that this element uses JMeter JSON JMESPath Extractor element, and not the JMeter JSON Extractor element. This means that uses JMESPath instead of JSON Path. 
:::

## Requests generation

### Conditionals

At some point, you will need to execute part of a test plan according to certain condition (eg: a value extracted from previous request). When you reach such point, you can use `ifController` like in following example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

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
        ifController("${__groovy(vars['ACCOUNT_ID'] != null)}", 
          httpSampler("http://my.service/accounts/${ACCOUNT_ID}")
        )
      )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

You can also use a Java lambda instead of providing JMeter expression, which benefits from Java type safety & IDEs code auto-completion:

```java
ifController(s -> s.vars.get("ACCOUNT_ID") != null,
    httpSampler("http://my.service/accounts/${ACCOUNT_ID}")
)
```

::: warning
Using java code (lambdas) will only work with embedded JMeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with BlazeMeter). Use the first option to avoid such limitations.
:::

Check [DslIfController](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/DslIfController.java) and [JMeter Component documentation](https://jmeter.apache.org/usermanual/component_reference.html#If_Controller) for more details.

### Loops

#### While Controller

If at any time you want to execute a given part of a test plan, inside a thread iteration, while a condition is met, then you can use `whileController` (internally using [JMeter While Controller](https://jmeter.apache.org/usermanual/component_reference.html#While_Controller)) like in following example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10, 
        whileController("${__groovy(vars['ACCOUNT_ID'] == null)}",
            httpSampler("http://my.service/accounts")
              .post("{\"name\": \"John Doe\"}", Type.APPLICATION_JSON)
              .children(
                regexExtractor("ACCOUNT_ID", "\"id\":\"([^\"]+)\"")
              )
        )
      )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

As with `ifController`, you can also use Java lambdas to benefit from IDE auto-completion and type safety. Eg:

```java
whileController(s -> s.vars.get("ACCOUNT_ID") == null,
    httpSampler("http://my.service/accounts")
      .post("{\"name\": \"John Doe\"}", Type.APPLICATION_JSON)
      .children(
        regexExtractor("ACCOUNT_ID", "\"id\":\"([^\"]+)\"")
      )
)
```

::: warning
Using java code (lambdas) will only work with embedded JMeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with BlazeMeter). Use the first option to avoid such limitations.
:::

::: warning
JMeter evaluates while conditions before entering each iteration, and after exiting each iteration. Take this into consideration if the condition has side effects (eg: incrementing counters, altering some other state, etc).
:::

::: tip
JMeter automatically generates a variable `__jm__<loopName>__idx` with the current index of while iteration (starting with 0). Example:

```java
whileController("items", "${__groovy(vars.getObject('__jm__items__idx') < 4)}",
    httpSampler("http://my.service/items")
      .post("{\"name\": \"My Item\"}", Type.APPLICATION_JSON)
)
```

Default name for while controller, when not specified, is `while`.
:::

Check [DslWhileController](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/DslWhileController.java) for more details.

#### For Loop Controller

In simple scenarios where you just want to execute a fixed number of times, within a thread group iteration, a given part of the test plan, you can just use `forLoopController` (which uses [JMeter Loop Controller component](https://jmeter.apache.org/usermanual/component_reference.html#Loop_Controller)) as in following example:

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
        forLoopController(5,
            httpSampler("http://my.service/accounts")
        )
      )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

This will result in 10 * 5 = 50 requests to the given URL for each thread in the thread group.

::: tip
JMeter automatically generates a variable `__jm__<loopName>__idx` with the current index of while iteration (starting with 0) which you can use in children elements.
:::

Check [ForLoopController](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/ForLoopController.java) for more details.

### Provide Request Parameters Programmatically per Request

With the standard DSL you can provide static values to request parameters, such as a body. However, you may also want to be able to modify your requests for each call. This is common in cases where your request creates something that must have unique values.

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.apache.jmeter.threads.JMeterVariables;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

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

### CSV as input data for requests

Sometimes is necessary to run same flow but using different but pre-defined data on each request. For example, a common use case is using a different user (from a given set) in each request.

This can be easily achieved using provided `csvDataSet` element. For example, having a file like this one:

```csv
USER,PASS
user1,pass1
user2,pass2
```

You can implement a test plan which test recurrent login with the two users with something like this:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        csvDataSet("users.csv"),
        threadGroup(5, 10,
            httpSampler("http://my.service/login")
                .post("{\"${USER}\": \"${PASS}\"", Type.APPLICATION_JSON),
            httpSampler("http://my.service/logout")
                .method(HttpMethod.POST)
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: tip
By default the CSV file will be opened once and shared by all threads. This means that when one thread reads a CSV line in one iteration, then the following thread reading a line will continue for the following line.

If you want to change this (to only share file per thread group, or using one file per thread), then you can use provided sharedIn method like in followin example:

```java
import us.abstracta.jmeter.javadsl.core.configs.DslCsvDataSet.Sharing;
...
    TestPlanStats stats=testPlan(
    csvDataSet("users.csv")
    .sharedIn(Sharing.THREAD),
    threadGroup(5,10,
    httpSampler("http://my.service/login")
    .post("{\"${USER}\": \"${PASS}\"",Type.APPLICATION_JSON),
    httpSampler("http://my.service/logout")
    .method(HttpMethod.POST)
    )
    )
```
:::

Check [DslCsvDataSet](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/configs/DslCsvDataSet.java) for additional details and options (like changing delimiter, handling files without headers line, stopping on end of file, etc.).

### Group requests

Sometimes, is necessary to be able to group requests which constitute different steps in a test. For example, to separate necessary requests to do a login from the ones used to add items to the cart and the ones to do a purchase. JMeter (and the DSL) provide Transaction Controllers for this purpose, here is an example:

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

### Emulate user delays between requests

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

::: warning
Timers apply to all samplers in their scope, adding a pause after pre-processors executions and before the actual sampling. For example, in previous example pauses would be added before checkout and also before user info (two pauses). 

If you want to apply a timer only to one sampler, add it as child of the given sampler. Like in this example:

```java
httpSampler("http://my.service/cart/chekout")
    .children(uniformRandomTimer(4000, 10000))
```
:::

::: warning
`uniformRandomTimer` `minimumMillis` and `maximumMillis` parameters differ from the ones used by JMeter Uniform Random Timer element, to make it simpler to users with no JMeter background. 

The generated JMeter test element uses as `Constant Delay Offset` the `minimumMillis` value, and as `Maximum random delay` `(maximumMillis - minimumMillis)` value.
:::

### Execute test plan part for a fraction of times

In some cases, you may want to execute a given part of the test plan not in every iteration and only for a given percent of the times to emulate certain probabilistic nature of the flow the users execute.

In such scenarios you may use `percentController`, which uses JMeter Throughput Controller to achieve exactly that. 

Here is an example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws Exception {
    TestPlanStats stats = testPlan(
      threadGroup(2, 10,
        percentController(40, // run this 40% of the times
            httpSampler("http://my.service/status"),
            httpSampler("http://my.service/poll")), 
        percentController(70, // run this 70% of the times
            httpSampler("http://my.service/items"))
      )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [PercentController](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/controllers/PercentController.java) for more details.

## Protocols

### HTTP performance testing

Throughout this guide, several examples have been shown for simple cases of HTTP requests (mainly how to do gets and posts), but the DSL provides additional features that you might need to be aware. 

Here we show some of them, but check [JmeterDsl](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/JmeterDsl.java) and [DslHttpSampler](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/DslHttpSampler.java) to explore all available features.

#### Methods & body

As previously seen you can do simple gets and post like in following snippet:

```java
httpSampler("http://my.service") // A simple get
httpSampler("http://my.service")
    .post("{\"field\":\"val\"}", Type.APPLICATION_JSON) // simple post
```

But you can also use additional methods to specify any HTTP method and body:

```java
httpSampler("http://my.service")
    .method(HttpMethod.PUT)
    .contentType(Type.APPLICATION_JSON)
    .body("{\"field\":\"val\"}")
```

When in need to generate dynamic URLs or bodies you can use a lambda expressions (as previously seen in some example):

```java
httpSampler("http://my.service")
  .post(s -> buildRequestBody(s.vars), Type.TEXT_PLAIN)
httpSampler("http://my.service")
  .body(s -> buildRequestBody(s.vars))
httpSampler(s -> buildRequestUrl(s.vars))
```

::: warning
As previously mentioned for other lambdas, using them will only work with embedded JMeter engine. So, prefer using [JSR223 pre processors](#provide-request-parameters-programmatically-per-request) with groovy script instead if you want to be able to run the test at scale or use generated JMX.
:::

#### Headers

You might have already noticed in some of the examples that we have shown already some ways to set some headers. For instance, in following snippet `Content-Type` header is being set in two different ways:

```java
httpSampler("http://my.service")
  .post("{\"field\":\"val\"}", Type.APPLICATION_JSON)
httpSampler("http://my.service").
  contentType(Type.APPLICATION_JSON)
```

These are handy methods to specify `Content-Type` header, but you can also set any header on a particular request using provided `header` method, like this:

```java
httpSampler("http://my.service")
  .header("X-First-Header", "val1")
  .header("X-Second-Header", "val2")
```

Additionally, you can specify headers to be used by all samplers in a test plan, thread group, transaction controllers, etc. For this you can use `httpHeaders` like this:

```java
testPlan(
  threadGroup(2, 10,
    httpHeaders()
      .header("X-Header", "val1"),
    httpSampler("http://my.service"),
    httpSampler("http://my.service/users")
  )
).run();
```

::: tip
You can also use lambda expressions for dynamically building HTTP Headers, but same limitations apply as in other cases (running in BlazeMeter or using generated JMX file).
:::

#### Cookies & Caching

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

#### Embedded resources

Sometimes you may need to reproduce browsers behavior, downloading for a given URL all associated resources (images, frames, etc.). 

jmeter-java-dsl allows you to easily reproduce this scenario by using the `downloadEmbeddedResources` method in `httpSampler` like in following example:

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
        threadGroup(5, 10,
            httpSampler("http://my.service/")
                .downloadEmbeddedResources()
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

This will make JMeter to automatically parse the HTTP response for embedded resources, download them and register embedded resources downloads as sub samples of the main sample.

Check [JMeter documentation](https://jmeter.apache.org/usermanual/component_reference.html#HTTP_Request) for additional details on downloaded embedded resources.

::: warning
The DSL, unlike JMeter, uses by default concurrent download of embedded resources (with up to 6 parallel downloads), which is the most used scenario to emulate browsers behavior. 
:::

#### Redirects

When jmeter-java-dsl (using JMeter logic) detects a redirection, it will automatically do a request to the redirected URL and register the redirection as a sub sample of the main request.

If you want to disable such logic, you can just call `.followRedirects(false)` in a given `httpSampler`.

### Java API performance testing

Sometimes JMeter provided samplers are not enough for testing a particular technology, custom code or service that requires some custom code to interact with. For these cases you might use `jsr223Sampler` which allows you to use custom logic to generate a sample result.

Here is an example for load testing a redis server:

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

::: tip
Remember adding any particular dependencies required by your code. For example, above example requires this dependency:

```xml
<dependency>
  <groupId>redis.clients</groupId>
  <artifactId>jedis</artifactId>
  <version>3.6.0</version>
  <scope>test</scope>
</dependency>
```
:::

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
Using java code (lambdas) will only work with embedded JMeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with BlazeMeter). Use the first option to avoid such limitations.
:::

`jsr223Sampler` is very powerful, but also makes code and test plan harder to maintain (as with any custom code) compared to using JMeter built-in samplers. So, in general, prefer using JMeter provided samplers if they are enough for the task at hand, and use `jsr223Sampler` sparingly. 

Check [DslJsr223Sampler](../../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/java/DslJsr223Sampler.java) for more details and additional options.

## JMX support

### Save as JMX

In case you want to load a test plan in JMeter GUI, you can save it just invoking `saveAsJMX` method in the test plan as in the following example:

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

### Run JMX file

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
When the JMX uses some custom plugin or JMeter protocol support, you might need to add required dependencies to be able to run the test in an embedded engine. For example, when running a TN3270 JMX test plan using RTE plugin you will need to add the following repository and dependencies:
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
