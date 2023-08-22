### JDBC and databases interactions

Several times you will need to interact with a database to either set it to a known state while setting up the test plan, clean it up while tearing down the test plan, or even check or generate some values in the database while the test plan is running.

For these use cases, you can use JDBC DSL-provided elements.

Including the following dependency in your project:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-jdbc</artifactId>
  <version>1.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-jdbc:1.19'
```
:::
::::

And adding a proper JDBC driver for your database, like this example for PostgreSQL:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
  <version>42.3.1</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'org.postgresql:postgresql:42.3.1'
```
:::
::::

You can interact with the database like this:

```java
import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.jdbc.JdbcJmeterDsl.*;

import java.io.IOException;
import java.sql.Types;
import java.time.Duration;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.postgresql.Driver;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.jdbc.DslJdbcSampler;

public class PerformanceTest {

  @Test
  public void testPerformance() throws IOException {
    String jdbcPoolName = "pgLocalPool";
    String productName = "dsltest-prod";
    DslJdbcSampler cleanUpSampler = jdbcSampler(jdbcPoolName,
        "DELETE FROM products WHERE name = '" + productName + "'")
        .timeout(Duration.ofSeconds(10));
    TestPlanStats stats = testPlan(
        jdbcConnectionPool(jdbcPoolName, Driver.class, "jdbc:postgresql://localhost/my_db")
            .user("user")
            .password("pass"),
        setupThreadGroup(
            cleanUpSampler
        ),
        threadGroup(5, 10,
            httpSampler("CreateProduct", "http://my.service/products")
                .post("{\"name\", \"" + productName + "\"}", ContentType.APPLICATION_JSON),
            jdbcSampler("GetProductsIdsByName", jdbcPoolName,
                "SELECT id FROM products WHERE name=?")
                .param(productName, Types.VARCHAR)
                .vars("PRODUCT_ID")
                .timeout(Duration.ofSeconds(10)),
            httpSampler("GetLatestProduct",
                "http://my.service/products/${__V(PRODUCT_ID_${PRODUCT_ID_#})}")
        ),
        teardownThreadGroup(
            cleanUpSampler
        )
    ).run();
    assertThat(stats.overall().sampleTimePercentile99()).isLessThan(Duration.ofSeconds(5));
  }

}
```

::: tip
Always specify a query timeout to quickly identify unexpected behaviors in queries.
:::

::: tip
Don't forget proper `WHERE` conditions in `UPDATES` and `DELETES`, and proper indexes for table columns participating in `WHERE` conditions 😊.
:::

Check [JdbcJmeterDsl](/jmeter-java-dsl-jdbc/src/main/java/us/abstracta/jmeter/javadsl/jdbc/JdbcJmeterDsl.java) for additional details and options and [JdbcJmeterDslTest](/jmeter-java-dsl-jdbc/src/test/java/us/abstracta/jmeter/javadsl/jdbc/JdbcJmeterDslTest.java) for additional examples.
