package us.abstracta.jmeter.javadsl.jdbc;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.samplers.BaseSampler;

/**
 * Allows interacting with databases through configured JDBC connections.
 * <p>
 * This sampler currently does not require to specify the type of query to send to the database (as
 * JMeter element does), and calculates the proper type from query string and defined parameters.
 *
 * @see DslJdbcConnectionPool
 * @since 0.38
 */
public class DslJdbcSampler extends BaseSampler<DslJdbcSampler> {

  private final String poolName;
  private final String query;
  private final List<QueryParameter> params = new ArrayList<>();
  private final List<String> vars = new ArrayList<>();
  private String resultsVar;
  private Duration timeout;
  private QueryType queryType;

  public DslJdbcSampler(String name, String poolName, String query) {
    super(name != null ? name : "", TestBeanGUI.class);
    this.poolName = poolName;
    this.query = query;
  }

  private static class QueryParameter {

    private static final Map<Integer, String> JDBC_TYPE_TO_PROPERTY_VALUE =
        buildJdbcTypeToPropertyValueMapping();

    private final Object value;
    private final int jdbcType;
    private final JdbcParamMode mode;

    private QueryParameter(Object value, int jdbcType, JdbcParamMode mode) {
      this.value = value;
      this.jdbcType = jdbcType;
      this.mode = mode;
    }

