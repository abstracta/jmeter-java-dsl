# Additional features

This document lists some use cases that might be really helpful in several cases.

## Run test at scale in [BlazeMeter](https://www.blazemeter.com/)

Running a load test from one machine is not always enough, since you are limited to the machine hardware capabilities. Sometimes, is necessary to run the test using a cluster of machines to be able to generate enough load for the system under test.

By including following module as dependency:

```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <projectId>jmeter-java-dsl-blazemeter</projectId>
  <version>0.4</version>
</dependency>
```

You can easily run a JMeter test plan at scale in BlazeMeter like this:

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
    assertThat(stats.overall().elapsedTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```
> This test is using `BZ_TOKEN` (a custom environment variable) to get the BlazeMeter API authentication credentials (with `<KEY_ID>:<KEY_SECRET>` format).

Note that is as simple as [generating a BlazeMeter authentication token](https://guide.blazemeter.com/hc/en-us/articles/115002213289-BlazeMeter-API-keys-) and adding `.runIn(new BlazeMeterEngine(...))` to any existing `jmeter-java-dsl` test to get it running at scale in BlazeMeter (with the rest of features provided by BlazeMeter, like the nice reporting it provides and historic data tracking). Here is an example of how a test would look like in BlazMeter:

![blazemeter.png](blazemeter.png) 

Check [BlazeMeterEngine](../jmeter-java-dsl-blazemeter/src/main/java/us/abstracta/jmeter/javadsl/blazemeter/BlazeMeterEngine.java) for details on usage and available settings when running tests in BlazeMeter.

> **Tip:** In case you want to get debug logs for HTTP calls to BlazeMeter API, you can include following setting to an existing `log4j2.xml` configuration file: 
>```xml
><Logger name="us.abstracta.jmeter.javadsl.blazemeter.BlazeMeterClient" level="DEBUG"/>
><Logger name="okhttp3" level="DEBUG"/>
>```

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

> Take into consideration that currently there is no automatic way to migrate changes done in JMX to the Java DSL.

This can be helpful to share a Java DSL defined test plan with people not used to the DSL, or to use some JMeter feature (or plugin) that is not yet supported by the DSL (**but, we strongly encourage you to report it as an issue**, so we can implement support for it).

## Run JMX file

jmeter-java-dsl also provides means to easily run a test plan from a JMX file either locally or in BlazeMeter (through previously mentioned jmeter-java-dsl-blazemeter module). Here is an example:

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
    assertThat(stats.overall().elapsedTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
``` 

This can be used to just run existing JMX files, or when DSL has no support for some JMeter functionality or plugin and you need to use JMeter GUI to build the test plan but still want to use jmeter-java-dsl to run the test plan embedded in Java test or code.

> When the JMX uses some custom plugins or JMeter protocol support, you might need to add required dependencies to be able to run the test in an embedded engine. For example, when running a TN3270 JMX test plan using RTE plugin you will need to add following repository and dependencies:
> ```xml
> <repositories>
>   <repository>
>     <id>jitpack.io</id>
>     <url>https://jitpack.io</url>
>   </repository>
> </repositories>
>
> <dependencies>
>    ...
>    <dependency>
>      <groupId>com.github.Blazemeter</groupId>
>      <artifactId>RTEPlugin</artifactId>
>      <version>3.1</version>
>      <scope>test</scope>
>    </dependency>
>    <dependency>
>      <groupId>com.github.Blazemeter</groupId>
>      <artifactId>dm3270</artifactId>
>      <version>0.12.3-lib</version>
>      <scope>test</scope>
>    </dependency>
> </dependencies>
> ```

## Publish test metrics to [InfluxDB](https://www.influxdata.com/products/influxdb-overview/) and visualizing them in [Grafana](https://grafana.com/)
 
When running tests with JMeter (and in particular with jmeter-java-dsl) a usual requirement is to be able to store such test runs in a persistent database to later on review such metrics, and compare different test runs. Additionally, jmeter-java-dsl only provides some summary data of test run in the console while it is running, but, since it doesn't provide any sort of UI, doesn't allow to easily analyze such information as it can be done in JMeter GUI.
 
To overcome these limitations you can use provided support for publishing JMeter test run metrics to InfluxDB, which allows keeping record of all run statistics and, through Grafana, get some nice dashboards like the following one:

![grafana](influxdb/grafana.png)

This can be easily done using an existing InfluxDB & Grafana server and using a dashboard like [this one](https://grafana.com/grafana/dashboards/4026), or you can even spin up some Docker containers with `docker-compose`. To try it out, you can run `docker-compose up` (previously [installing Docker](https://docs.docker.com/get-docker/) in you machine) inside [this directory](influxdb). After containers are started, you should be able to see a nice dashboard by loading Grafana URL ([http://localhost:3000](http://localhost:3000)) in any browser.

> Use provided `docker-compose` settings for some local tests only. It uses weak credentials and is not properly configured for production purposes.

After having an InfluxDB instance, running a JMeter test plan publishing to InfluxDB is as simple as including the `influxDbListener` as in following example:

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
    assertThat(stats.overall().elapsedTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

Now you can see the results of your test runs live and check past test run metrics!

Check [InfluxDbBackendListener](../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/listeners/InfluxDbBackendListener.java) for additional details and settings.

## Generate HTML report from test plan execution

After running a test plan you would usually like to visualize the results in friendly way that eases analysis of collected information. 

One, and preferred way, to do that is through previously mentioned alternative of using InfluxDb & Grafana. 

Another way might just be using jtlWriter (as shown in [Readme](../README.md)) and then loading the jtl file in JMeter GUI with one of provided listeners (like view results tree, summary report, etc). 

Another alternative is just generating a standalone report for the test plan execution using jmeter-java-dsl provided htmlReporter like this:

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
      htmlReporter("html-report-" + Instant.now())
    ).run();
    assertThat(stats.overall().elapsedTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

> **Note:** htmlReporter will throw an exception if provided directory path is a non empty directory or file

## Change sample result statuses with custom logic

By default, JMeter marks any HTTP samples with a response code of 4xx or 5xx as failed. In general this is a good practice, and allows you to easily identify when something unusual happens. In some cases though, this might not be enough, and you might need to implement some custom logic to mark some cases as success.

Take as an example the scenario where the service under test returns a 429 status code which the test plan should ignore. In such scenario you can use [JSR233PostProcessor](../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslJsr223PostProcessor.java) to modify the result status and avoid marking the request as failure in such scenarios. Here is an example:

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
    assertThat(stats.overall().elapsedTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

> Check [DslJsr223PostProcessor](../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslJsr223PostProcessor.java) for more details and additional options.

JSR223PostProcessor is a very powerful tool, but is not the only, nor the best, alternative for many cases where JMeter already provides a better and simpler alternative (eg: asserting response bodies contain some string). Currently, jmeter-java-dsl does not support all the features JMeter provides. So, if you need something already provided by JMeter, please create an issue in GitHub requesting such a feature or submit a pull request with the required support.
   
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
                    jsr223PreProcessor(
                        "vars.put('REQUEST_BODY', " + getClass().getName()
                            + ".buildRequestBody(vars))")
                )
        )
    ).run();
    assertThat(stats.overall().elapsedTimePercentile99()).isLessThan(Duration.ofSeconds(5));
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

Check [DslJsr223PreProcessor](../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/preprocessors/DslJsr223PreProcessor.java) for more details and additional options.

## Use part of a response in a following request

It is a usual requirement while creating a test plan for an application, to be able to use part of a response (e.g.: a generated ID, token, etc) in a subsequent request. This can be easily achieved using JMeter extractors and variables. Here is an example with jmeter-java-dsl:

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
    assertThat(stats.overall().elapsedTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [DslRegexExtractor](../jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslRegexExtractor.java) for more details and additional options. 