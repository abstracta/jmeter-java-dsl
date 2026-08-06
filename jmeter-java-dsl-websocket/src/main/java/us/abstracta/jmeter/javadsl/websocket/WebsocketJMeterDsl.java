package us.abstracta.jmeter.javadsl.websocket;

import eu.luminis.jmeter.wssampler.CloseWebSocketSampler;
import eu.luminis.jmeter.wssampler.CloseWebSocketSamplerGui;
import eu.luminis.jmeter.wssampler.DataPayloadType;
import eu.luminis.jmeter.wssampler.OpenWebSocketSampler;
import eu.luminis.jmeter.wssampler.OpenWebSocketSamplerGui;
import eu.luminis.jmeter.wssampler.SingleReadWebSocketSampler;
import eu.luminis.jmeter.wssampler.SingleReadWebSocketSampler.DataType;
import eu.luminis.jmeter.wssampler.SingleReadWebSocketSamplerGui;
import eu.luminis.jmeter.wssampler.SingleWriteWebSocketSampler;
import eu.luminis.jmeter.wssampler.SingleWriteWebSocketSamplerGui;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.samplers.BaseSampler;
import us.abstracta.jmeter.javadsl.http.JmeterUrl;

/**
 * Provides factory methods to create WebSocket samplers for performance
 * testing.
 * <p>
 * This class serves as the entry point for creating different types of
 * WebSocket operations:
 * <ul>
 * <li>{@link #websocketConnect(String)} - Establish a WebSocket connection</li>
 * <li>{@link #websocketWrite(String)} - Send messages to the server</li>
 * <li>{@link #websocketRead()} - Read responses from the server</li>
 * <li>{@link #websocketDisconnect()} - Close the WebSocket connection</li>
 * </ul>
 * <p>
 * Example usage:
 * 
 * <pre>{@code
 *import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
 *import static us.abstracta.jmeter.javadsl.websocket.DslWebsocketSampler.webSocketSampler;
 *import us.abstracta.jmeter.javadsl.core.TestPlanStats;
 * 
 *public class Test {
 * 
 *  public static void main(String[] args) throws Exception {
 *    TestPlanStats stats = testPlan(
 *      threadGroup(1, 1,
 *        webSocketSampler().connect("wss://server.com/websocket")
 *        webSocketSampler().write("Hello WebSocket!")
 *        webSocketSampler().read()
 *          .children(
 *            responseAssertion()
 *              .equalsToStrings("Hello WebSocket!")),
 *        webSocketSampler().disconnect()
 *      )
 *    ).run();
 *   }
 * }
 * }</pre>
 *
 * @since 2.2
 */
public class WebsocketJMeterDsl {

  private WebsocketJMeterDsl() {
  }

  private static boolean containsJmeterExpression(String value) {
    return value != null && value.contains("${");
  }

  /**
   * Creates a WebSocket connect sampler to establish a connection to the server.
   * <p>
   * After establishing the connection, use {@link #websocketWrite(String)} and
   * {@link #websocketRead()}
   * samplers to send and receive messages. Remember to close the connection using
   * {@link #websocketDisconnect()} when finished.
   * <p>
   * It could be also used alone to test the connection to the server.
   * <p>
   * <b>URL Format:</b> {@code ws://host:port/path?query} or
   * {@code wss://host:port/path?query}
   * <p>
   * The URL may be a fixed value or include JMeter expressions (variables or
   * functions) in its parts. For example:
   * <ul>
   * <li>{@code ws://localhost:8080/raw} - fixed URL</li>
   * <li>{@code ws://${HOST}:${PORT}/raw} - host and port from JMeter variables</li>
   * <li>{@code wss://${SERVER}/chat?token=${TOKEN}} - host, path and query with
   * expressions</li>
   * </ul>
   * The DSL parses the URL at build time into the WebSocket plugin fields (server,
   * port, path and TLS). JMeter then resolves expressions in each field at
   * runtime.
   * <p>
   * A full URL as a single JMeter expression (e.g. {@code ${URL}}) is not
   * supported: without a literal {@code ws://} or {@code wss://} scheme the URL
   * cannot be split into those fields. Prefer expressions in URL parts as shown
   * above, or use a Java variable when the full URL is known at plan build time.
   *
   * @param url the WebSocket server URL. Supported schemes: {@code ws://} (plain)
   *            and {@code wss://} (TLS). May contain JMeter expressions in host,
   *            port, path or query.
   * @return the connect sampler for further configuration or usage
   * @since 2.2
   */
  public static DslConnectSampler websocketConnect(String url) {
    return new DslConnectSampler(url);
  }

