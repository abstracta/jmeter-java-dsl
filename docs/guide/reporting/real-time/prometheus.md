#### Prometheus

As in previous scenarios, you can also use Prometheus and Grafana.

To use the module, you will need to include the following dependency in your project:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-prometheus</artifactId>
  <version>1.29</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-prometheus:1.29'
```
:::
::::

And use provided `prometheusListener()` method like in this example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.prometheus.DslPrometheusListener.*;

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
        prometheusListener()
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

As in previous cases, you can to try it locally by running `docker-compose up` inside [this directory](/docs/guide/reporting/real-time/prometheus). After containers are started, you can follow the same steps as in previous scenarios.

::: warning
Use the provided `docker-compose` settings for local tests only. It uses weak credentials and is not properly configured for production purposes.
:::

Check [DslPrometheusListener](/jmeter-java-dsl-prometheus/src/main/java/us/abstracta/jmeter/javadsl/prometheus/DslPrometheusListener.java) for details on listener settings.

Here is an example that shows the default settings used by `prometheusListener`:

```java
import us.abstracta.jmeter.javadsl.prometheus.DslPrometheusListener.PrometheusMetric;
...
prometheusListener()
  .metrics(
    PrometheusMetric.responseTime("ResponseTime", "the response time of samplers")
      .labels(PrometheusMetric.SAMPLE_LABEL, PrometheusMetric.RESPONSE_CODE)
      .quantile(0.75, 0.5)
      .quantile(0.95, 0.1)
      .quantile(0.99, 0.01)
      .maxAge(Duration.ofMinutes(1)),
    PrometheusMetric.successRatio("Ratio", "the success ratio of samplers")
      .labels(PrometheusMetric.SAMPLE_LABEL, PrometheusMetric.RESPONSE_CODE)
  )
  .port(9270)
  .host("0.0.0.0")
  .endWait(Duration.ofSeconds(10))
...
```
> Note that the default settings are different from the used JMeter Prometheus Plugin, to allow easier usage and avoid missing metrics at the end of test plan execution.

::: tip
When configuring the `prometheusListener` always consider setting a `endWait` that is greater thant the Prometheus Server configured `scrape_interval` to avoid missing metrics at the end of test plan execution (e.g.: 2x the scrape interval value).
:::
