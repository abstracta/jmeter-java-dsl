#### Boundaries based extraction

Regular expressions are quite powerful and flexible, but also are complex and performance might not be optimal in some scenarios. When you know that desired extraction is always surrounded by some specific text that never varies, then you can use `boundaryExtractor` which is simpler and in many cases more performant:

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
                    boundaryExtractor("ACCOUNT_ID", "\"id\":\"", "\"")
                ),
            httpSampler("http://my.service/accounts/${ACCOUNT_ID}")
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

Check [DslBoundaryExtractor](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/postprocessors/DslBoundaryExtractor.java) for more details and additional options.
