#### JSON extraction

When the response of a request is JSON, then you can use `jsonExtractor` like in the following example:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(2, 10,
            httpSampler("http://my.service/accounts")
                .post("{\"name\": \"John Doe\"}", ContentType.APPLICATION_JSON)
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

::: tip
By default this element uses JMeter JSON [JMESPath](https://jmespath.org/) Extractor element, and in consequence JMESPath as query language.

If you want to use JMeter JSON Extractor element, and in consequence [JSONPath](https://github.com/json-path/JsonPath) as query language, you can simply use `.queryLanguage(JsonQueryLanguage.JSON_PATH)` and a JSONPath query.
:::
