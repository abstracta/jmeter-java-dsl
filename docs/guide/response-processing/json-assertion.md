### Check for expected JSON

When checking for JSON responses, it is usually easier to just use `jsonAssertion`. Here is an example:

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
                    jsonAssertion("id")
                ),
            httpSampler("http://my.service/accounts/${ACCOUNT_ID}")
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: tip
Previous example just checks that sample result JSON contains an `id` field. You can use `matches(regex)`, `equalsTo(value)` or even `equalsToJson(json)` methods to check `id` associated value.
Additionally, you can use the `not()` method to check for the inverse condition. E.g.: does not contain `id` field, or field value does not match a given regular expression or is not equal to a given value.
:::

::: tip
By default this element uses JMeter JSON [JMESPath](https://jmespath.org/) Assertion element, and in consequence, JMESPath as query language.

If you want to use JMeter JSON Assertion element, and in consequence [JSONPath](https://github.com/json-path/JsonPath) as the query language, you can simply use `.queryLanguage(JsonQueryLanguage.JSON_PATH)` and a JSONPath query.
:::
