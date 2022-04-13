package us.abstracta.jmeter.javadsl.http;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.StringParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleGuiClassCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.core.configs.BaseConfigElement;
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
   * Allows specifying which http client implementation to use by default for HTTP samplers.
   * <p>
   * This can be overwritten by {@link DslHttpSampler#clientImpl(HttpClientImpl)}.
   *
   * @param clientImpl the HTTP client implementation to use. If none is specified, then {@link
   *                   DslHttpSampler.HttpClientImpl#HTTP_CLIENT} is used.
   * @return the HTTP Defaults instance for further configuration or usage.
   * @see DslHttpSampler.HttpClientImpl
   * @see DslHttpSampler#clientImpl(HttpClientImpl)
   */
  public DslHttpDefaults clientImpl(HttpClientImpl clientImpl) {
    this.clientImpl = clientImpl;
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
      StringParam protocol = paramBuilder.stringParam(HTTPSamplerBase.PROTOCOL);
      StringParam host = paramBuilder.stringParam(HTTPSamplerBase.DOMAIN);
      StringParam port = paramBuilder.stringParam(HTTPSamplerBase.PORT);
      StringParam path = paramBuilder.stringParam(HTTPSamplerBase.PATH, "/");

      if (!protocol.isDefault() && !host.isDefault()) {
        ret.chain("url", new StringParam(
            protocol.getValue() + "://" + host.getValue() + (port.isDefault() ? ""
                : ":" + port.getValue()) + (path.isDefault() ? "" : path.getValue())));
      } else {
        ret.chain("protocol", protocol)
            .chain("host", host)
            .chain("port", port)
            .chain("path", path);
      }

      ret.chain("encoding", new EncodingParam(paramBuilder))
          .chain("downloadEmbeddedResources",
              paramBuilder.boolParam(HTTPSamplerBase.IMAGE_PARSER, false))
          .chain("clientImpl", new ClientImplParam(paramBuilder));
      return ret;
    }

  }

}
