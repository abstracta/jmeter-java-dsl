# jmeter-java-dsl

Simple Java API to run performance tests, using [JMeter] as engine, in an VCS (versioning control system) and programmers friendly way.

## Usage

If you use [maven](https://maven.apache.org/what-is-maven.html), just include following dependency:

```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <projectId>jmeter-java-dsl</projectId>
  <version>0.2</version>
</dependency>
``` 

Here is a simple example test in [JUnit 5](https://junit.org/junit5/)+ with 2 threads/users iterating 10 times each to send HTTP POST requests with a JSON body to `http://my.service`:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

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
          .post("{\"name\": \"test\"}", Type.APPLICATION_JSON)
      ),
      //this is just to log details of each request stats
      jtlWriter("test.jtl")
    ).run();
    assertThat(stats.overall().elapsedTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

> This example also uses [AssertJ](https://joel-costigliola.github.io/assertj/assertj-core-quick-start.html) for assertions, but you can use whatever assertion library you chose.

More examples can be found in [tests](jmeter-java-dsl/src/test/java/us/abstracta/jmeter/javadsl/JmeterDslTest.java)

> **Tip 1:** Since JMeter uses [log4j2](https://logging.apache.org/log4j/2.x/), if you want to control logging level or output, you can use something similar to the tests included [log4j2.xml](jmeter-java-dsl/src/test/resources/log4j2.xml).
>
> **Tip 2:** When working with multiple samplers in a test plan, specify their names to easily check their respective statistics.

## Why?

There are many tools to script performance/load tests, being [JMeter] and [Gatling](https://gatling.io/) the most popular ones.

JMeter is great for people with no programming knowledge since it provides a graphical interface to create test plans and run them. Additionally, it is the most popular tool (with a lot of supporting tools built on it) and has a big amount of supported protocols and plugins that makes it very versatile. 

But, JMeter has some problems as well: sometimes might be slow to create test plans in JMeter GUI, and you can't get the full picture of the test plan unless you dig in every tree node to check its properties. Furthermore, it doesn't provide a simple programmer friendly API (you can check [here](https://www.blazemeter.com/blog/5-ways-launch-jmeter-test-without-using-jmeter-gui/) for an example on how to run jmeter programmatically without jmeter-java-dsl), nor a VCS friendly format (too verbose and hard to review). For example, for the same test plan previously showed with jmeter-java-dsl, in JMeter you would need a JMX file like [this](docs/sample.jmx), and even then, it wouldn't be as simple to do assertions on collected statistics as in provided example.
 
Gatling does provide a simple API and a VCS friendly format, but requires scala knowledge and environment. Additionally, it doesn't provide as rich environment as JMeter (protocol support, plugins, tools) and requires learning a new framework for testing (if you already use JMeter, which is the most popular tool).

[Taurus](https://gettaurus.org/) is another open-source tool that allows specifying tests in a VCS friendly yaml syntax, and provides additional features like pass/fail criteria and easier CI/CD integration. But, this tool requires a python environment, in addition to the java environment. Additionally, there is no built-in GUI or IDE auto-completion support, which makes it harder to discover and learn the actual syntax. Finally, Taurus syntax only supports a subset of the features JMeter provides, which reduces scope usage. 

Finally, [ruby-dsl](https://github.com/flood-io/ruby-jmeter) is also an opensource library which allows specifying and run in ruby custom dsl JMeter test plans. This is the most similar tool to jmeter-java-dsl, but it requires ruby (in addition to java environment) with the additional performance impact, does not follow same naming and structure convention as JMeter, and lacks of debugging integration with JMeter execution engine.

jmeter-java-dsl tries to get the best of these tools by providing a simple java API with VCS friendly format to run JMeter tests, taking advantage of all JMeter benefits and knowledge also providing many of the benefits of Gatling scripting.
As shown in previous example, it can be easily executed with JUnit, modularized in code and easily integrated in any CI/CD pipeline. Additionally, it makes it easy to debug the execution of test plans with usual IDE debugger tools. Finally, as with most Java libraries, you can use it not only in a Java project, but also in projects of most JVM languages (like kotlin, scala, groovy, etc).

Here is a table with summary of main pros and cons of each tool:

|Tool|Pros|Cons|
|----|----|----|
|JMeter| <ul><li>GUI for non programmers<li>Popularity<li>Protocols Support<li>Documentation<li>Rich ecosystem|<ul><li>Slow test plan creation<li>No VCS friendly format<li>Not programmers friendly<li>No simple CI/CD integration|
|Gatling| <ul><li>VCS friendly<li>IDE friendly (auto-complete and debug)<li>Natural CI/CD integration<li>Natural code modularization and reuse<li>Less resources (CPU & RAM) usage<li>All details of simple test plans at a glance|<ul><li>Scala knowledge and environment required<li>Smaller set of protocols supported<li>Less documentation & tooling|
|Taurus| <ul><li>VCS friendly<li>Simple CI/CD integration<li>Unified framework for running any type of test<li>built-in support for running tests at scale<li>All details of simple test plans at a glance<li>Simple way to do assertions on statistics|<ul><li>Both Java and Python environments required<li>Not as simple to discover (IDE auto-complete or GUI) supported functionality<li>Not complete support of JMeter capabilities (nor in the roadmap)|
|ruby-dsl| <ul><li>VCS friendly<li>Simple CI/CD integration<li>Unified framework for running any type of test<li>built-in support for running tests at scale<li>All details of simple test plans at a glance|<ul><li>Both Java and Ruby environments required<li>Not following same naming convention and structure as JMeter<li>Not complete support of JMeter capabilities (nor in the roadmap)<li>No integration for debugging JMeter code|
|jmeter-java-dsl| <ul><li>VCS friendly<li>IDE friendly (auto-complete and debug)<li>Natural CI/CD integration<li>Natural code modularization and reuse<li>Existing JMeter documentation<li>Easy to add support for JMeter supported protocols and new plugins<li>Could easily interact with JMX files and take advantage of JMeter ecosystem<li>All details of simple test plans at a glance<li>Simple way to do assertions on statistics|<ul><li>Basic Java knowledge required<li>Same resources (CPU & RAM) usage as JMeter|

## Additional features

### Run test at scale

Running a load test from one machine is not always enough, since you are limited to the machine hardware capabilities. Some times is necessary to run the test using a cluster of machines to be able to generate enough load for the system under test.

By including following module as dependency:

```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <projectId>jmeter-java-dsl-blazemeter</projectId>
  <version>0.2</version>
</dependency>
```

You can easily run a JMeter test plan at scale in BlazeMeter like this:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.time.Duration;
import org.eclipse.jetty.http.MimeTypes.Type;
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
          .post("{\"name\": \"test\"}", Type.APPLICATION_JSON)
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

Note that is as simple as [generating a BlazeMeter authentication token](https://guide.blazemeter.com/hc/en-us/articles/115002213289-BlazeMeter-API-keys-) and adding `.runIn(new BlazeMeterEngine(...))` to any existing `jmeter-java-dsl` test to get it running at scale in BlazeMeter.

Check [BlazeMeterEngine](jmeter-java-dsl-blazemeter/src/main/java/us/abstracta/jmeter/javadsl/blazemeter/BlazeMeterEngine.java) for details on usage and available settings when running tests in BlazeMeter.

> **Tip:** In case you want to get debug logs for HTTP calls to BlazeMeter API, you can include following setting to an existing `log4j2.xml` configuration file: 
>```xml
><Logger name="us.abstracta.jmeter.javadsl.blazemeter.BlazeMeterClient" level="DEBUG"/>
><Logger name="okhttp3" level="DEBUG"/>
>```

### Save as JMX

In case you want to load a test plan in JMeter GUI, you can save it just invoking `saveAsJMX` method in the test plan as in following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import org.eclipse.jetty.http.MimeTypes.Type;

public class SaveTestPlanAsJMX {
  
  public static void main(String[] args) throws Exception {
    testPlan(
      threadGroup(2, 10,
        httpSampler("http://my.service")
          .post("{\"name\": \"test\"}", Type.APPLICATION_JSON)
      ),
      //this is just to log details of each request stats
      jtlWriter("test.jtl")
    ).saveAsJmx("dsl-test-plan.jmx");
  }
  
}
```

> Take into consideration that currently there is no automatic way to migrate changes done in JMX to the Java DSL.

This can be helpful to share a Java DSL defined test plan with people not used to the DSL, or to use some JMeter feature (or plugin) that is not yet supported by the DSL (**but, we strongly encourage you to report it as an issue**, so we can implement support for it).  

## Contributing & Requesting features

Currently, the project only covers the basic, but most used, features when implementing JMeter performance tests. 
The idea is to evaluate if the community (you) is interested in using the library, and if so, implement new features as the community request them, covering at some point most of JMeter (and plugins) features. 
In order to accomplish this, we need you to **please create an issue for any particular feature or need that you have**.

We would also really appreciate pull requests. Check the [CONTRIBUTING](CONTRIBUITING.md) guide for an explanation of main library components and how can you extend the library.

Finally, if you like this project, **please give it a star**, that helps the project to be more visible and gain relevance. 

[JMeter]: http://jmeter.apache.org/
