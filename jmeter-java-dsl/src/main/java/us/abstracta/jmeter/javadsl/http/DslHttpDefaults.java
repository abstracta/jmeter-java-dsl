package us.abstracta.jmeter.javadsl.http;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleGuiClassCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.core.configs.BaseConfigElement;
import us.abstracta.jmeter.javadsl.http.DslBaseHttpSampler.BaseHttpSamplerCodeBuilder;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler.HttpClientImpl;

/**
 * Allows configuring default values for common properties of HTTP samplers.
 * <p>
 * This is mainly a way to avoid duplication and an alternative to using java variables or builder
 * method. When in doubt, prefer using java variables or custom defined builder methods since they
 * are easier to write (in some cases), read and identify their scope.
 *
 * @see DslHttpSampler
 * @since 0.39
 */
public class DslHttpDefaults extends BaseConfigElement {

  private String protocol;
  private String host;
  private String port;
  private String path;
  private Charset encoding;
  private String proxyUrl;
  private String proxyUser;
  private String proxyPassword;
  private boolean downloadEmbeddedResources;
  private HttpClientImpl clientImpl;

  public DslHttpDefaults() {
    super("HTTP Request Defaults", HttpDefaultsGui.class);
  }

  /**
   * Specifies the default URL for HTTP samplers.
   * <p>
   * The DSL will parse the URL and properly set each of HTTP Request Defaults properties (protocol,
   * host, port and path).
   * <p>
   * You can later on overwrite in a sampler the path (by specifying only the path as url), or the
   * entire url (by specifying the full url as url).
   *
   * @param url specifies the default URL to be used by HTTP samplers. It might contain the path or
   *            not.
   * @return the HTTP Defaults instance for further configuration or usage.
   */
  public DslHttpDefaults url(String url) {
    /*
    url is decomposed and not just set on path, to allow in samplers to override just path and
    reuse other default settings
    */
    JmeterUrl parsedUrl = JmeterUrl.valueOf(url);
    protocol = parsedUrl.protocol();
    host = parsedUrl.host();
    port = parsedUrl.port();
    path = parsedUrl.path();
    return this;
  }

  /**
   * Specifies the default protocol (eg: HTTP, HTTPS) to be used in the HTTP samplers.
   * <p>
   * You can specify entire url through {@link #url(String)}, but this method allows you to only
   * specify protocol when you need to override some other default, or just want that all samplers
   * use same default protocol.
   * <p>
   * In general prefer using java variables and methods, to get shorter and more maintainable code,
   * and use this method sparingly.
   *
   * @param protocol contains protocol value to be used (e.g.: http, https, etc).
   * @return the HTTP Defaults instance for further configuration or usage.
   * @since 0.49
   */
  public DslHttpDefaults protocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  /**
   * Specifies the default server host (domain) to be used in the HTTP samplers.
   * <p>
   * You can specify entire url through {@link #url(String)}, but this method allows you to only
   * specify host (and not protocol) when you need to override some other default, or just want that
   * all samplers use same default host.
   * <p>
   * In general prefer using java variables and methods, to get shorter and more maintainable code,
   * and use this method sparingly.
   *
   * @param host contains server name without protocol (no http/https) and path.
   * @return the HTTP Defaults instance for further configuration or usage.
   * @since 0.49
   */
  public DslHttpDefaults host(String host) {
    this.host = host;
    return this;
  }

  /**
   * Specifies the default port to be used in the HTTP samplers.
   * <p>
   * You can specify entire url through {@link #url(String)}, but this method allows you to only
   * specify port (and not protocol or host) when you need to override some other default, or just
   * want that all samplers use same default port.
   * <p>
   * In general prefer using java variables and methods, to get shorter and more maintainable code,
   * and use this method sparingly.
   *
   * @param port contains port value to be used.
   * @return the HTTP Defaults instance for further configuration or usage.
   * @since 0.49
   */
  public DslHttpDefaults port(int port) {
    this.port = String.valueOf(port);
    return this;
  }