    private static Map<Integer, String> buildJdbcTypeToPropertyValueMapping() {
      HashMap<Integer, String> ret = new HashMap<>();
      Field[] fields = java.sql.Types.class.getFields();
      try {
        for (Field field : fields) {
          String name = field.getName();
          Integer value = (Integer) field.get(null);
          ret.put(value, name.toLowerCase(java.util.Locale.ENGLISH));
        }
        return ret;
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    private String getTypePropertyValue() {
      String propVal = JDBC_TYPE_TO_PROPERTY_VALUE.get(jdbcType);
      if (propVal == null) {
        // this is required because some databases define their own types
        propVal = String.valueOf(jdbcType);
      }
      return mode == JdbcParamMode.IN ? propVal : mode.name() + " " + propVal;
    }

  }

  /**
   * Specifies the mode to apply to a given query parameter.
   */
  public enum JdbcParamMode {
    /**
     * The parameter is only used by database query to read a value.
     * <p>
     * This is the default mode of parameters when none is specified.
     */
    IN,
    /**
     * The parameter value is altered by the database query, and used as a resulting output of the
     * query.
     */
    OUT,
    /**
     * The parameter value is used to read a value by the query and is altered containing a
     * resulting output.
     */
    INOUT
  }

  /**
   * Specifies the type of query to use the JDBC driver with.
   * <p>
   * In general this should be auto-detected, but, when auto-detection fails to properly identify
   * the type, then you can manually specify the type through
   * {@link DslJdbcSampler#queryType(QueryType)} method.
   */
  public enum QueryType {
    /**
     * Identifies simple select statement with no parameters as placeholders.
     * <p>
     * This query type is usually used in combination with {@link DslJdbcSampler#vars(String...)}
     * and/or {@link DslJdbcSampler#resultsVar}.
     */
    SELECT("Select Statement"),
    /**
     * Identifies simple insert, update, delete or similar database altering query with no
     * parameters as placeholders.
     */
    UPDATE("Update Statement"),
    /**
     * Identifies callable statement with potential input, output or inout parameters. For example
     * functions, stored procedures, etc.
     * <p>
     * This type of query usually requires setting parameters through {@link
     * DslJdbcSampler#param(Object, int)} or {@link DslJdbcSampler#param(Object, int,
     * JdbcParamMode)} and potentially getting results through
     * {@link DslJdbcSampler#vars(String...)} and/or {@link DslJdbcSampler#resultsVar}.
     */
    CALLABLE("Callable Statement"),
    /**
     * Same as {@link QueryType#SELECT} but with parameters set through placeholders ("?" symbol).
     * <p>
     * This type of query requires setting parameters through {@link DslJdbcSampler#param(Object,
     * int)}.
     */
    PREPARED_SELECT("Prepared Select Statement"),
    /**
     * Same as {@link QueryType#UPDATE} but with parameters set through placeholders ("?" symbol).
     * <p>
     * This type of query requires setting parameters through {@link DslJdbcSampler#param(Object,
     * int)}.
     */
    PREPARED_UPDATE("Prepared Update Statement"),
    /**
     * Identifies a commit statement. This is the same as {@link DslJdbcSampler#commit()}
     */
    COMMIT("Commit"),
    /**
     * Identifies a rollback statement. This is the same as {@link DslJdbcSampler#rollback()}
     */
    ROLLBACK("Rollback"),
    /**
     * Disables auto-commits. This is the same as {@link DslJdbcSampler#autoCommit(boolean)} with
     * false argument.
     */
    AUTO_COMMIT_FALSE("AutoCommit(false)"),
    /**
     * Enables auto-commits. This is the same as {@link DslJdbcSampler#autoCommit(boolean)} with
     * true argument.
     */
    AUTO_COMMIT_TRUE("AutoCommit(true)");

    private final String propertyValue;

    QueryType(String propertyValue) {
      this.propertyValue = propertyValue;
    }

  }

  /**
   * Allows specifying a parameter value to pass to the query and used in provided query
   * placeholders ("?" symbol).
   * <p>
   * In general, prefer using this instead of using JMeter variable references or expressions inside
   * query, since is more performance and safer.
   * <p>
   * In the end, this just specifies parameters to pass to a prepared statement generated from the
   * given query string.
   * <p>
   * There is no need to replace nulls with special values or have any special consideration for
   * strings containing commas and quotes since the DSL abstracts such details.
   *
   * @param value         specifies the actual value to set on the parameter.
   * @param jdbcParamType sets the type of the parameter, being the value one of {@link
   *                      java.sql.Types} defined ones, or a specific one for the database that is
   *                      not included in Types class.
   * @return the DslJdbcSampler for further configuration or usage.
   * @see java.sql.Types
   */
  public DslJdbcSampler param(Object value, int jdbcParamType) {
    return param(value, jdbcParamType, JdbcParamMode.IN);
  }

  /**
   * Same as {@link #param(Object, int)} but allowing also to specify parameter mode.
   * <p>
   * When mode is not specified, then {@link JdbcParamMode#IN} is used.
   * <p>
   * Remember specifying a parameter for each of query placeholders ('?'), even if is an OUT
   * parameter.
   *
   * @param value         specifies the actual value to set on the parameter.
   * @param jdbcParamType sets the type of the parameter, being the value one of {@link
   *                      java.sql.Types} defined ones, or a specific one for the database that is
   *                      not included in Types class.
   * @param mode          specifies the {@link JdbcParamMode} for the parameter.
   * @return the DslJdbcSampler for further configuration or usage.
   * @see #param(Object, int)
   * @see JdbcParamMode
   */
  public DslJdbcSampler param(Object value, int jdbcParamType, JdbcParamMode mode) {
    params.add(new QueryParameter(value, jdbcParamType, mode));
    return this;
  }

  /**
   * Allows specifying the name prefixes of variables where to store query retrieved row columns.
   * <p>
   * JMeter adds a suffix to each variable with the row number, starting with 1 (eg: if the name of
   * the variable is MY_FIELD, then the first row column value will be MY_FIELD_1). Additionally,
   * JMeter creates a variable with "_#" suffix which includes the number of returned values/rows
   * (eg: MY_FIELD_#).
   * <p>
   * Alternatively you may use {@link #resultsVar} for getting a list of row maps.
   *
   * @param vars specifies variables names prefixes to use for each returned query column or
   *             function/procedure output parameter.
   * @return the DslJdbcSampler for further configuration or usage.
   */
  public DslJdbcSampler vars(String... vars) {
    this.vars.addAll(Arrays.asList(vars));
    return this;
  }

  /**
   * Allows specifying the name of a variable where to store a list of query retrieved row maps.
   * <p>
   * Each element in the list will contain a map where the key is the column name or alias, or
   * parameter output name, and the value is the associated value.
   *
   * @param resultsVar specifies the name of the variable where to store the list of row maps.
   * @return the DslJdbcSampler for further configuration or usage.
   */
  public DslJdbcSampler resultsVar(String resultsVar) {
    this.resultsVar = resultsVar;
    return this;
  }

  /**
   * Allows specifying a maximum amount of time a query can take before it fails.
   *
   * <b>Warning:</b> by default the timeout is not set, which differs from default JMeter component
   * default behavior that sets it to 0 (infinity timeout). We decided to do this since we consider
   * setting an infinity timeout to be a bad practices and additionally some database drivers might
   * not support setting one.
   * <p>
   * You should set the timeout with a big enough value that covers all expected scenarios, but also
   * low enough to quickly detect any potential abnormal behavior.
   *
   * @param timeout specifies the timeout duration.
   *                <p>
   *                By default, no timeout will be set, which might use the default timeout for the
   *                driver, connection, database. This also avoid issues with drivers that don't
   *                support a query timeout. When set to 0, then an infinity timeout will be
   *                considered (not recommended).
   *                <p>
   *                Since JDBC only supports specifying times in seconds, if you specify a smaller
   *                granularity (like milliseconds) it will be rounded up to seconds.
   * @return the DslJdbcSampler for further configuration or usage.
   */
  public DslJdbcSampler timeout(Duration timeout) {
    this.timeout = timeout;
    return this;
  }

  /**
   * Allows committing changes applied by previous queries when auto-commit is disabled on the
   * connection pool.
   * <p>
   * When this method is used, query string is ignored (so you can set it to null).
   *
   * @return the DslJdbcSampler for further configuration or usage.
   */
  public DslJdbcSampler commit() {
    this.queryType = QueryType.COMMIT;
    return this;
  }

  /**
   * Allows undoing changes done by previous queries when auto-commit is disabled on the connection
   * pool.
   * <p>
   * When this method is used, query string is ignored (so you can set it to null).
   *
   * @return the DslJdbcSampler for further configuration or usage.
   */
  public DslJdbcSampler rollback() {
    this.queryType = QueryType.ROLLBACK;
    return this;
  }

  /**
   * Allows enabling or disabling auto-commits on the connection pool.
   * <p>
   * When this method is used, query string is ignored (so you can set it to null).
   *
   * @param enabled specifies to enable auto-commits when set to true, or disable them otherwise.
   * @return the DslJdbcSampler for further configuration or usage.
   */
  public DslJdbcSampler autoCommit(boolean enabled) {
    this.queryType = enabled ? QueryType.AUTO_COMMIT_TRUE : QueryType.AUTO_COMMIT_FALSE;
    return this;
  }

  /**
   * Allows to explicitly specify the query type when auto-detection is not enough.
   * <p>
   * In general this method should not be needed to invoke, but in some cases auto-detection may
   * fail, and this method allows you to explicitly specify it.
   *
   * @param queryType the type of query to assign to use with the JDBC Driver.
   * @return the DslJdbcSampler for further configuration or usage.
   * @see QueryType
   */
  public DslJdbcSampler queryType(QueryType queryType) {
    this.queryType = queryType;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    JDBCSampler ret = new JDBCSampler();
    ret.setQueryType(getQueryType().propertyValue);
    ret.setDataSource(poolName);
    ret.setQuery(query);
    ret.setQueryArguments(params.stream()
        .map(this::extractParamValue)
        .collect(Collectors.joining(",")));
    ret.setQueryArgumentsTypes(params.stream()
        .map(QueryParameter::getTypePropertyValue)
        .collect(Collectors.joining(",")));
    ret.setVariableNames(String.join(",", vars));
    ret.setResultVariable(resultsVar);
    ret.setQueryTimeout(timeout != null ? String.valueOf(durationToSeconds(timeout)) : "-1");
    return ret;
  }

  private QueryType getQueryType() {
    if (queryType != null) {
      return queryType;
    }
    if (query == null) {
      throw new IllegalStateException(
          "Query can only be null when using commit, rollback or autoCommit");
    }
    String queryType = query.trim();
    queryType = queryType.substring(0, queryType.indexOf(" ")).toLowerCase(Locale.US);
    if ("select".equals(queryType)) {
      return params.isEmpty() ? QueryType.SELECT : QueryType.PREPARED_SELECT;
    } else if ("insert".equals(queryType) || "update".equals(queryType) || "delete".equals(
        queryType)) {
      return params.isEmpty() ? QueryType.UPDATE : QueryType.PREPARED_UPDATE;
    } else {
      return QueryType.CALLABLE;
    }
  }

  private String extractParamValue(QueryParameter p) {
    if (p.value == null) {
      return "]NULL[";
    }
    String strValue = p.value.toString();
    if (strValue.contains(",") || strValue.contains("\"")) {
      return "\"" + strValue.replace("\"", "\"\"") + "\"";
    }
    return strValue;
  }

}
