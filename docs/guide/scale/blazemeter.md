### BlazeMeter

By including the following module as a dependency:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-blazemeter</artifactId>
  <version>1.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-blazemeter:1.19'
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

BlazeMeter will not only allow you to run the test at scale but also provides additional features like nice real-time reporting, historic data tracking, etc. Here is an example of how a test would look in BlazeMeter:

![BlazeMeter Example Execution Dashboard](./blazemeter.png)

Check [BlazeMeterEngine](/jmeter-java-dsl-blazemeter/src/main/java/us/abstracta/jmeter/javadsl/blazemeter/BlazeMeterEngine.java) for details on usage and available settings when running tests in BlazeMeter.

::: warning
By default the engine is configured to timeout if test execution takes more than 1 hour.
This timeout exists to avoid any potential problem with BlazeMeter execution not detected by the
client, and avoid keeping the test indefinitely running until is interrupted by a user,
which may incur in unnecessary expenses in BlazeMeter and is specially annoying when running tests
in automated fashion, for example in CI/CD.
It is strongly advised to **set this timeout properly in each run**, according to the expected test
execution time plus some additional margin (to consider for additional delays in BlazeMeter
test setup and teardown) to avoid unexpected test plan execution failure (due to timeout) or
unnecessary waits when there is some unexpected issue with BlazeMeter execution.
:::

::: warning
`BlazeMeterEngine` always returns 0 as `sentBytes` statistics since there is no efficient way to get it from BlazMeter.
:::

::: tip
`BlazeMeterEngine` will automatically upload to BlazeMeter files used in `csvDataSet` and `httpSampler` with `bodyFile` or `bodyFilePart` methods.

For example this test plan works out of the box (no need for uploading referenced files or adapt test plan):

```java
testPlan(
    threadGroup(100, Duration.ofMinutes(5),
      csvDataSet(new TestResource("users.csv")),
      httpSampler(SAMPLE_LABEL, "https://myservice/users/${USER}")
    )
).runIn(new BlazeMeterEngine(System.getenv("BZ_TOKEN"))
    .testTimeout(Duration.ofMinutes(10)));
```

If you need additional files to be uploaded to BlazeMeter, you can easily specify them with the `BlazemeterEngine.assets()` method.
:::

::: tip
By default `BlazeMeterEngine` will run tests from default location (most of the times `us-east4-a`). But in some scenarios you might want to change the location, or even run the test from multiple locations.

Here is an example how you can easily set this up:

```java
testPlan(
    threadGroup(300, Duration.ofMinutes(5), // 300 total users for 5 minutes
      httpSampler(SAMPLE_LABEL, "https://myservice")
    )
).runIn(new BlazeMeterEngine(System.getenv("BZ_TOKEN"))
    .location(BlazeMeterLocation.GCP_SAO_PAULO, 30) // 30% = 90 users will run in Google Cloud Platform at Sao Paulo
    .location("MyPrivateLocation", 70) // 70% = 210 users will run in MyPrivateLocation named private location
    .testTimeout(Duration.ofMinutes(10)));
```

:::

::: tip
In case you want to get debug logs for HTTP calls to BlazeMeter API, you can include the following setting to an existing `log4j2.xml` configuration file:
```xml
<Logger name="us.abstracta.jmeter.javadsl.blazemeter.BlazeMeterClient" level="DEBUG"/>
<Logger name="okhttp3" level="DEBUG"/>
```
:::

::: warning
If you use test elements (JSR223 elements, `httpSamplers`, `ifController` or `whileController`) with Java lambdas instead of strings, check [this section of the user guide](../response-processing/lambdas.md#lambdas) to use them while running test plan in BlazeMeter.
:::
