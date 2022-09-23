package us.abstracta.jmeter.javadsl.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.jmeter.protocol.jdbc.config.DataSourceElement;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.core.configs.BaseConfigElement;

/**
 * Defines a JDBC connection pool used by other elements to interact with a database.
 *
 * @since 0.38
 */
public class DslJdbcConnectionPool extends BaseConfigElement {

  private static final Map<Integer, String> TRANSACTION_ISOLATION_TO_PROPERTY_VALUE =
      buildTransactionIsolationToPropertyValueMapping();
  private static final Duration DEFAULT_MAX_CONNECTION_WAIT = Duration.ofSeconds(10);
  private static final int DEFAULT_TRANSACTION_ISOLATION = -1;
  private static final String DEFAULT_PROPERTY_VALUE = "DEFAULT";

  protected Class<? extends Driver> driverClass;
  protected String url;
  protected String user;
  protected String password;
  protected boolean autoCommit = true;
  protected int maxConnections;
  protected Duration maxConnectionWait = DEFAULT_MAX_CONNECTION_WAIT;
  protected int transactionIsolation = DEFAULT_TRANSACTION_ISOLATION;

  public DslJdbcConnectionPool(String name, Class<? extends Driver> driverClass, String url) {
    super(name, TestBeanGUI.class);
    this.driverClass = driverClass;
    this.url = url;
  }

  private static Map<Integer, String> buildTransactionIsolationToPropertyValueMapping() {
    HashMap<Integer, String> ret = new HashMap<>();
    ret.put(DEFAULT_TRANSACTION_ISOLATION, DEFAULT_PROPERTY_VALUE);
    ret.put(Connection.TRANSACTION_NONE, "TRANSACTION_NONE");
    ret.put(Connection.TRANSACTION_READ_COMMITTED, "TRANSACTION_READ_COMMITTED");
    ret.put(Connection.TRANSACTION_READ_UNCOMMITTED, "TRANSACTION_READ_UNCOMMITTED");
    ret.put(Connection.TRANSACTION_REPEATABLE_READ, "TRANSACTION_REPEATABLE_READ");
    ret.put(Connection.TRANSACTION_SERIALIZABLE, "TRANSACTION_SERIALIZABLE");
    return ret;
  }

  /**
   * Allows setting the username required to connect to the database.
   *
   * @param user is the username to connect to the database with.
   * @return the config element for further configuration or usage.
   */
  public DslJdbcConnectionPool user(String user) {
    this.user = user;
    return this;
  }

  /**
   * Allows setting the password required to connect to the database.
   *
   * @param password is the password to connect to the database with.
   * @return the config element for further configuration or usage.
   */
  public DslJdbcConnectionPool password(String password) {
    this.password = password;
    return this;
  }

  /**
   * Allows setting if auto-commit is enabled or not in pool connections.
   * <p>
   * When enabled, auto-commits avoid having to explicitly commit each modification query (insert,
   * update, delete, etc.), but on the other hand, when doing several queries in batch, it is not as
   * performant as committing several queries at once. Additionally, you might want to disable
   * auto-commit when you want a set of queries to execute in transaction mode (rolling back
   * previous modifications on some particular conditions).
   * <p>
   * When auto-commit is disabled, you will need to use {@link DslJdbcSampler#commit()} for changes
   * to take effect, and might optionally use {@link DslJdbcSampler#rollback()} to cancel the
   * transaction.
   *
   * @param enabled specifies whether auto-commit is enabled or disabled by default. The connection
   *                behavior can be changed at any point in time by
   *                {@link DslJdbcSampler#autoCommit(boolean)}. By default, this is enabled.
   * @return the config element for further configuration or usage.
   */
  public DslJdbcConnectionPool autoCommit(boolean enabled) {
    this.autoCommit = enabled;
    return this;
  }

  /**
   * Allows setting max number of connections to keep with the database.
   * <p>
   * The number of connections has a direct impact on database performance and in JMeter required
   * memory. In general the default setting, 0, is good enough, assigning one connection to each
   * JMeter thread. If you set it to another value then the pool of connections will be shared by
   * all threads in JMeter and you should be careful to set it to a number that avoids threads
   * waiting for each other on a connection to be available (eg: setting it to the max number of
   * threads).
   *
   * @param maxConnections specifies the maximum number of connections to use to connect to the
   *                       Database. 0 means one connection per thread. When set to a value
   *                       different from 0, then connections are shared by JMeter threads.
   * @return the config element for further configuration or usage.
   */
  public DslJdbcConnectionPool maxConnections(int maxConnections) {
    this.maxConnections = maxConnections;
    return this;
  }