  /**
   * Specifies the default URL path to be used in the HTTP samplers.
   * <p>
   * You can specify entire url through {@link #url(String)}, but this method allows you to only
   * specify path (and not protocol, host or port) when you need to override some other default, or
   * just want that all samplers use same path.
   * <p>
   * In general prefer using java variables and methods, to get shorter and more maintainable code,
   * and use this method sparingly.
   *
   * @param path contains URL path to be used by samplers.
   * @return the HTTP Defaults instance for further configuration or usage.
   * @since 0.49
   */
  public DslHttpDefaults path(String path) {
    this.path = path;
    return this;
  }

  /**
   * Specifies the default charset to be used for encoding URLs and requests contents.
   * <p>
   * This can be overwritten by {@link DslHttpSampler#encoding(Charset)}.
   *
   * @param encoding specifies the charset to be used by default.
   * @return the HTTP Defaults instance for further configuration or usage.
   * @see DslHttpSampler#encoding(Charset)
   */
  public DslHttpDefaults encoding(Charset encoding) {
    this.encoding = encoding;
    return this;
  }

  /**
   * Allows enabling automatic download of HTML embedded resources (images, iframes, etc) by
   * default.
   *
   * @return the HTTP Defaults instance for further configuration or usage.
   * @see DslHttpSampler#downloadEmbeddedResources()
   */
  public DslHttpDefaults downloadEmbeddedResources() {
    this.downloadEmbeddedResources = true;
    return this;
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
   * @return the config element for further configuration or usage.
   */
  public DslHttpDefaults proxy(String url) {
    this.proxyUrl = url;
    return this;
  }

  /**
   * Same as {@link #proxy(String)} but allowing also to specify proxy credentials.
   *
   * @param url      specifies the proxy url. For example http://myproxy:8181.
   * @param username specifies the username used to authenticate with the proxy.
   * @param password specifies the password used to authenticate with the proxy.
   * @return the config element for further configuration or usage.
   * @see #proxy(String)
   */
  public DslHttpDefaults proxy(String url, String username, String password) {
    this.proxyUrl = url;
    this.proxyUser = username;
    this.proxyPassword = password;
    return this;
  }

  /**
   * Allows specifying which http client implementation to use by default for HTTP samplers.
   * <p>
   * This can be overwritten by {@link DslHttpSampler#clientImpl(HttpClientImpl)}.
   *
   * @param clientImpl the HTTP client implementation to use. If none is specified, then
   *                   {@link DslHttpSampler.HttpClientImpl#HTTP_CLIENT} is used.
   * @return the HTTP Defaults instance for further configuration or usage.
   * @see DslHttpSampler.HttpClientImpl
   * @see DslHttpSampler#clientImpl(HttpClientImpl)
   */
  public DslHttpDefaults clientImpl(HttpClientImpl clientImpl) {
    this.clientImpl = clientImpl;
    return this;
  }

  /**
   * Specifies whether to reset (drop and recreate) connections on each thread group iteration or
   * not.
   * <p>
   * By default, connections will be reused to avoid common issues of port and file descriptors
   * exhaustion requiring OS tuning, even though this means that generated load is not realistic
   * enough for emulating as if each iteration were a different user. If you need to proper
   * generation of connections and disconnections between iterations, then consider using this
   * method.
   * <p>
   * When using reset connection for each thread consider tuning OS like explained in "Configure
   * your environment" section of
   * <a href="https://medium.com/@chientranthien/how-to-generate-high-load-benchmark-with-jmeter-80e828a67592">this article</a>.
   * <p>
   * <b>Warning:</b> This setting is applied at JVM level, which means that it will affect the
   * entire test plan and potentially other test plans running in the same JVM instance.
   *
   * @param enable specifies to reset connections on each thread group iteration when true,
   *               otherwise reuse connections. By default, connections are reused.
   * @return the HTTP Defaults instance for further configuration or usage.
   * @since 0.65
   */
  public DslHttpDefaults resetConnectionsBetweenIterations(boolean enable) {
    System.setProperty(DslBaseHttpSampler.RESET_CONNECTIONS_BETWEEN_ITERATIONS_PROP,
        String.valueOf(enable));
    return this;
  }

  /**
   * Allows specifying the connections ttl (time-to-live) used to determine how much time a
   * connection can be kept open.
   * <p>
   * This setting allows tuning connections handling avoiding unnecessary resources usage depending
   * on the use case and server under test settings.
   *
   * @param ttl specifies the duration for connections to keep open before they are closed. By
   *            default, this is set to 1 minute.
   * @return the HTTP Defaults instance for further configuration or usage.
   * @since 0.65
   */
  public DslHttpDefaults connectionsTtl(Duration ttl) {
    System.setProperty("httpclient4.time_to_live", String.valueOf(ttl.toMillis()));
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    ConfigTestElement ret = new ConfigTestElement();
    if (protocol != null) {
      ret.setProperty(HTTPSamplerBase.PROTOCOL, protocol);
    }
    if (host != null) {
      ret.setProperty(HTTPSamplerBase.DOMAIN, host);
    }
    if (port != null) {
      ret.setProperty(HTTPSamplerBase.PORT, port);
    }
    if (path != null) {
      ret.setProperty(HTTPSamplerBase.PATH, path);
    }
    if (encoding != null) {
      ret.setProperty(HTTPSamplerBase.CONTENT_ENCODING, encoding.toString());
    }
    ret.setProperty(new TestElementProperty(HTTPSamplerBase.ARGUMENTS, new Arguments()));
    if (proxyUrl != null) {
      JmeterUrl parsedUrl = JmeterUrl.valueOf(proxyUrl);
      ret.setProperty(HTTPSamplerBase.PROXYSCHEME, parsedUrl.protocol());
      ret.setProperty(HTTPSamplerBase.PROXYHOST, parsedUrl.host());
      ret.setProperty(HTTPSamplerBase.PROXYPORT, parsedUrl.port());
      if (proxyUser != null) {
        ret.setProperty(HTTPSamplerBase.PROXYUSER, proxyUser);
      }
      if (proxyPassword != null) {
        ret.setProperty(HTTPSamplerBase.PROXYPASS, proxyPassword);
      }
    }
    if (downloadEmbeddedResources) {
      ret.setProperty(HTTPSamplerBase.IMAGE_PARSER, true);
      ret.setProperty(HTTPSamplerBase.CONCURRENT_DWN, true);
    }
    if (clientImpl != null) {
      ret.setProperty(HTTPSamplerBase.IMPLEMENTATION, clientImpl.propertyValue);
    }
    return ret;
  }

  public static class CodeBuilder extends SingleGuiClassCallBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(HttpDefaultsGui.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      MethodCall ret = buildMethodCall();
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(context.getTestElement());
      MethodParam protocol = paramBuilder.stringParam(HTTPSamplerBase.PROTOCOL);
      MethodParam host = paramBuilder.stringParam(HTTPSamplerBase.DOMAIN);
      MethodParam port = paramBuilder.stringParam(HTTPSamplerBase.PORT);
      MethodParam path = paramBuilder.stringParam(HTTPSamplerBase.PATH, "/");

      if (!protocol.isDefault() && !host.isDefault()) {
        ret.chain("url", BaseHttpSamplerCodeBuilder.buildUrlParam(protocol,
            host, port, path));
      } else {
        ret.chain("protocol", protocol)
            .chain("host", host)
            .chain("port", port)
            .chain("path", path);
      }

      ret.chain("encoding", paramBuilder.encodingParam(HTTPSamplerBase.CONTENT_ENCODING, null))
          .chain("downloadEmbeddedResources",
              paramBuilder.boolParam(HTTPSamplerBase.IMAGE_PARSER, false))
          .chain("clientImpl",
              paramBuilder.enumParam(HTTPSamplerBase.IMPLEMENTATION, HttpClientImpl.HTTP_CLIENT));
      return ret;
    }

  }

}
