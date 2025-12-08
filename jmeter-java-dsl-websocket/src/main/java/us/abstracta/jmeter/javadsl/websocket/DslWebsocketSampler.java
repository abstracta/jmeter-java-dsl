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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.samplers.BaseSampler;

public class DslWebsocketSampler extends BaseSampler<DslWebsocketSampler> {
  private static final String DEFAULT_NAME = "Websocket Sampler";

  private DslWebsocketSampler(String name) {
    super(name == null ? DEFAULT_NAME : name, OpenWebSocketSamplerGui.class);
  }

  /**
   * Provides factory methods to create WebSocket samplers for performance
   * testing.
   * <p>
   * This class serves as the entry point for creating different types of
   * WebSocket operations:
   * <ul>
   * <li>{@link #connect(String)} - Establish a WebSocket connection</li>
   * <li>{@link #write(String)} - Send messages to the server</li>
   * <li>{@link #read()} - Read responses from the server</li>
   * <li>{@link #disconnect()} - Close the WebSocket connection</li>
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
  public static DslWebsocketSampler webSocketSampler() {
    return new DslWebsocketSampler(DEFAULT_NAME);
  }

  /**
   * Creates a WebSocket connect sampler to establish a connection to the server.
   * <p>
   * After establishing the connection, use {@link #write(String)} and
   * {@link #read()}
   * samplers to send and receive messages. Remember to close the connection using
   * {@link #disconnect()} when finished.
   *
   * @return the connect sampler for further configuration or usage
   * @since 2.2
   * @see #link {@link DslConnectSampler#server(String)}
   * @see #link {@link DslConnectSampler#port(int)}
   * @see #link {@link DslConnectSampler#path(String)}
   * @see #link {@link DslConnectSampler#tls(boolean)}
   */
  public static DslConnectSampler connect() {
    return new DslConnectSampler();
  }