  /**
   * Creates WebSocket disconnect sampler to gracefully close the connection to
   * the
   * <p>
   * This sampler sends a close frame to the server and waits for the server's
   * close frame response. It is recommended to always close connections
   * explicitly to properly release resources.
   *
   * @return the disconnect sampler for further configuration or usage
   * @since 2.2
   */
  public static DslDisconnectSampler websocketDisconnect() {
    return new DslDisconnectSampler();
  }

  /**
   * Creates a WebSocket write sampler to send a text message to the server.
   * <p>
   * Requires an active WebSocket connection established via
   * {@link #websocketConnect(String)}.
   * 
   * @param requestData the message to send to the WebSocket server
   * @return the write sampler for further configuration or usage
   * @since 2.2
   */
  public static DslWriteSampler websocketWrite(String requestData) {
    return new DslWriteSampler(requestData);
  }

  /**
   * Same as {@link #websocketWrite(String)} but allowing to specify the payload data type.
   *
   * @param requestData the message to send to the WebSocket server
   * @param dataType    the payload data type
   * @return the write sampler for further configuration or usage
   * @since 2.2
   */
  public static DslWriteSampler websocketWrite(String requestData, PayloadDataType dataType) {
    return new DslWriteSampler(requestData, dataType);
  }

  /**
   * Same as {@link #websocketWrite(String, PayloadDataType)} but allowing to use JMeter
   * expressions (variables or functions) to solve the actual data type value.
   *
   * @param requestData the message to send to the WebSocket server
   * @param dataType    a JMeter expression that returns the payload data type
   * @return the write sampler for further configuration or usage
   * @since 2.2
   */
  public static DslWriteSampler websocketWrite(String requestData, String dataType) {
    return new DslWriteSampler(requestData, dataType);
  }

  /**
   * Creates a WebSocket read sampler to receive a message from the server.
   * <p>
   * By default, this sampler blocks execution until a response is received or
   * the timeout is reached. This behavior can be changed using
   * {@link DslReadSampler#waitForResponse(boolean)}.
   * <p>
   * Requires an active WebSocket connection established via
   * {@link #websocketConnect(String)}.
   * 
   * @return the read sampler for further configuration or usage
   * @since 2.2
   */
  public static DslReadSampler websocketRead() {
    return new DslReadSampler();
  }

  /**
   * Same as {@link #websocketRead()} but allowing to specify the data type to read from the
   * server.
   *
   * @param dataType the data type to read from the server
   * @return the read sampler for further configuration or usage
   * @since 2.2
   */
  public static DslReadSampler websocketRead(PayloadDataType dataType) {
    return new DslReadSampler(dataType);
  }

  /**
   * Same as {@link #websocketRead(PayloadDataType)} but allowing to use JMeter expressions
   * (variables or functions) to solve the actual data type value.
   *
   * @param dataType a JMeter expression that returns the data type to read from the server
   * @return the read sampler for further configuration or usage
   * @since 2.2
   */
  public static DslReadSampler websocketRead(String dataType) {
    return new DslReadSampler(dataType);
  }

  public enum PayloadDataType implements EnumParam.EnumPropertyValue {
    TEXT("Text"),
    BINARY("Binary");

    private final String propertyValue;

    PayloadDataType(String propertyValue) {
      this.propertyValue = propertyValue;
    }

