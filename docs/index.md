---
home: true
heroHeight: 68
heroImage: /logo.svg
actions:
  - text: User Guide →
    link: /guide/
features:
- title: 💙 Git, IDE & Programmers Friendly
  details: Simple way of defining performance tests that takes advantage of IDEs autocompletion and inline documentation.
- title: 💪 JMeter ecosystem & community
  details: Use the most popular performance tool and take advantage of the wide support of protocols and tools.
- title: 😎 Built-in features & extensibility
  details: Built-in additional features which ease usage (like <a href="guide/#dsl-code-generation-from-jmx-file">jmx2dsl</a> and <a href="guide/#dsl-recorder">recorder</a>)  and CI/CD pipelines integration.
footer: Made by <a href="https://abstracta.us">Abstracta</a> with ❤️ | Apache 2.0 Licensed | Powered by <a href="https://v2.vuepress.vuejs.org/">Vuepress</a>
footerHtml: true
---

## Example

Add dependency to your project:

```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl</artifactId>
  <version>1.19</version>
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

You can use [this project](https://github.com/abstracta/jmeter-java-dsl-sample) as a starting point.

<!-- @include: testimonials.md -->
