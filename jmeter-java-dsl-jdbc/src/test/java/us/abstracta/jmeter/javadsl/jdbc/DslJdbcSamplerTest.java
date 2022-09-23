package us.abstracta.jmeter.javadsl.jdbc;

import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.jdbc.JdbcJmeterDsl.jdbcSampler;

import java.sql.Types;
import java.time.Duration;
import org.junit.jupiter.api.Nested;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.jdbc.DslJdbcSampler.JdbcParamMode;
import us.abstracta.jmeter.javadsl.jdbc.DslJdbcSampler.QueryType;

public class DslJdbcSamplerTest {

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    protected CodeBuilderTest() {
      codeGenerator.addBuildersFrom(JdbcJmeterDsl.class);
    }

    public DslTestPlan testPlanWithJdbcSamplerInsert() {
      return testPlan(
          threadGroup(1, 1,
              jdbcSampler("pool",
                  "INSERT INTO users(name, age) VALUES (?, ?)")
                  .param("user", Types.VARCHAR)
                  .param("user, los", Types.VARCHAR)
                  .param("user\" los", Types.VARCHAR)
                  .param(23, Types.INTEGER)
                  .param(false, Types.BOOLEAN)
                  .param(5.6, Types.DOUBLE)
          )
      );
    }

    public DslTestPlan testPlanWithJdbcSamplerSelect() {
      return testPlan(
          threadGroup(1, 1,
              jdbcSampler("pool", "SELECT name, user FROM users")
                  .vars("USER_NAME", "USER_AGE")
                  .timeout(Duration.ofSeconds(5))
          )
      );
    }

    public DslTestPlan testPlanWithJdbcSamplerCall() {
      return testPlan(
          threadGroup(1, 1,
              jdbcSampler("pool", "{CALL incr(?)}")
                  .param(1, Types.INTEGER, JdbcParamMode.INOUT)
                  .vars("INCR")
          )
      );
    }

    public DslTestPlan testPlanWithJdbcSamplerCommit() {
      return testPlan(
          threadGroup(1, 1,
              jdbcSampler("pool", "")
                  .commit()
          )
      );
    }

    public DslTestPlan testPlanWithJdbcSamplerRollback() {
      return testPlan(
          threadGroup(1, 1,
              jdbcSampler("pool", "")
                  .rollback()
          )
      );
    }

    public DslTestPlan testPlanWithJdbcSamplerAutoCommitTrue() {
      return testPlan(
          threadGroup(1, 1,
              jdbcSampler("pool", "")
                  .autoCommit(true)
          )
      );
    }

    public DslTestPlan testPlanWithJdbcSamplerAutoCommitFalse() {
      return testPlan(
          threadGroup(1, 1,
              jdbcSampler("pool", "")
                  .autoCommit(false)
          )
      );
    }

    public DslTestPlan testPlanWithJdbcSamplerNonMatchingQueryType() {
      return testPlan(
          threadGroup(1, 1,
              jdbcSampler("pool", "SELECT * FROM USER")
                  .queryType(QueryType.UPDATE)
          )
      );
    }

  }

}