    @Override
    public String propertyValue() {
      return propertyValue;
    }

    public static boolean isText(String value) {
      return value == null || TEXT.propertyValue().equalsIgnoreCase(value)
          || "text".equalsIgnoreCase(value);
    }

    public static boolean isValidDataType(String value) {
      return isText(value) || BINARY.propertyValue().equalsIgnoreCase(value)
          || "binary".equalsIgnoreCase(value);
    }
  }

  public static class DslConnectSampler extends BaseSampler<DslConnectSampler> {
    private String connectionTimeoutMillis;
    private String responseTimeoutMillis;
    private String server;
    private String port;
    private String path;
    private boolean tls = false;

    private DslConnectSampler(String url) {
      super("WebSocket Open Connection", OpenWebSocketSamplerGui.class);
      parseUrl(url);
    }

    private void parseUrl(String url) {
      JmeterUrl parsed = JmeterUrl.valueOf(url);
      String scheme = parsed.protocol();
      if (scheme != null && !containsJmeterExpression(scheme)) {
        if (!"ws".equals(scheme) && !"wss".equals(scheme)) {
          throw new IllegalArgumentException(
              "Invalid WebSocket URL. Must start with 'ws://' or 'wss://'");
        }
        tls = "wss".equals(scheme);
      }
      server = parsed.host();
      if ((server == null || server.isEmpty()) && scheme != null
          && !containsJmeterExpression(scheme)) {
        throw new IllegalArgumentException("Invalid WebSocket URL. Host is required");
      }
      String parsedPort = parsed.port();
      if (parsedPort == null || parsedPort.isEmpty()) {
        if (scheme != null && !containsJmeterExpression(scheme)) {
          port = tls ? "443" : "80";
        } else {
          port = "";
        }
      } else {
        port = parsedPort;
      }
      String parsedPath = parsed.path();
      path = (parsedPath == null || parsedPath.isEmpty()) ? "/" : parsedPath;
    }

    @Override
    protected TestElement buildTestElement() {
      OpenWebSocketSampler ret = new OpenWebSocketSampler();
      if (connectionTimeoutMillis != null) {
        ret.setConnectTimeout(connectionTimeoutMillis);
      }
      if (responseTimeoutMillis != null) {
        ret.setReadTimeout(responseTimeoutMillis);
      }
      ret.setTLS(tls);
      if (server != null) {
        ret.setServer(server);
      }
      if (port != null) {
        ret.setPort(port);
      }
      if (path != null) {
        ret.setPath(path);
      }
      return ret;
    }

