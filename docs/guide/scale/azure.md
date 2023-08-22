### Azure Load Testing

To use [Azure Load Testing](https://azure.microsoft.com/en-us/products/load-testing/) to execute your test plans at scale, is as easy as including the following module as a dependency:

:::: code-group type:card
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-azure</artifactId>
  <version>1.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-azure:1.19'
```
:::
::::

And using the provided engine like this:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.azure.AzureEngine;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service")
        )
    ).runIn(new AzureEngine(System.getenv("AZURE_CREDS")) // AZURE_CREDS=tenantId:clientId:secretId
        .testName("dsl-test")
        /* 
        This specifies the number of engine instances used to execute the test plan. 
        In this case means that it will run 2(threads in thread group)x2(engines)=4 concurrent users/threads in total. 
        Each engine executes the test plan independently.
         */
        .engines(2) 
        .testTimeout(Duration.ofMinutes(20)));
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```
> This test is using `AZURE_CREDS`, a custom environment variable containing `tenantId:clientId:clientSecret` with proper values for each. Check in [Azure Portal tenant properties](https://portal.azure.com/#view/Microsoft_AAD_IAM/TenantPropertiesBlade) the proper tenant ID for your subscription, and follow [this guide](https://learn.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal) to register an application with proper permissions and secrets generation for tests execution.

As with the BlazeMeter and OctoPerf, you can not only run the test at scale but also get additional features like nice real-time reporting, historic data tracking, etc. Here is an example of how a test looks like in Azure Load Testing:

![Azure Load Testing Example Execution Dashboard](./azure.png)

Check [AzureEngine](/jmeter-java-dsl-azure/src/main/java/us/abstracta/jmeter/javadsl/azure/AzureEngine.java) for details on usage and available settings when running tests in Azure Load Testing.

::: tip
`AzureEngine` will automatically upload to Azure Load Testing files used in `csvDataSet` and `httpSampler` with `bodyFile` or `bodyFilePart` methods.

For example this test plan works out of the box (no need for uploading referenced files or adapt test plan):

```java
testPlan(
    threadGroup(100, Duration.ofMinutes(5),
      csvDataSet(new TestResource("users.csv")),
      httpSampler(SAMPLE_LABEL, "https://myservice/users/${USER}")
    )
).runIn(new AzureEngine(System.getenv("BZ_TOKEN"))
    .testTimeout(Duration.ofMinutes(10)));
```

If you need additional files to be uploaded to Azure Load Testing, you can easily specify them with the `AzureEngine.assets()` method.
:::

::: tip
If you use a `csvDataSet` and multiple Azure engines (through the `engines()` method) and want to split provided CSVs between the Azure engines, as to not generate same requests from each engine, then you can use `splitCsvsBetweenEngines`.
:::

::: tip
If you want to correlate test runs to other entities (like a CI/CD job id, product version release, git commit, etc) you can add such information in the test run name by using the `testRunName()` method.
:::

::: tip
To get a full view in Azure Load Testing test run execution report not only of the performance test collected metrics, but also metrics from the application components under test, you can register all the application components using the `monitoredResources()` method.

`monitoredResources()` requires a list of resources ids, which you can get by navigating in Azure portal to the correct resource, and then copy part of the url from the browser. For example, a resource id for a container app looks like `/subscriptions/my-subscription-id/resourceGroups/my-resource-group/providers/Microsoft.App/containerapps/my-papp`.
:::

::: tip
As with BlazeMeter and OctoPerf cases, if you want to get debug logs for HTTP calls to Azure API, you can include the following setting to an existing `log4j2.xml` configuration file:
```xml
<Logger name="us.abstracta.jmeter.javadsl.azure.AzureClient" level="DEBUG"/>
<Logger name="okhttp3" level="DEBUG"/>
```
:::

::: warning
There is currently no built-in support for test elements with Java lambdas in `AzureEngine` (as there is for `BlazeMeterEngine`). If you need it, please request it by creating a [GitHub issue](https://github.com/abstracta/jmeter-java-dsl/issues).
:::

::: warning
By default the engine is configured to timeout if test execution takes more than 1 hour.
This timeout exists to avoid any potential problem with Azure Load Testing execution not detected by the
client, and avoid keeping the test indefinitely running until is interrupted by a user,
which may incur in unnecessary expenses in Azure and is specially annoying when running tests 
in automated fashion, for example in CI/CD.
It is strongly advised to **set this timeout properly in each run**, according to the expected test
execution time plus some additional margin (to consider for additional delays in Azure Load Testing
test setup and teardown) to avoid unexpected test plan execution failure (due to timeout) or
unnecessary waits when there is some unexpected issue with Azure Load Testing execution.
:::

