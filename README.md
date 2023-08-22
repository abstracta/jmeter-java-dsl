![logo](/docs/.vuepress/public/logo.svg)

[![Maven Central](https://img.shields.io/maven-central/v/us.abstracta.jmeter/jmeter-java-dsl.svg?label=Maven%20Central)](https://search.maven.org/artifact/us.abstracta.jmeter/jmeter-java-dsl)
[![Reproducible Builds](https://img.shields.io/badge/Reproducible_Builds-ok-green?labelColor=1e5b96)](https://github.com/jvm-repo-rebuild/reproducible-central/blob/master/content/us/abstracta/jmeter/jmeter-java-dsl/README.md)

Simple Java API to run performance tests, using [JMeter] as engine, in a Git and programmers friendly way.

If you like this project, **please give it a star :star:!** This helps the project be more visible, gain relevance, and encourage us to invest more effort in new features.

[Here](https://abstracta.github.io/jmeter-dotnet-dsl) you can find the .Net DSL.

Please join [discord server](https://discord.gg/WNSn5hqmSd) or create GitHub [issues](https://github.com/abstracta/jmeter-java-dsl/issues) and [discussions](https://github.com/abstracta/jmeter-java-dsl/discussions) to be part of the community and clear out doubts, get the latest news, propose ideas, report issues, etc.

## Usage

If you use [maven](https://maven.apache.org/what-is-maven.html), just include the following dependency:

```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl</artifactId>
  <version>1.19</version>
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
      jtlWriter("target/jtls")
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }
  
}
```

> This example also uses [AssertJ](https://joel-costigliola.github.io/assertj/assertj-core-quick-start.html) for assertions, but you can use whatever assertion library you choose.

More examples can be found in [tests](jmeter-java-dsl/src/test/java/us/abstracta/jmeter/javadsl)

You can use [this project](https://github.com/abstracta/jmeter-java-dsl-sample) as a starting point.

> **Tip 1:** Check [the DSL recorder](https://abstracta.github.io/jmeter-java-dsl/guide/#dsl-recorder) and [jmx2dsl](https://abstracta.github.io/jmeter-java-dsl/guide/#dsl-code-generation-from-jmx-file) to ease test plan creation or migration from existing JMX files.
>
> **Tip 2:** Since JMeter uses [log4j2](https://logging.apache.org/log4j/2.x/), if you want to control the logging level or output, you can use something similar to the tests included [log4j2.xml](jmeter-java-dsl/src/test/resources/log4j2.xml).
>
> **Tip 3:** When working with multiple samplers in a test plan, specify their names to easily check their respective statistics.

**Check [here](https://abstracta.github.io/jmeter-java-dsl/) for details on some interesting use cases**, like running tests at scale in [BlazeMeter](https://www.blazemeter.com/) or [OctoPerf](https://octoperf.com/), saving and loading test plans from JMX, publishing test metrics to [InfluxDB](https://www.influxdata.com/products/influxdb-overview/) (and visualizing them from [Grafana](https://grafana.com/)), and general usage guides.

## Why?

Check more about the motivation and analysis of alternatives [here](https://abstracta.github.io/jmeter-java-dsl/motivation/)

## Support

Join our [Discord server](https://discord.gg/WNSn5hqmSd) to engage with fellow JMeter DSL enthusiasts, ask questions, and share experiences. Visit [GitHub Issues](https://github.com/abstracta/jmeter-java-dsl/issues) or [GitHub Discussions](https://github.com/abstracta/jmeter-java-dsl/discussions) for bug reports, feature requests and share ideas.

[Abstracta](https://abstracta.us), the main supporter for JMeter DSL development, offers enterprise-level support. Get faster response times, personalized customizations and consulting.

For detailed support information, visit our [Support](https://abstracta.github.io/jmeter-java-dsl/support) page.

## Articles & Talks

* [Developer’s friendly tools for continuous performance testing](https://abstracta.us/blog/performance-testing/developers-friendly-tools-for-continuous-performance-testing/): Walk-through from Fiddler recording to JMeter DSL test plan by Belen Vignolo @ Abstracta. [Russian translation by Ksenia Moseenkova](https://habr.com/ru/company/otus/blog/653823/).
* [JMeterDSL: Bringing Performance Testing Closer to Developers](https://www.blazemeter.com/blog/jmeterdsl-performance-testing-developers): Intro to JMeter DSL and scaling execution in BlazeMeter by Yaina Machado.
* [Performance testing tools trend](https://www.linkedin.com/pulse/performance-testing-tools-trend-roger-abelenda/): A quick review of different alternatives for performance testing in Java and associated trend by Roger Abelenda @ Abstracta.
* [JMeter scripting: la pieza faltante](https://www.youtube.com/watch?v=n-U6YPXAGX0): Spanish demo by Roger Abelenda and hosted by Blanca Moreno @ QA Minds.
* [Getting Started with JMeter DSL](https://qainsights.com/getting-started-with-jmeter-dsl): Intro to JMeter DSL and general thoughts by Roger Abelenda and hosted by NaveenKumar Namachivayam @ QA Insights. [Here is the video version](https://www.youtube.com/watch?v=JnnmSSYE2ok).
* [Virtual Threads: JMeter meets Project Loom](https://abstracta.us/blog/performance-testing/virtual-threads-jmeter-meets-project-loom/): Experimenting with Virtual Threads in JMeter using JMeter DSL as a prototyping tool by Roger Abelenda @ Abstracta. [Here is the Spanish version](https://medium.com/@abstracta/threads-virtuales-jmeter-y-project-loom-ad2a849af53f)
* [JMeter Scripts Written in Java??](https://www.youtube.com/watch?v=_drADTk82kg): JMeter DSL demo and discussion at PerfBytes session by Roger Abelenda and hosted by Mark Tomlinson.
* [JMeter: test as code solutions](https://octoperf.com/blog/2022/06/13/jmeter-test-as-code/): JMeter DSL & Taurus review by Gérald Pereira @ OctoPerf.
* [JMeter DSL, an Innovative Tool for Performance Testing](https://abstracta.us/blog/tools/jmeter-dsl-an-innovative-tool-for-performance-testing/): Short article on JMeter DSL motivation by Roger Abelenda @ Abstracta. [Spanish version](https://medium.com/@abstracta/jmeter-dsl-una-innovadora-herramienta-para-testing-de-performance-e808e3e82c3b).
* [JMeter DSL, the Story of Abstracta’s Latest Innovation in Software Testing](https://abstracta.us/blog/performance-testing/jmeter-dsl-abstractas-latest-innovation-in-software-testing/): Post about JMeter DSL inception story by Natalie Rodgers & Roger Abelenda @ Abstracta. [Spanish Version](https://medium.com/@abstracta/jmeter-dsl-la-historia-de-la-m%C3%A1s-reciente-innovaci%C3%B3n-en-testing-de-software-de-abstracta-743b02e287e2).
* [Develop JMeter Scripts Using Java with Roger Abelenda](https://testguild.com/podcast/performance/p93-roger/): Short interview by Joe Colantonio from TestGuild to Roger Abelenda about JMeter DSL basics.
* [PerfOps - faster and cheaper through a service approach](https://habr.com/ru/company/oleg-bunin/blog/682746/): A nice analysis on implementing a performance experts service while using JMeter DSL as basics for creating a framework on top of it by Kirill Yurkov. (In Russian, but you can use Chrome Translation ;))
* [pymeter announcement](https://www.linkedin.com/feed/update/urn:li:activity:6987704015933304832/): Announcement of a python API, built on top JMeter DSL, which eases JMeter test plan creation and execution for python devs by Eldad Uzman. 
* [Evolve Your Selenium Scripts Into Performance Scripts](https://www.youtube.com/watch?v=YZhCPXfMuqo): SeleniumConf 2023 talk showing generation of performance scripts from Selenium scripts, using JMeter DSL recorder. [Here](https://github.com/abstracta/selenium-jmeter-dsl-demo) is demo repository.
* [Developer-Friendly JMeter DSL for Authoring JMeter Scripts](https://techcommunity.microsoft.com/t5/apps-on-azure-blog/developer-friendly-jmeter-dsl-for-authoring-jmeter-scripts/ba-p/3834565): Azure Load Testing article on integration between JMeter DSL and Azure Load Testing.
* [JMeter DSL: Un giro emocionante en las pruebas de rendimiento](https://www.freerangetesters.com/post/jmeter-dsl-un-giro-emocionante-en-las-pruebas-de-rendimiento): Short Spanish article that highlights some of the benefits and differentiators of using JMeter DSL and included recorder.  

## Ecosystem

* [pymeter](https://github.com/eldaduzman/pymeter): python API based on JMeter DSL that allows python devs to create and run JMeter test plans.

## Contributing & Requesting features

Currently, the project covers the most used features required when implementing JMeter performance tests, but not everything the JMeter supports/provides.

We invest in the development of DSL according to the community's (your) interest, which we evaluate by reviewing GitHub stars' evolution, feature requests, and contributions.

To keep improving the DSL we need you to **please create an issue for any particular feature or need that you have**.

We also really appreciate pull requests. Check the [CONTRIBUTING](CONTRIBUTING.md) guide for an explanation of the main library components and how you can extend the library.

[JMeter]: http://jmeter.apache.org/