    /**
     * Sets the connection timeout for the WebSocket connection creation.
     *
     * @param timeoutMillis the connection timeout in milliseconds (default value is
     *                      20000 milliseconds)
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler connectionTimeout(int timeoutMillis) {
      this.connectionTimeoutMillis = String.valueOf(timeoutMillis);
      return this;
    }

    /**
     * Same as {@link #connectionTimeout(int)} but allowing to use JMeter
     * expressions
     * (variables or
     * functions) to solve the actual parameter values.
     * 
     * @param timeoutMillis a JMeter expression that returns timeout in milliseconds
     *                      (default value is 20000 milliseconds)
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler connectionTimeout(String timeoutMillis) {
      this.connectionTimeoutMillis = timeoutMillis;
      return this;
    }

    /**
     * Sets the response timeout for the WebSocket negotiation.
     *
     * @param timeoutMillis the response timeout in milliseconds (default value is
     *                      6000 milliseconds)
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler responseTimeout(int timeoutMillis) {
      this.responseTimeoutMillis = String.valueOf(timeoutMillis);
      return this;
    }

    /**
     * Same as {@link #responseTimeout(int)} but allowing to use JMeter expressions
     * (variables or functions) to solve the actual parameter values.
     * 
     * @param timeoutMillis a JMeter expression that returns timeout in milliseconds
     *                      (default value is 6000 milliseconds)
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler responseTimeout(String timeoutMillis) {
      this.responseTimeoutMillis = timeoutMillis;
      return this;
    }

    public static class CodeBuilder extends SingleTestElementCallBuilder<OpenWebSocketSampler> {

      public CodeBuilder(List<Method> builderMethods) {
        super(OpenWebSocketSampler.class, builderMethods);
      }

      @Override
      protected MethodCall buildMethodCall(OpenWebSocketSampler testElement,
          MethodCallContext context) {
        TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
        MethodParam server = paramBuilder.stringParam("server", "");
        MethodParam port = paramBuilder.stringParam("port", "");
        MethodParam path = paramBuilder.stringParam("path", "");
        MethodParam tls = paramBuilder.boolParam("TLS", false);
        String protocol = tls.getExpression().equals("true") ? "wss" : "ws";
        String url = protocol + "://" + server.getExpression() + ":" + port.getExpression()
            + path.getExpression();
        return new MethodCall("websocketConnect", DslConnectSampler.class,
            new StringParam(url))
            .chain("connectionTimeout", paramBuilder.intParam("connectTimeout", 20000))
            .chain("responseTimeout", paramBuilder.intParam("readTimeout", 6000));
      }
    }
  }

  public enum StatusCode implements EnumParam.EnumPropertyValue {
    NORMAL_CLOSURE("1000"),
    GOING_AWAY("1001"),
    PROTOCOL_ERROR("1002"),
    UNSUPPORTED_DATA("1003"),
    NO_STATUS_CODE_PRESENT("1005"),
    MESSAGE_TYPE_ERROR("1007"),
    POLICY_VIOLATION("1008"),
    MESSAGE_TOO_BIG_ERROR("1009"),
    TLS_HANDSHAKE_ERROR("1015");

    private final String propertyValue;

    StatusCode(String propertyValue) {
      this.propertyValue = propertyValue;
    }

    @Override
    public String propertyValue() {
      return propertyValue;
    }

    public static boolean isValidStatusCode(String value) {
      return Arrays.stream(StatusCode.values())
          .anyMatch(code -> code.propertyValue().equals(value));
    }

  }

  public static class DslDisconnectSampler extends BaseSampler<DslDisconnectSampler> {
    private String responseTimeoutMillis;
    private String statusCode;

    private DslDisconnectSampler() {
      super("WebSocket Close", CloseWebSocketSamplerGui.class);
    }

    @Override
    protected TestElement buildTestElement() {
      CloseWebSocketSampler close = new CloseWebSocketSampler();
      if (responseTimeoutMillis != null) {
        close.setReadTimeout(responseTimeoutMillis);
      }
      if (statusCode != null) {
        close.setStatusCode(statusCode);
      }
      return close;
    }

    /**
     * Sets the response timeout for the close frame from server to be received.
     *
     * @param timeoutMillis the response timeout in milliseconds (default value is
     *                      6000 milliseconds)
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslDisconnectSampler responseTimeout(int timeoutMillis) {
      this.responseTimeoutMillis = String.valueOf(timeoutMillis);
      return this;
    }

    /**
     * Same as {@link #responseTimeout(int)} but allowing to use JMeter expressions
     * (variables or functions) to solve the actual parameter values.
     * 
     * @param timeoutMillis a JMeter expression that returns timeout in milliseconds
     *                      (default value is 6000 milliseconds)
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslDisconnectSampler responseTimeout(String timeoutMillis) {
      this.responseTimeoutMillis = timeoutMillis;
      return this;
    }

    /**
     * Sets the status code to indicate the reason for closing the connection.
     * <p>
     * Common status codes:
     * <ul>
     * <li>1000 - Normal closure (default)</li>
     * <li>1001 - Going away</li>
     * <li>1002 - Protocol error</li>
     * <li>1003 - Unsupported data</li>
     * </ul>
     * <p>
     * For a complete list, see
     * <a href="https://datatracker.ietf.org/doc/html/rfc6455#section-7.4">RFC
     * 6455 Section 7.4</a>
     * 
     * @param statusCode the status code for the WebSocket disconnect
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslDisconnectSampler statusCode(StatusCode statusCode) {
      this.statusCode = statusCode.propertyValue();
      return this;
    }

    /**
     * Same as {@link #statusCode(StatusCode)} but allowing to use JMeter
     * expressions
     * (variables or functions) to solve the actual parameter values.
     * 
     * @param statusCode a JMeter expression that returns the status code for the
     *                   WebSocket disconnect
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslDisconnectSampler statusCode(String statusCode) {
      this.statusCode = statusCode;
      return this;
    }

    public static class CodeBuilder extends SingleTestElementCallBuilder<CloseWebSocketSampler> {

      public CodeBuilder(List<Method> builderMethods) {
        super(CloseWebSocketSampler.class, builderMethods);
      }

      @Override
      protected MethodCall buildMethodCall(CloseWebSocketSampler testElement,
          MethodCallContext context) {
        TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
        MethodParam statusCode = paramBuilder.stringParam("statusCode", "1000");
        if (StatusCode.isValidStatusCode(statusCode.getExpression())) {
          statusCode = paramBuilder.enumParam("statusCode", StatusCode.NORMAL_CLOSURE);
        }
        return new MethodCall("websocketDisconnect", DslDisconnectSampler.class)
            .chain("responseTimeout", paramBuilder.intParam("readTimeout", 6000))
            .chain("statusCode", statusCode);
      }
    }
  }

  public static class DslWriteSampler extends BaseSampler<DslWriteSampler> {
    private String requestData;
    private String dataType;

    private DslWriteSampler(String requestData) {
      this(requestData, PayloadDataType.TEXT);
    }

    private DslWriteSampler(String requestData, PayloadDataType dataType) {
      this(requestData, dataType.propertyValue());
    }

    private DslWriteSampler(String requestData, String dataType) {
      super("WebSocket Single Write", SingleWriteWebSocketSamplerGui.class);
      this.requestData = requestData;
      this.dataType = dataType;
      validateDataType(dataType);
    }

    private static void validateDataType(String dataType) {
      if (dataType != null && !containsJmeterExpression(dataType)
          && !PayloadDataType.isValidDataType(dataType)) {
        throw new IllegalArgumentException("Invalid data type. Must be 'text' or 'binary'");
      }
    }

    private static void setWriteDataType(SingleWriteWebSocketSampler write, String dataType) {
      if (containsJmeterExpression(dataType)) {
        write.setProperty("payloadType", dataType);
      } else if (PayloadDataType.isText(dataType)) {
        write.setType(DataPayloadType.Text);
      } else {
        write.setType(DataPayloadType.Binary);
      }
    }

    @Override
    protected TestElement buildTestElement() {
      SingleWriteWebSocketSampler write = new SingleWriteWebSocketSampler();
      setWriteDataType(write, dataType);
      write.setRequestData(requestData);
      write.setCreateNewConnection(false);
      return write;
    }

    public static class CodeBuilder
        extends SingleTestElementCallBuilder<SingleWriteWebSocketSampler> {

      public CodeBuilder(List<Method> builderMethods) {
        super(SingleWriteWebSocketSampler.class, builderMethods);
      }

      @Override
      protected MethodCall buildMethodCall(SingleWriteWebSocketSampler testElement,
          MethodCallContext context) {
        TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
        MethodParam requestData = paramBuilder.stringParam("requestData", "");
        MethodParam dataTypeParam = paramBuilder.enumParam("payloadType", PayloadDataType.TEXT);
        MethodCall ret = new MethodCall("websocketWrite", DslWriteSampler.class,
            new StringParam(requestData.getExpression()));
        if (!dataTypeParam.isDefault()) {
          ret = new MethodCall("websocketWrite", DslWriteSampler.class,
              new StringParam(requestData.getExpression()), dataTypeParam);
        }
        return ret;
      }
    }
  }

  public static class DslReadSampler extends BaseSampler<DslReadSampler> {
    private String responseTimeoutMillis;
    private boolean waitForResponse = true;
    private String dataType;

    private DslReadSampler() {
      this(PayloadDataType.TEXT);
    }

    private DslReadSampler(PayloadDataType dataType) {
      this(dataType.propertyValue());
    }

    private DslReadSampler(String dataType) {
      super("WebSocket Single Read", SingleReadWebSocketSamplerGui.class);
      this.dataType = dataType;
      validateDataType(dataType);
    }

    private static void validateDataType(String dataType) {
      if (dataType != null && !containsJmeterExpression(dataType)
          && !PayloadDataType.isValidDataType(dataType)) {
        throw new IllegalArgumentException("Invalid data type. Must be 'text' or 'binary'");
      }
    }

    private static void setReadDataType(SingleReadWebSocketSampler read, String dataType) {
      if (containsJmeterExpression(dataType)) {
        read.setProperty("dataType", dataType);
      } else if (PayloadDataType.isText(dataType)) {
        read.setDataType(DataType.Text);
      } else {
        read.setDataType(DataType.Binary);
      }
    }

    @Override
    protected TestElement buildTestElement() {
      SingleReadWebSocketSampler read = new SingleReadWebSocketSampler();
      if (responseTimeoutMillis != null) {
        read.setReadTimeout(responseTimeoutMillis);
      }
      setReadDataType(read, dataType);
      read.setOptional(!waitForResponse);
      read.setCreateNewConnection(false);
      return read;
    }

    /**
     * Specifies whether the sampler should block execution until a response is
     * received.
     * <p>
     * When set to {@code true} (default), the sampler waits for a server response
     * or until the timeout expires. When set to {@code false}, the sampler returns
     * immediately if no message is available.
     *
     * @param waitForResponse {@code true} to block until response is received,
     *                        {@code false} to return immediately
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslReadSampler waitForResponse(boolean waitForResponse) {
      this.waitForResponse = waitForResponse;
      return this;
    }

    /**
     * Same as {@link #responseTimeout(int)} but allowing to use JMeter expressions
     * (variables or functions) to solve the actual parameter values.
     * 
     * @param timeoutMillis a JMeter expression that returns timeout in milliseconds
     *                      (default value is 6000 milliseconds)
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslReadSampler responseTimeout(String timeoutMillis) {
      this.responseTimeoutMillis = timeoutMillis;
      return this;
    }

    /**
     * Sets the response timeout for the WebSocket response to be received.
     *
     * @param timeoutMillis the response timeout in milliseconds (default value is
     *                      6000 milliseconds)
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslReadSampler responseTimeout(int timeoutMillis) {
      this.responseTimeoutMillis = String.valueOf(timeoutMillis);
      return this;
    }

    public static class CodeBuilder
        extends SingleTestElementCallBuilder<SingleReadWebSocketSampler> {

      public CodeBuilder(List<Method> builderMethods) {
        super(SingleReadWebSocketSampler.class, builderMethods);
      }

      @Override
      protected MethodCall buildMethodCall(SingleReadWebSocketSampler testElement,
          MethodCallContext context) {
        TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
        boolean optionalParam = !paramBuilder.boolParam("optional", false)
            .getExpression().equals("true");
        MethodParam dataTypeParam = paramBuilder.enumParam("dataType", PayloadDataType.TEXT);
        MethodCall ret = new MethodCall("websocketRead", DslReadSampler.class);
        if (!dataTypeParam.isDefault()) {
          ret = new MethodCall("websocketRead", DslReadSampler.class, dataTypeParam);
        }
        return ret
            .chain("responseTimeout", paramBuilder.intParam("readTimeout", 6000))
            .chain("waitForResponse", new BoolParam(optionalParam, true))
            .chain("createNewConnection", paramBuilder.boolParam("createNewConnection", false));
      }
    }
  }
}