  /**
   * Creates a WebSocket connect sampler to establish a connection to the server.
   * <p>
   * After establishing the connection, use {@link #write(String)} and
   * {@link #read()}
   * samplers to send and receive messages. Remember to close the connection using
   * {@link #disconnect()} when finished.
   * <p>
   * It could be also used alone to test the connection to the server.
   * <p>
   * <b>URL Format:</b> {@code ws://host:port/path?query} or
   * {@code wss://host:port/path?query}
   * <p>
   *
   * @param url the WebSocket server URL. Supported schemes: {@code ws://} (plain)
   *            and {@code wss://} (TLS)
   * @return the connect sampler for further configuration or usage
   * @since 2.2
   */
  public static DslConnectSampler connect(String url) {
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
  public static DslDisconnectSampler disconnect() {
    return new DslDisconnectSampler();
  }

  /**
   * Creates a WebSocket write sampler to send a text message to the server.
   * <p>
   * Requires an active WebSocket connection established via
   * {@link #connect(String)}.
   * 
   * @return the write sampler for further configuration or usage
   * @since 2.2
   * @see #link {@link DslWriteSampler#requestData(String)}
   */
  public static DslWriteSampler write() {
    return new DslWriteSampler();
  }

  /**
   * Creates a WebSocket write sampler to send a text message to the server.
   * <p>
   * Requires an active WebSocket connection established via
   * {@link #connect(String)}.
   * 
   * @return the write sampler for further configuration or usage
   * @param requestData the message to send to the WebSocket server
   * @since 2.2
   */
  public static DslWriteSampler write(String requestData) {
    return new DslWriteSampler(requestData);
  }

  /**
   * Creates a WebSocket read sampler to receive a message from the server.
   * <p>
   * By default, this sampler blocks execution until a response is received or
   * the timeout is reached. This behavior can be changed using
   * {@link DslReadSampler#waitForResponse(boolean)}.
   * <p>
   * Requires an active WebSocket connection established via
   * {@link #connect(String)}.
   * 
   * @return the read sampler for further configuration or usage
   * @since 2.2
   */
  public static DslReadSampler read() {
    return new DslReadSampler();
  }

  @Override
  protected TestElement buildTestElement() {
    throw new UnsupportedOperationException(
        "Use specific sampler types: connect(), disconnect(), write(), or read()");
  }

  public static class DslConnectSampler extends BaseSampler<DslConnectSampler> {
    private String connectionTimeout;
    private String responseTimeout;
    private String server;
    private String port;
    private String path;
    private boolean tls = false;

    private DslConnectSampler() {
      super("WebSocket Open Connection", OpenWebSocketSamplerGui.class);
    }

    private DslConnectSampler(String url) {
      super("WebSocket Open Connection", OpenWebSocketSamplerGui.class);
      try {
        URI uri = new URI(url);

        String scheme = uri.getScheme();
        if (scheme == null || (!"ws".equals(scheme) && !"wss".equals(scheme))) {
          throw new IllegalArgumentException(
              "Invalid WebSocket URL. Must start with 'ws://' or 'wss://'");
        }

        this.tls("wss".equals(scheme));
        this.server(uri.getHost());
        if (this.server == null) {
          throw new IllegalArgumentException("Invalid WebSocket URL. Host is required");
        }

        int port = uri.getPort();
        if (port == -1) {
          this.port(this.tls ? "443" : "80");
        } else {
          this.port(String.valueOf(port));
        }

        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
          this.path("/");
        } else {
          this.path(path);
        }

        String query = uri.getQuery();
        if (query != null && !query.isEmpty()) {
          this.path(this.path + "?" + query);
        }

      } catch (URISyntaxException e) {
        throw new IllegalArgumentException("Invalid WebSocket URL: " + url, e);
      }
    }

    @Override
    protected TestElement buildTestElement() {
      OpenWebSocketSampler ret = new OpenWebSocketSampler();
      if (connectionTimeout != null) {
        ret.setConnectTimeout(connectionTimeout);
      }
      if (responseTimeout != null) {
        ret.setReadTimeout(responseTimeout);
      }
      ret.setTLS(tls);
      ret.setServer(server);
      ret.setPort(port);
      ret.setPath(path);
      return ret;
    }

    /**
     * Same as {@link #connectionTimeout(int)} but allowing to use JMeter
     * expressions
     * (variables or
     * functions) to solve the actual parameter values.
     * 
     * @param timeout a JMeter expression that returns timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler connectionTimeout(String timeout) {
      this.connectionTimeout = timeout;
      return this;
    }

    /**
     * Sets the connection timeout for the WebSocket connection creation.
     *
     * @param timeout the connection timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler connectionTimeout(int timeout) {
      this.connectionTimeout = String.valueOf(timeout);
      return this;
    }

    /**
     * Same as {@link #responseTimeout(int)} but allowing to use JMeter
     * expressions
     * (variables or
     * functions) to solve the actual parameter values.
     * 
     * @param timeout a JMeter expression that returns timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler responseTimeout(String timeout) {
      this.responseTimeout = timeout;
      return this;
    }

    /**
     * Sets the response timeout for the WebSocket negotiation.
     *
     * @param timeout the response timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler responseTimeout(int timeout) {
      this.responseTimeout = String.valueOf(timeout);
      return this;
    }

    /**
     * Specifies the WebSocket server to connect to.
     *
     * @param server the WebSocket server to connect to
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler server(String server) {
      this.server = server;
      return this;
    }

    /**
     * Specifies the WebSocket port to connect to.
     *
     * @param port the WebSocket port to connect to
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler port(String port) {
      this.port = port;
      return this;
    }

    /**
     * Specifies the WebSocket port to connect to.
     *
     * @param port the WebSocket port to connect to
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler port(int port) {
      this.port = String.valueOf(port);
      return this;
    }

    /**
     * Specifies the WebSocket path to connect to. In case of need query parameters,
     * they should be included in the path.
     * <p>
     * Example:
     * 
     * <pre>{@code
     * path("/websocket?room=general")
     * }</pre>
     *
     * @param path the WebSocket path to connect to
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler path(String path) {
      this.path = path;
      return this;
    }

    /**
     * Specifies if the WebSocket connection should be established using TLS.
     *
     * @param tls the WebSocket TLS flag
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler tls(boolean tls) {
      this.tls = tls;
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
        MethodParam server = paramBuilder.stringParam("server");
        MethodParam port = paramBuilder.stringParam("port");
        MethodParam path = paramBuilder.stringParam("path");
        MethodParam tls = paramBuilder.boolParam("TLS", false);
        MethodCall ret = null;
        if (!server.isDefault() && !port.isDefault() && !path.isDefault()) {
          String protocol = tls.isDefault() ? "ws"
              : (tls.getExpression().equals("true") ? "wss" : "ws");
          String url = protocol + "://" + server.getExpression() + ":"
              + port.getExpression() + path.getExpression();
          ret = new MethodCall("webSocketSampler().connect",
              DslConnectSampler.class, new StringParam(url));
        } else {
          ret = buildMethodCall()
              .chain("webSocketSampler().connect()")
              .chain("server", server)
              .chain("port", port)
              .chain("path", path)
              .chain("tls", tls);
        }
        return ret
            .chain("connectionTimeout", paramBuilder.stringParam("connectTimeout", ""))
            .chain("responseTimeout", paramBuilder.stringParam("readTimeout", ""));
      }
    }
  }

  public static class DslDisconnectSampler extends BaseSampler<DslDisconnectSampler> {
    private String responseTimeout;
    private String statusCode;

    private DslDisconnectSampler() {
      super("WebSocket Close", CloseWebSocketSamplerGui.class);
    }

    @Override
    protected TestElement buildTestElement() {
      CloseWebSocketSampler close = new CloseWebSocketSampler();
      if (responseTimeout != null) {
        close.setReadTimeout(responseTimeout);
      }
      if (statusCode != null) {
        close.setStatusCode(statusCode);
      }
      return close;
    }

    /**
     * Same as {@link #responseTimeout(int)} but allowing to use JMeter
     * expressions
     * (variables or
     * functions) to solve the actual parameter values.
     * 
     * @param timeout a JMeter expression that returns timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslDisconnectSampler responseTimeout(String timeout) {
      this.responseTimeout = timeout;
      return this;
    }

    /**
     * Sets the response timeout for the close frame from server to be received.
     *
     * @param timeout the response timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslDisconnectSampler responseTimeout(int timeout) {
      this.responseTimeout = String.valueOf(timeout);
      return this;
    }

    /**
     * Same as {@link #statusCode(int)} but allowing to use JMeter
     * expressions
     * (variables or
     * functions) to solve the actual parameter values.
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
    public DslDisconnectSampler statusCode(int statusCode) {
      this.statusCode = String.valueOf(statusCode);
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
        return new MethodCall("webSocketSampler().disconnect", DslDisconnectSampler.class)
            .chain("responseTimeout", paramBuilder.stringParam("readTimeout"))
            .chain("statusCode", paramBuilder.stringParam("statusCode"));
      }
    }
  }

  public static class DslWriteSampler extends BaseSampler<DslWriteSampler> {
    private String requestData;

    private DslWriteSampler() {
      super("WebSocket Single Write", SingleWriteWebSocketSamplerGui.class);
    }

    private DslWriteSampler(String requestData) {
      super("WebSocket Single Write", SingleWriteWebSocketSamplerGui.class);
      this.requestData = requestData;
    }

    @Override
    protected TestElement buildTestElement() {
      SingleWriteWebSocketSampler write = new SingleWriteWebSocketSampler();
      write.setType(DataPayloadType.Text);
      write.setRequestData(requestData);
      write.setCreateNewConnection(false);
      return write;
    }

    /**
     * Sets the request data for the WebSocket write.
     *
     * @param requestData the request data for the WebSocket write
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslWriteSampler requestData(String requestData) {
      this.requestData = requestData;
      return this;
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
        return new MethodCall("webSocketSampler().write", DslWriteSampler.class)
            .chain("requestData", paramBuilder.stringParam("requestData"))
            .chain("createNewConnection", paramBuilder.boolParam("createNewConnection", false));
      }
    }
  }

  public static class DslReadSampler extends BaseSampler<DslReadSampler> {
    private String responseTimeout;
    private boolean waitForResponse = true;

    private DslReadSampler() {
      super("WebSocket Single Read", SingleReadWebSocketSamplerGui.class);
    }

    @Override
    protected TestElement buildTestElement() {
      SingleReadWebSocketSampler read = new SingleReadWebSocketSampler();
      if (responseTimeout != null) {
        read.setReadTimeout(responseTimeout);
      }
      read.setDataType(DataType.Text);
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
     * Same as {@link #responseTimeout(int)} but allowing to use JMeter
     * expressions
     * (variables or
     * functions) to solve the actual parameter values.
     * 
     * @param timeout a JMeter expression that returns timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslReadSampler responseTimeout(String timeout) {
      this.responseTimeout = timeout;
      return this;
    }

    /**
     * Sets the response timeout for the WebSocket response to be received.
     *
     * @param timeout the response timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslReadSampler responseTimeout(int timeout) {
      this.responseTimeout = String.valueOf(timeout);
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
        return new MethodCall("webSocketSampler().read", DslReadSampler.class)
            .chain("responseTimeout", paramBuilder.stringParam("readTimeout", ""))
            .chain("createNewConnection", paramBuilder.boolParam("createNewConnection", false));
      }
    }
  }
}
