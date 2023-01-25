### CSV as input data for requests

Sometimes is necessary to run the same flow but use different but pre-defined data on each request. For example, a common use case is using a different user (from a given set) in each request.

This can be easily achieved using provided `csvDataSet` element. For example, having a file like this one:

```csv
USER,PASS
user1,pass1
user2,pass2
```

You can implement a test plan which tests recurrent login with the two users with something like this:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import java.time.Duration;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    TestPlanStats stats = testPlan(
        csvDataSet("users.csv"),
        threadGroup(5, 10,
            httpSampler("http://my.service/login")
                .post("{\"${USER}\": \"${PASS}\"", ContentType.APPLICATION_JSON),
            httpSampler("http://my.service/logout")
                .method(HTTPConstants.POST)
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: tip
By default, the CSV file will be opened once and shared by all threads. This means that when one thread reads a CSV line in one iteration, then the following thread reading a line will continue with the following line.

If you want to change this (to share the file per thread group, or use one file per thread), then you can use provided `sharedIn` method like in the followin example:

```java
import us.abstracta.jmeter.javadsl.core.configs.DslCsvDataSet.Sharing;
...
  TestPlanStats stats = testPlan(
      csvDataSet("users.csv")
        .sharedIn(Sharing.THREAD),
      threadGroup(5, 10,
          httpSampler("http://my.service/login")
            .post("{\"${USER}\": \"${PASS}\"", Type.APPLICATION_JSON),
          httpSampler("http://my.service/logout")
            .method(HTTPConstants.POST)
      )
  )
```
:::

::: warning
You can use `randomOrder()` method to get CSV lines in random order (using [Random CSV Data Set plugin](https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/random-csv-data-set/RandomCSVDataSetConfig.md)), but this is less performant as getting them sequentially, so use it sparingly.
:::

Check [DslCsvDataSet](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/configs/DslCsvDataSet.java) for additional details and options (like changing delimiter, handling files without headers line, stopping on the end of file, etc.).
