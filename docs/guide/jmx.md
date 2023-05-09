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

This can be helpful to share a Java DSL defined test plan with people not used to the DSL or to use some JMeter feature (or plugin) that is not yet supported by the DSL (**but, we strongly encourage you to report it as an issue [here](https://github.com/abstracta/jmeter-java-dsl/issues)** so we can include such support into the DSL for the rest of the community).

::: tip
If you get any error (like `CannotResolveClassException`) while loading the JMX in JMeter GUI, you can try copying `jmeter-java-dsl` jar (and any other potential modules you use) to JMeter `lib` directory, restart JMeter and try loading the JMX again.
:::

::: tip
If you want to migrate changes done in JMX to the Java DSL, you can use [jmx2dsl](./jmx2dsl#dsl-code-generation-from-jmx-file) as an accelerator. The resulting plan might differ from the original one, so sometimes it makes sense to use it, and some it is faster just to port the changes manually.
:::

::: warning
If you use JSR223 Pre- or Post-processors with Java code (lambdas) instead of strings or use one of the HTTP Sampler methods which receive a function as a parameter, then the exported JMX will not work in JMeter GUI. You can migrate them to use jsrPreProcessor with string scripts instead.
:::

### Run JMX file

jmeter-java-dsl also provides means to easily run a test plan from a JMX file either locally, in BlazeMeter (through [previously mentioned jmeter-java-dsl-blazemeter module](./scale/blazemeter#blazemeter)), OctoPerf (through [jmeter-java-dsl-octoperf module](./scale/octoperf#octoperf)), or Azure Load testing (through [jmeter-java-dsl-azure module](./scale/azure#azure-load-testing)). Here is an example:

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

This can be used to just run existing JMX files, or when DSL has no support for some JMeter functionality or plugin (although you can use [wrappers](./wrapper#custom-or-yet-not-supported-test-elements) for this), and you need to use JMeter GUI to build the test plan but still want to use jmeter-java-dsl to run the test plan embedded in Java test or code.

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