  /**
   * Specifies the time to wait for a connection to be available.
   * <p>
   * When this time is surpassed waiting for a connection, then an error will be generated.
   * <p>
   * Is usually a good practice to set this to a value that can detect potential unexpected
   * behavior. I.e.: set it to a value that is big enough for usual behavior not to fail, but low
   * enough for detecting unusual/unexpected behavior.
   *
   * @param maxConnectionWait duration to wait for a connection until an error arises. By default,
   *                          this is set to 10 seconds. If set to 0 seconds, it means that it will
   *                          wait indefinitely for the connection (which is not advisable).
   * @return the config element for further configuration or usage.
   */
  public DslJdbcConnectionPool maxConnectionWait(Duration maxConnectionWait) {
    this.maxConnectionWait = maxConnectionWait;
    return this;
  }

  /**
   * Allows specifying the transaction isolation level to use for queries executed by this pool.
   * <p>
   * Transaction isolation level are usually required to be tuned either to improve performance or
   * avoid potential conflicts between concurrent queries.
   *
   * @param transactionIsolation specifies a transaction level which value is -1 or one of
   *                             {@link Connection#TRANSACTION_NONE},
   *                             {@link Connection#TRANSACTION_READ_COMMITTED},
   *                             {@link Connection#TRANSACTION_READ_UNCOMMITTED},
   *                             {@link Connection#TRANSACTION_REPEATABLE_READ} or
   *                             {@link Connection#TRANSACTION_SERIALIZABLE}. By default is set to
   *                             -1, which means that it will use the default level for the
   *                             connection string, session or database.
   * @return the config element for further configuration or usage.
   * @see java.sql.Connection
   */
  public DslJdbcConnectionPool transactionIsolation(int transactionIsolation) {
    this.transactionIsolation = transactionIsolation;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    DataSourceElement ret = new DataSourceElement();
    ret.setDataSource(name);
    ret.setDriver(driverClass.getName());
    ret.setDbUrl(url);
    ret.setUsername(user);
    ret.setPassword(password);
    ret.setAutocommit(autoCommit);
    ret.setPoolMax(String.valueOf(maxConnections));
    ret.setTimeout(String.valueOf(maxConnectionWait.toMillis()));
    ret.setTransactionIsolation(transactionIsolationToPropertyValue(transactionIsolation));
    ret.setPreinit(true);
    return ret;
  }

  private String transactionIsolationToPropertyValue(int val) {
    /*
     this mapping is not strictly needed (since jmeter supports also int value), but makes
     resulting JMX easier to read.
     */
    return Optional.ofNullable(TRANSACTION_ISOLATION_TO_PROPERTY_VALUE.get(val))
        .orElseThrow(() -> new IllegalArgumentException(
            "Unknown transaction level " + val + " for pool " + name));
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<DataSourceElement> {

    public CodeBuilder(List<Method> builderMethods) {
      super(DataSourceElement.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(DataSourceElement testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      return buildMethodCall(paramBuilder.stringParam("dataSource"),
          new ClassParam(testElement.getPropertyAsString("driver")),
          paramBuilder.stringParam("dbUrl"))
          .chain("user", paramBuilder.stringParam("username"))
          .chain("password", paramBuilder.stringParam("password"))
          .chain("autoCommit", paramBuilder.boolParam("autocommit", true))
          .chain("maxConnections", paramBuilder.intParam("poolMax", 0))
          .chain("maxConnectionWait",
              paramBuilder.durationParamMillis("timeout", DEFAULT_MAX_CONNECTION_WAIT))
          .chain("transactionIsolation", new TransactionIsolationParam(
              testElement.getPropertyAsString("transactionIsolation")));
    }

  }

  private static class ClassParam extends MethodParam {

    private final String className;

    private ClassParam(String className) {
      super(Class.class, null);
      this.className = className;
    }

    @Override
    public Set<String> getImports() {
      return Collections.singleton(className);
    }

    @Override
    protected String buildCode(String indent) {
      return className.substring(className.lastIndexOf(".") + 1) + ".class";
    }

  }

  private static class TransactionIsolationParam extends MethodParam {

    protected TransactionIsolationParam(String expression) {
      super(int.class, expression);
    }

    @Override
    public boolean isDefault() {
      return expression == null || DEFAULT_PROPERTY_VALUE.equals(expression);
    }

    @Override
    public Set<String> getImports() {
      return DEFAULT_PROPERTY_VALUE.equals(expression) ? Collections.emptySet()
          : Collections.singleton(Connection.class.getName());
    }

    @Override
    protected String buildCode(String indent) {
      return DEFAULT_PROPERTY_VALUE.equals(expression) ? "-1" : "Connection." + expression;
    }

  }

}
