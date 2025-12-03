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

/**
 * Allows to create a WebSocket sampler to establish a connection, send a
 * message, read a response and disconnect.
 *
 * @since 2.2
 */
public class DslWebsocketSampler extends BaseSampler<DslWebsocketSampler> {
  private static final String DEFAULT_NAME = "Websocket Sampler";

  /**
   * Creates a WebSocket sampler.
   *
   * @param name the name of the sampler
   * @return the sampler for further configuration or usage
   * @since 2.2
   */
  private DslWebsocketSampler(String name) {
    super(name == null ? DEFAULT_NAME : name, OpenWebSocketSamplerGui.class);
  }

  /**
   * Creates a WebSocket sampler.
   *
   * @return the sampler for further configuration or usage
   * @since 2.2
   */
  public static DslWebsocketSampler webSocketSampler() {
    return new DslWebsocketSampler(DEFAULT_NAME);
  }

  /**
   * Creates a WebSocket connect sampler.
   *
   * @return the connect sampler for further configuration or usage
   * @since 2.2
   */
  public static DslConnectSampler connect() {
    return new DslConnectSampler();
  }

  /**
   * Creates a WebSocket connect sampler with URL parsing.
   *
   * @param url the WebSocket server URL
   * @return the connect sampler for further configuration or usage
   * @since 2.2
   */
  public static DslConnectSampler connect(String url) {
    DslConnectSampler sampler = new DslConnectSampler();
    sampler.parseUrl(url);
    return sampler;
  }

  /**
   * Creates a WebSocket disconnect sampler.
   *
   * @return the disconnect sampler for further configuration or usage
   * @since 2.2
   */
  public static DslDisconnectSampler disconnect() {
    return new DslDisconnectSampler();
  }

  /**
   * Creates a WebSocket write sampler.
   *
   * @return the write sampler for further configuration or usage
   * @since 2.2
   */
  public static DslWriteSampler write() {
    return new DslWriteSampler();
  }

  /**
   * Creates a WebSocket read sampler.
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

  /**
   * Inner class for WebSocket connect operations.
   */
  public static class DslConnectSampler extends BaseSampler<DslConnectSampler> {
    private String connectionTimeout;
    private String responseTimeout;
    private String server;
    private String port;
    private String path;
    private boolean tls;

    private DslConnectSampler() {
      super("WebSocket Open Connection", OpenWebSocketSamplerGui.class);
    }

