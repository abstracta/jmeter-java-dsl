package us.abstracta.jmeter.javadsl.jdbc;

import java.sql.Driver;

/**
 * Includes builder methods for JDBC test elements which allow interacting with databases.
 *
 * @since 0.38
 */
public class JdbcJmeterDsl {

  private JdbcJmeterDsl() {
  }

  /**
   * Builds a JDBC connection pool which is used by other test elements to interact with a
   * database.
   *
   * @param name        is the name assigned to the pool, and used by other test elements to use the
   *                    configured pool. This name should be unique for the pool, and is directly
   *                    tied to a JMeter thread variable
   * @param driverClass specifies the JDBC Driver class, specific for the particular database to
   *                    connect to. To specify a proper value, you will need to add the JDBC
   *                    implementation jar library for the specific database in the classpath (as
   *                    project test dependency).
   * @param url         the connection string used to connect to the database through the JDBC
   *                    driver. This URL could include additional connection properties that may be
   *                    required for the database (for example, specifying a default encoding).
   * @return the DslJdbcConnectionPool instance that can be further customized though fluent API or
   * added to a test plan.
   * @see DslJdbcConnectionPool
   */
  public static DslJdbcConnectionPool jdbcConnectionPool(String name,
      Class<? extends Driver> driverClass, String url) {
    return new DslJdbcConnectionPool(name, driverClass, url);
  }

  /**
   * Builds a JDBC sampler which allows interacting with a database (sending queries) through JDBC.
   *
   * @param poolName is a name of a previously defined {@link #jdbcConnectionPool(String, Class,
   *                 String)}, and specifies which connection pool to use to interact with the
   *                 database.
   * @param query    specifies the query string (it might be a prepared statement string) to send to
   *                 the database. You can pass null value when using {@link
   *                 DslJdbcSampler#autoCommit(boolean)}, {@link DslJdbcSampler#commit()} and {@link
   *                 DslJdbcSampler#rollback()} methods.
   * @return the DslJdbcSampler instance for further configuration or usage in test plan.
   * @see #jdbcConnectionPool(String, Class, String)
   * @see DslJdbcSampler
   */
  public static DslJdbcSampler jdbcSampler(String poolName, String query) {
    return new DslJdbcSampler(null, poolName, query);
  }

  /**
   * Is the same as {@link #jdbcSampler(String, String)} but allowing to set a name to the sampler
   * for easy identification in collected statistics and metrics.
   *
   * @param name     specifies the name to assign to the sampler.
   * @param poolName is a name of a previously defined {@link #jdbcConnectionPool(String, Class,
   *                 String)}, and specifies which connection pool to use to interact with the
   *                 database.
   * @param query    specifies the query string (it might be a prepared statement string) to send to
   *                 the database. You can pass null value when using {@link
   *                 DslJdbcSampler#autoCommit(boolean)}, {@link DslJdbcSampler#commit()} and {@link
   *                 DslJdbcSampler#rollback()} methods.
   * @return DslJdbcSampler
   */
  public static DslJdbcSampler jdbcSampler(String name, String poolName, String query) {
    return new DslJdbcSampler(name, poolName, query);
  }

}
