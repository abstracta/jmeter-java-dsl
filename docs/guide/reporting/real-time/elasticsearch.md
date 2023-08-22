#### Elasticsearch

Another alternative is using provided `jmeter-java-dsl-elasticsearch-listener` module with Elasticsearch and Grafana servers using a dashboard like [this one](/docs/guide/reporting/real-time/elasticsearch/grafana-provisioning/dashboards/jmeter.json).

To use the module, you will need to include the following dependency in your project:

:::: code-group type:card
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-elasticsearch-listener</artifactId>
  <version>1.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
repositories {
    ...
    maven { url 'https://jitpack.io' }
}

dependencies {
    ...
    testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-elasticsearch-listener:1.19'
}
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
This module uses [this JMeter plugin](https://github.com/delirius325/jmeter-elasticsearch-backend-listener) which, at its current version, has performance and dependency issues that might affect your project. [This](https://github.com/delirius325/jmeter-elasticsearch-backend-listener/pull/109) and [this](https://github.com/delirius325/jmeter-elasticsearch-backend-listener/pull/110) pull requests fix those issues, but until they are merged and released, you might face such issues.    
:::

In the same fashion as InfluxDB, if you want to try it locally, you can run `docker-compose up` inside [this directory](/docs/guide/reporting/real-time/elasticsearch) and follow similar steps [as described for InfluxDB](./influxdb.md#influxdb) to visualize live metrics in Grafana.

::: warning
Use provided `docker-compose` settings for local tests only. It uses weak or no credentials and is not properly configured for production purposes.
:::

Check [ElasticsearchBackendListener](/jmeter-java-dsl-elasticsearch-listener/src/main/java/us/abstracta/jmeter/javadsl/elasticsearch/listener/ElasticsearchBackendListener.java) for additional details and settings.
