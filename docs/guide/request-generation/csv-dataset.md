### CSV as input data for requests

Sometimes is necessary to run the same flow but using different pre-defined data on each request. For example, a common use case is to use a different user (from a given set) in each request.

This can be easily achieved using the provided `csvDataSet` element. For example, having a file like this one:

```csv
USER,PASS
user1,pass1
user2,pass2
```

You can implement a test plan that tests recurrent login with the two users with something like this:

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
To properly format the data in your CSV, a general rule you can apply is to replace each double quotes with two double quotes and add double quotes to the beginning and end of each CSV value.

E.g.: if you want one CSV field to contain the value `{"field": "value"}`, then use `"{""field:"": ""value""}"`.

This way, with a simple search and replace, you can include in a CSV field any format like JSON, XML, etc.

Note: JMeter uses should be aware that JMeter DSL `csvDataSet` sets `Allowed quoted data?` flag, in associated `Csv Data Set Config` element, to `true`.
:::

By default, the CSV file will be opened once and shared by all threads. This means that when one thread reads a CSV line in one iteration, then the following thread reading a line will continue with the following line.

If you want to change this (to share the file per thread group or use one file per thread), then you can use the provided `sharedIn` method like in the following example:

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
You can use the `randomOrder()` method to get CSV lines in random order (using [Random CSV Data Set plugin](https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/random-csv-data-set/RandomCSVDataSetConfig.md)), but this is less performant as getting them sequentially, so use it sparingly.
:::

Check [DslCsvDataSet](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/core/configs/DslCsvDataSet.java) for additional details and options (like changing delimiter, handling files without headers line, stopping on the end of file, etc.).
