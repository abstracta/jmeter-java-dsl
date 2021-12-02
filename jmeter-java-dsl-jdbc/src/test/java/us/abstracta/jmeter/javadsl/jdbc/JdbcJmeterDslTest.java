package us.abstracta.jmeter.javadsl.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223Sampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.jdbc.JdbcJmeterDsl.jdbcConnectionPool;
import static us.abstracta.jmeter.javadsl.jdbc.JdbcJmeterDsl.jdbcSampler;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.Test;
import org.postgresql.Driver;
import org.testcontainers.containers.PostgreSQLContainer;
import us.abstracta.jmeter.javadsl.jdbc.DslJdbcSampler.JdbcParamMode;

public class JdbcJmeterDslTest {

  private static final String POOL_NAME = "myPool";
  private static final String USER_VARS_PREFIX = "USER_";
  private static final String USER_ID_VAR = USER_VARS_PREFIX + "ID";
  private static final String USER_NAME_VAR = USER_VARS_PREFIX + "NAME";
  private static final String USER_AGE_VAR = USER_VARS_PREFIX + "AGE";
  private static final String USER_ADDRESS_VAR = USER_VARS_PREFIX + "ADDRESS";
  private static final String SELECT_USERS = "SELECT id, name, age, address FROM users";

  @Test
  public void shouldGetInsertedRecordWhenTestPlanWithJdbcSamplersInsertingAndRetrieving()
      throws Exception {
    try (PostgreSQLContainer<?> postgres = buildPostgresContainer()) {
      postgres.start();
      Map<String, Object> vars = new HashMap<>();
      /*
       we use a name and address with different convention that others to test proper transformation
       */
      String userName = null;
      String userAddress = "My address3, is \"your address\"";
      testPlan(
          threadGroup(1, 1,
              buildJdbcPool(postgres, POOL_NAME),
              jdbcSampler(POOL_NAME,
                  "INSERT INTO users(name, age, address) VALUES (?, ?, ?)")
                  .param(userName, Types.VARCHAR)
                  .param(23, Types.INTEGER)
                  .param(userAddress, Types.VARCHAR),
              jdbcSampler(POOL_NAME, SELECT_USERS + " WHERE id > ?")
                  .vars(USER_ID_VAR, USER_NAME_VAR, USER_AGE_VAR, USER_ADDRESS_VAR)
                  .param(1, Types.INTEGER),
              jsr223Sampler(s -> vars.putAll(extractJMeterUserVars(s.vars)))
          )
      ).run();
      Map<String, Object> expectedMap = buildExpectedVarsMap(2, 3);
      expectedMap.put(buildRecordFieldVar(USER_NAME_VAR, "2"), userName);
      expectedMap.put(buildRecordFieldVar(USER_ADDRESS_VAR, "2"), userAddress);
      assertThat(vars).isEqualTo(expectedMap);
    }
  }

  private PostgreSQLContainer<?> buildPostgresContainer() {
    return new PostgreSQLContainer<>("postgres:9.6.12")
        .withInitScript("init.sql");
  }

  private DslJdbcConnectionPool buildJdbcPool(PostgreSQLContainer<?> postgres, String poolName) {
    return jdbcConnectionPool(poolName, Driver.class, postgres.getJdbcUrl())
        .user(postgres.getUsername())
        .password(postgres.getPassword());
  }

  private Map<String, Object> extractJMeterUserVars(JMeterVariables vars) {
    return vars.entrySet().stream()
        .filter(e -> e.getKey().startsWith(USER_VARS_PREFIX))
        // need to use this approach due to https://bugs.openjdk.java.net/browse/JDK-8148463
        .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
  }

  private Map<String, Object> buildExpectedVarsMap(int fromId, int toId) {
    HashMap<String, Object> ret = new HashMap<>();
    int recordsCount = toId - fromId + 1;
    String recordsCountStr = String.valueOf(recordsCount);
    ret.putAll(buildRecordVarsMap("#", recordsCountStr, recordsCountStr, recordsCountStr,
        recordsCountStr));
    for (int i = 0; i < recordsCount; i++) {
      int userId = fromId + i;
      ret.putAll(
          buildRecordVarsMap(String.valueOf(i + 1), String.valueOf(userId), buildUserName(userId),
              String.valueOf(buildUserAge(userId)), buildUserAddress(userId)));
    }
    return ret;
  }

  private Map<String, Object> buildRecordVarsMap(String varsSuffix, String userId, String userName,
      String userAge,
      String userAddress) {
    Map<String, Object> ret = new HashMap<>();
    ret.put(buildRecordFieldVar(USER_ID_VAR, varsSuffix), userId);
    ret.put(buildRecordFieldVar(USER_NAME_VAR, varsSuffix), userName);
    ret.put(buildRecordFieldVar(USER_AGE_VAR, varsSuffix), userAge);
    ret.put(buildRecordFieldVar(USER_ADDRESS_VAR, varsSuffix), userAddress);
    return ret;
  }

  private String buildRecordFieldVar(String fieldVarName, String recordId) {
    return fieldVarName + "_" + recordId;
  }

  private String buildUserName(int userId) {
    return "User" + userId;
  }

  private int buildUserAge(int userId) {
    return 20 + userId;
  }

  private String buildUserAddress(int userId) {
    return "My address" + userId;
  }

  @Test
  public void shouldGetExistingRecordsInResultsVarWhenTestPlanWithSelectSamplerWithResultsVar()
      throws Exception {
    try (PostgreSQLContainer<?> postgres = buildPostgresContainer()) {
      postgres.start();
      List<Map<String, Object>> vars = new ArrayList<>();
      String resultsVarName = "USERS";
      testPlan(
          threadGroup(1, 1,
              buildJdbcPool(postgres, POOL_NAME),
              jdbcSampler(POOL_NAME, SELECT_USERS)
                  .resultsVar(resultsVarName),
              jsr223Sampler(
                  s -> vars.addAll((List<Map<String, Object>>) s.vars.getObject(resultsVarName)))
          )
      ).run();
      assertThat(vars).isEqualTo(buildExpectedResultsVar(1, 2));
    }
  }

  private List<Map<String, Object>> buildExpectedResultsVar(int fromId, int toId) {
    List<Map<String, Object>> ret = new ArrayList<>();
    for (int i = 0; i < toId - fromId + 1; i++) {
      ret.add(buildRecordMap(fromId + i));
    }
    return ret;
  }

  private Map<String, Object> buildRecordMap(int id) {
    Map<String, Object> ret = new HashMap<>();
    ret.put("id", id);
    ret.put("name", buildUserName(id));
    ret.put("age", buildUserAge(id));
    ret.put("address", buildUserAddress(id));
    return ret;
  }

  @Test
  public void shouldGetIncrementedValueWhenJdbcSamplerWithInOutFunctionCall() throws Exception {
    try (PostgreSQLContainer<?> postgres = buildPostgresContainer()) {
      postgres.start();
      // using atomic integer just as a holder
      AtomicInteger val = new AtomicInteger();
      String varName = "VAR";
      testPlan(
          threadGroup(1, 1,
              buildJdbcPool(postgres, POOL_NAME),
              jdbcSampler(POOL_NAME, "{CALL incr(?)}")
                  .param("1", Types.INTEGER, JdbcParamMode.INOUT)
                  .vars(varName),
              jsr223Sampler(s -> val.set(Integer.parseInt(s.vars.get(varName))))
          )
      ).run();
      assertThat(val.get()).isEqualTo(2);
    }
  }

}
