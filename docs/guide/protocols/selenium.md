### Selenium

With JMeter DSL is quite simple to integrate your existing selenium scripts into performance tests. One common use case is to do real user monitoring or synthetics monitoring (get time spent in particular parts of a Selenium script) while the backend load is being generated.

Here is an example of how you can do this with JMeter DSL:

```java
public class PerformanceTest {

  public static class SeleniumSampler implements SamplerScript, ThreadListener {

    private WebDriver driver;

    @Override
    public void threadStarted() {
      driver = new ChromeDriver(); // you can invoke existing set up logic to reuse it
    }

    @Override
    public void runScript(SamplerVars v) {
      driver.get("https://mysite"); // you can invoke existing selenium script for reuse here
    }

    @Override
    public void threadFinished() {
      driver.close(); // you can invoke existing tear down logic to reuse it
    }

  }

  @Test
  public void shouldGetExpectedSampleResultWhenJsr223SamplerWithLambdaAndCustomResponse()
      throws IOException {
    Duration testPlanDuration = Duration.ofMinutes(10);
    TestPlanStats stats = testPlan(
        threadGroup(1, testPlanDuration,
            jsr223Sampler("Real User Monitor", SeleniumSampler.class)
        ),
        threadGroup(100, testPlanDuration,
            httpSampler("https://mysite/products")
                .post("{\"name\": \"test\"}", Type.APPLICATION_JSON)
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofMillis(500));
  }

}
```

Check [previous section](./java.md#java-api-performance-testing) for more details on `jsr223Sampler`.