package us.abstracta.jmeter.javadsl.jdbc;

import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.jdbc.JdbcJmeterDsl.jdbcConnectionPool;

import java.sql.Connection;
import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.postgresql.Driver;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class DslJdbcConnectionPoolTest {

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    protected CodeBuilderTest() {
      codeGenerator.addBuildersFrom(JdbcJmeterDsl.class);
    }

    public DslTestPlan testPlanWithConnectionPool() {
      return testPlan(
          jdbcConnectionPool("myPool", Driver.class, "jdbc:postgresql://localhost/my_db")
      );
    }

    public DslTestPlan testPlanWithConnectionPoolAndNonDefaultSettngs() {
      return testPlan(
          jdbcConnectionPool("myPool", Driver.class, "jdbc:postgresql://localhost/my_db")
              .user("myUser")
              .password("myPassword")
              .autoCommit(false)
              .maxConnections(10)
              .maxConnectionWait(Duration.ofSeconds(5))
              .transactionIsolation(Connection.TRANSACTION_NONE)
      );
    }

  }

}
