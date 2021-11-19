---
home: true
heroImage: /logo.svg
actionText: User Guide →
actionLink: /guide/
features:
- title: 💙 Git, IDE & Programmers Friendly
  details: Simple way of defining performance tests which takes advantage of IDEs autocompletion and inline documentation.
- title: 💪 JMeter ecosystem & community
  details: Use the most popular performance tool and take advantage of wide support of protocols and tools.
- title: 😎 Built-in features & extensibility
  details: Built-in additional features which ease usage and using it in CI/CD pipelines. 
---

## Example

Add dependency to your project:

```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl</artifactId>
  <version>0.33.1</version>
  <scope>test</scope>
</dependency>
```

Create performance test:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

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
            )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```




