package us.abstracta.jmeter.javadsl.http;

import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PreProcessor;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleGuiClassCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorScript;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorVars;
import us.abstracta.jmeter.javadsl.core.samplers.BaseSampler;
import us.abstracta.jmeter.javadsl.core.util.JmeterFunction;

/**
 * Abstracts common logic used by HTTP based samplers.
 *
 * @param <T> type of the sampler used to provide proper fluent API methods.
 * @since 0.52
 */
public abstract class DslBaseHttpSampler<T extends DslBaseHttpSampler<T>> extends BaseSampler<T> {

  public static final String RESET_CONNECTIONS_BETWEEN_ITERATIONS_PROP =
      "httpclient.reset_state_on_thread_group_iteration";

  protected String path;
  protected final HttpHeaders headers = new HttpHeaders();
  protected String protocol;
  protected String host;
  protected String port;
  protected String proxyUrl;
  protected String proxyUser;
  protected String proxyPassword;
  protected Duration connectionTimeout;
  protected Duration responseTimeout;

  public DslBaseHttpSampler(String name, String url, Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass);
    if (url == null) {
      return;
    }
    JmeterUrl parsedUrl = JmeterUrl.valueOf(url);
    protocol = parsedUrl.protocol();
    host = parsedUrl.host();
    port = parsedUrl.port();
    path = parsedUrl.path();
  }

  /**
   * Specifies the HTTP Sampler protocol to be used in the HTTP request generated by the sampler.
   * <p>
   * You can specify entire url when creating a sampler, but this method allows you to override the
   * protocol if needed. For example, if you have defaults element with url and just need in one
   * sampler to have a different protocol.
   * <p>
   * In general prefer using java variables and methods, to get shorter and more maintainable code,
   * and use this method sparingly.
   *
   * @param protocol contains protocol value to be used (e.g.: http, https, etc).
   * @return the sampler for further configuration or usage.
   */
  public T protocol(String protocol) {
    this.protocol = protocol;
    return (T) this;
  }

  /**
   * Specifies the server host (domain) to be used in the HTTP request generated by the sampler.
   * <p>
   * You can specify entire url when creating a sampler, but this method allows you to override the
   * host if needed. For example, if you have defaults element with url and just need in one sampler
   * to have a different host.
   * <p>
   * In general prefer using java variables and methods, to get shorter and more maintainable code,
   * and use this method sparingly.
   *
   * @param host contains server name without protocol (no http/https) and path.
   * @return the sampler for further configuration or usage.
   */
  public T host(String host) {
    this.host = host;
    return (T) this;
  }

  /**
   * Specifies the HTTP Sampler port to be used in the HTTP request generated by the sampler.
   * <p>
   * You can specify entire url when creating a sampler, but this method allows you to override the
   * port if needed. For example, if you have defaults element with url and just need in one sampler
   * to have a different port.
   * <p>
   * In general prefer using java variables and methods, to get shorter and more maintainable code,
   * and use this method sparingly.
   *
   * @param port contains port value to be used.
   * @return the sampler for further configuration or usage.
   */
  public T port(int port) {
    this.port = String.valueOf(port);
    return (T) this;
  }

  /**
   * Specifies an HTTP header to be sent by the sampler.
   * <p>
   * To specify multiple headers just invoke this method several times with the different header
   * names and values.
   *
   * @param name  of the HTTP header.
   * @param value of the HTTP header.
   * @return the sampler for further configuration or usage.
   */
  public T header(String name, String value) {
    headers.header(name, value);
    return (T) this;
  }

  /**
   * Same as {@link #header(String, String)} but allows using dynamically calculated HTTP header
   * value.
   * <p>
   * This method is just an abstraction that uses a JMeter variable as HTTP header value and
   * calculates the variable with a jsr223PreProcessor.
   * <p>
   * <b>WARNING:</b> As this method internally uses
   * {@link JmeterDsl#jsr223PreProcessor(PreProcessorScript)}, same limitations and considerations
   * apply. Check its documentation. To avoid such limitations you may use
   * {@link #header(String, String)} with a JMeter variable instead, and dynamically set the
   * variable with {@link JmeterDsl#jsr223PreProcessor(String)}.
   *
   * @param name          of the HTTP header.
   * @param valueSupplier builds the header value.
   * @return the altered sampler to allow for fluent API usage.
   */
  public T header(String name, Function<PreProcessorVars, String> valueSupplier) {
    String variableNamePrefix = "PRE_PROCESSOR_HEADER~";
    return header(name, JmeterFunction.var(variableNamePrefix + name))
        .children(
            jsr223PreProcessor(s -> s.vars.put(variableNamePrefix + name, valueSupplier.apply(s)))
        );
  }

  /**
   * Allows to easily specify the Content-Type HTTP header to be used by the sampler.
   *
   * @param contentType value to send as Content-Type header.
   * @return the sampler for further configuration or usage.
   */
  public T contentType(ContentType contentType) {
    headers.contentType(contentType);
    return (T) this;
  }

  /**
   * Allows to set the maximum amount of time to wait for an HTTP connection to be established.
   * <p>
   * If the connection is not established within the specified timeout, then the request will fail
   * and sample result will be marked as failed with proper response message.
   *
   * @param timeout specifies the duration to be used as connection timeout. When set to 0 it
   *                specifies to not timeout (wait indefinitely), which is not recommended. When set
   *                to a negative number the operating system default is used. By default, is set to
   *                -1.
   * @return the sampler for further configuration or usage.
   * @since 1.4
   */
  public T connectionTimeout(Duration timeout) {
    connectionTimeout = timeout;
    return (T) this;
  }

  /**
   * Allows to set the maximum amount of time to wait for a response to an HTTP request.
   * <p>
   * If the response takes more than specified time, then the request will fail and sample result
   * will be marked as failed with proper response message.
   *
   * @param timeout specifies the duration to be used as response timeout. When set to 0 it
   *                specifies to not timeout (wait indefinitely), which is not recommended. When set
   *                to a negative number the operating system default is used. By default, is set to
   *                -1.
   * @return the sampler for further configuration or usage.
   * @since 1.4
   */
  public T responseTimeout(Duration timeout) {
    responseTimeout = timeout;
    return (T) this;
  }

  /**
   * Allows specifying a proxy through which all http requests will be sent to their final
   * destination.
   * <p>
   * This is usually helpful when you need to use a proxy to access the internet when all access is
   * behind and enterprise proxy (due to security measures) or when you want to intercept requests
   * for further analysis or modification by other tools like fiddler or mitmproxy.
   * <p>
   * If your proxy requires authentication check {@link #proxy(String, String, String)}.
   *
   * @param url specifies the proxy url. For example http://myproxy:8181.
   * @return the sampler for further configuration or usage.
   */
  public T proxy(String url) {
    proxyUrl = url;
    return (T) this;
  }

  /**
   * Same as {@link #proxy(String)} but allowing also to specify proxy credentials.
   *
   * @param url      specifies the proxy url. For example http://myproxy:8181.
   * @param username specifies the username used to authenticate with the proxy.
   * @param password specifies the password used to authenticate with the proxy.
   * @return the sampler for further configuration or usage.
   * @see #proxy(String)
   */
  public T proxy(String url, String username, String password) {
    proxyUrl = url;
    proxyUser = username;
    proxyPassword = password;
    return (T) this;
  }

  @Override
  protected TestElement buildTestElement() {
    if (JMeterUtils.getProperty(RESET_CONNECTIONS_BETWEEN_ITERATIONS_PROP) == null) {
      JMeterUtils.setProperty(RESET_CONNECTIONS_BETWEEN_ITERATIONS_PROP, String.valueOf(false));
    }
    HTTPSamplerProxy ret = new HTTPSamplerProxy();
    HttpElementHelper.modifyTestElementUrl(ret, protocol, host, port, path);
    // We need to use this logic since setPath method triggers additional logic
    if (path != null) {
      ret.setPath(path);
    }
    HttpElementHelper.modifyTestElementTimeouts(ret, connectionTimeout, responseTimeout);
    HttpElementHelper.modifyTestElementProxy(ret, proxyUrl, proxyUser, proxyPassword);
    return configureHttpTestElement(ret);
  }

  protected abstract HTTPSamplerProxy configureHttpTestElement(HTTPSamplerProxy elem);

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    HashTree ret = super.buildTreeUnder(parent, context);
    if (!headers.isEmpty()) {
      context.buildChild(headers, ret);
    }
    new DslCookieManager().registerDependency(context);
    new DslCacheManager().registerDependency(context);
    return ret;
  }

  protected abstract static class BaseHttpSamplerCodeBuilder extends SingleGuiClassCallBuilder {

    private final String defaultName;

    protected BaseHttpSamplerCodeBuilder(String defaultName,
        Class<? extends JMeterGUIComponent> guiClass, List<Method> builderMethods) {
      super(guiClass, builderMethods);
      this.defaultName = defaultName;
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      HTTPSamplerProxy testElement = (HTTPSamplerProxy) context.getTestElement();
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodParam name = paramBuilder.nameParam(defaultName);
      MethodParam protocol = paramBuilder.stringParam(HTTPSamplerBase.PROTOCOL);
      MethodParam domain = paramBuilder.stringParam(HTTPSamplerBase.DOMAIN);
      MethodParam port = paramBuilder.intParam(HTTPSamplerBase.PORT);
      MethodParam path = paramBuilder.stringParam(HTTPSamplerBase.PATH, "/");
      MethodParam url = buildUrlParam(protocol, domain,
          new StringParam(port.isDefault() ? "" : "" + port.getExpression()), path);
      MethodCall ret = buildBaseHttpMethodCall(name, url, paramBuilder);
      context.findBuilder(DslCacheManager.CodeBuilder.class)
          .registerDependency(context, ret);
      context.findBuilder(DslCookieManager.CodeBuilder.class)
          .registerDependency(context, ret);
      if (url.equals(path)) {
        ret.chain("protocol", protocol)
            .chain("host", domain)
            .chain("port", port);
      }
      chainRequestCalls(ret, testElement, context);
      chainAdditionalOptions(ret, paramBuilder);
      HttpElementHelper.chainConnectionOptionsToMethodCall(ret, paramBuilder);
      return ret;
    }

    protected abstract MethodCall buildBaseHttpMethodCall(MethodParam name, MethodParam url,
        TestElementParamBuilder paramBuilder);

    public static MethodParam buildUrlParam(MethodParam protocol, MethodParam domain,
        MethodParam port, MethodParam path) {
      if (!domain.isDefault()) {
        return new StringParam(
            new JmeterUrl(protocol.getExpression(), domain.getExpression(), port.getExpression(),
                path.isDefault() ? "" : path.getExpression()).toString());
      } else {
        return path;
      }
    }

    protected abstract void chainRequestCalls(MethodCall ret, HTTPSamplerProxy testElement,
        MethodCallContext context);

    protected String removeContentTypeHeader(MethodCallContext context) {
      if (context == null) {
        return null;
      }
      String headerName = HTTPConstants.HEADER_CONTENT_TYPE;
      HeaderManager headers = (HeaderManager) context.getTestElement();
      Header header = headers.getFirstHeaderNamed(headerName);
      headers.removeHeaderNamed(headerName);
      return header == null ? null : header.getValue();
    }

    protected void chainContentType(MethodCall ret, String contentType) {
      ret.chain("contentType", new ContentTypeParam(contentType));
    }

    protected void chainHeaders(MethodCall ret, MethodCallContext headers) {
      if (headers != null) {
        ret.reChain(headers.buildMethodCall());
      }
    }

    protected abstract void chainAdditionalOptions(MethodCall ret,
        TestElementParamBuilder paramBuilder);

  }

}