    /**
     * Parses a WebSocket URL and sets the corresponding fields.
     *
     * @param url the WebSocket URL to parse
     * @throws IllegalArgumentException if the URL is invalid
     */
    private void parseUrl(String url) {
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
          this.path(this.getPath() + "?" + query);
        }

      } catch (URISyntaxException e) {
        throw new IllegalArgumentException("Invalid WebSocket URL: " + url, e);
      }
    }

    @Override
    protected TestElement buildTestElement() {
      OpenWebSocketSampler ret = new OpenWebSocketSampler();
      ret.setConnectTimeout(connectionTimeout);
      ret.setReadTimeout(responseTimeout);
      ret.setTLS(tls);
      ret.setServer(server);
      ret.setPort(port);
      ret.setPath(path);
      return ret;
    }

    /**
     * Sets the connection timeout for the WebSocket connection.
     *
     * @param timeout the connection timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler connectionTimeout(String timeout) {
      this.connectionTimeout = timeout;
      return this;
    }

    /**
     * Sets the response timeout for the WebSocket operations.
     *
     * @param timeout the response timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler responseTimeout(String timeout) {
      this.responseTimeout = timeout;
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
     * Sets the WebSocket port to connect to.
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
     * Sets the WebSocket path to connect to.
     *
     * @param path the WebSocket path to connect to
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslConnectSampler path(String path) {
      this.path = path;
      return this;
    }

    private String getPath() {
      return path;
    }

    /**
     * Sets the WebSocket TLS flag.
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

        // Try to build URL if all required parameters are available
        if (!server.isDefault() && !port.isDefault() && !path.isDefault()) {
          String protocol = tls.isDefault() ? "ws"
              : (tls.getExpression().equals("true") ? "wss" : "ws");
          String url = protocol + "://" + server.getExpression() + ":"
              + port.getExpression() + path.getExpression();
          MethodCall ret = new MethodCall("webSocketSampler().connect",
              DslConnectSampler.class, new StringParam(url));

          // Add non-default timeout parameters
          MethodParam connectionTimeout = paramBuilder.stringParam("connectTimeout");
          if (!connectionTimeout.isDefault()) {
            ret.chain("connectionTimeout", connectionTimeout);
          }
          MethodParam responseTimeout = paramBuilder.stringParam("readTimeout");
          if (!responseTimeout.isDefault()) {
            ret.chain("responseTimeout", responseTimeout);
          }

          return ret;
        } else {
          // Fall back to individual method calls
          return buildMethodCall()
              .chain("webSocketSampler().connect()")
              .chain("server", server)
              .chain("port", port)
              .chain("path", path)
              .chain("tls", tls)
              .chain("connectionTimeout", paramBuilder.stringParam("connectTimeout"))
              .chain("responseTimeout", paramBuilder.stringParam("readTimeout"));
        }
      }
    }
  }

  /**
   * Inner class for WebSocket disconnect operations.
   */
  public static class DslDisconnectSampler extends BaseSampler<DslDisconnectSampler> {
    private String responseTimeout;
    private String statusCode;

    private DslDisconnectSampler() {
      super("WebSocket Close", CloseWebSocketSamplerGui.class);
    }

    @Override
    protected TestElement buildTestElement() {
      CloseWebSocketSampler close = new CloseWebSocketSampler();
      close.setReadTimeout(responseTimeout);
      close.setStatusCode(statusCode);
      return close;
    }

    /**
     * Sets the response timeout for the WebSocket operations.
     *
     * @param timeout the response timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslDisconnectSampler responseTimeout(String timeout) {
      this.responseTimeout = timeout;
      return this;
    }

    /**
     * Sets the status code for the WebSocket disconnect.
     *
     * @param statusCode the status code for the WebSocket disconnect
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
        return new MethodCall("webSocketSampler().disconnect", DslDisconnectSampler.class)
            .chain("responseTimeout", paramBuilder.stringParam("readTimeout"))
            .chain("statusCode", paramBuilder.stringParam("statusCode"));
      }
    }
  }

  /**
   * Inner class for WebSocket write operations.
   */
  public static class DslWriteSampler extends BaseSampler<DslWriteSampler> {
    private String connectionTimeout;
    private String requestData;
    private boolean createNewConnection = false;

    private DslWriteSampler() {
      super("WebSocket Single Write", SingleWriteWebSocketSamplerGui.class);
    }

    @Override
    protected TestElement buildTestElement() {
      SingleWriteWebSocketSampler write = new SingleWriteWebSocketSampler();
      write.setConnectTimeout(connectionTimeout != null ? connectionTimeout : "20000");
      write.setType(DataPayloadType.Text);
      write.setRequestData(requestData);
      write.setCreateNewConnection(createNewConnection);
      return write;
    }

    /**
     * Sets the connection timeout for the WebSocket connection.
     *
     * @param timeout the connection timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslWriteSampler connectionTimeout(String timeout) {
      this.connectionTimeout = timeout;
      return this;
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

    /**
     * Sets the create new connection flag for the WebSocket write.
     *
     * @param createNewConnection the create new connection flag for the WebSocket write
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslWriteSampler createNewConnection(boolean createNewConnection) {
      this.createNewConnection = createNewConnection;
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

  /**
   * Inner class for WebSocket read operations.
   */
  public static class DslReadSampler extends BaseSampler<DslReadSampler> {
    private String connectionTimeout;
    private String responseTimeout;
    private boolean createNewConnection = false;

    private DslReadSampler() {
      super("WebSocket Single Read", SingleReadWebSocketSamplerGui.class);
    }

    @Override
    protected TestElement buildTestElement() {
      SingleReadWebSocketSampler read = new SingleReadWebSocketSampler();
      read.setConnectTimeout(connectionTimeout != null ? connectionTimeout : "20000");
      read.setReadTimeout(responseTimeout != null ? responseTimeout : "7000");
      read.setDataType(DataType.Text);
      read.setOptional(false);
      read.setCreateNewConnection(createNewConnection);
      return read;
    }

    /**
     * Sets the connection timeout for the WebSocket connection.
     *
     * @param timeout the connection timeout in milliseconds
     * @return the sampler for further configuration or usage
     * @since 2.2
     */
    public DslReadSampler connectionTimeout(String timeout) {
      this.connectionTimeout = timeout;
      return this;
    }

    /**
     * Sets the response timeout for the WebSocket operations.
     *
     * @param timeout the response timeout in milliseconds
     * @return the sampler for further configuration or usage
     */
    public DslReadSampler responseTimeout(String timeout) {
      this.responseTimeout = timeout;
      return this;
    }

    public DslReadSampler createNewConnection(boolean createNewConnection) {
      this.createNewConnection = createNewConnection;
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
            .chain("responseTimeout", paramBuilder.stringParam("readTimeout"))
            .chain("createNewConnection", paramBuilder.boolParam("createNewConnection", false));
      }
    }
  }
}
