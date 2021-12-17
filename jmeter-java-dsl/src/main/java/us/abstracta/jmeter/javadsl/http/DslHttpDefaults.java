package us.abstracta.jmeter.javadsl.http;

import java.nio.charset.Charset;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.configs.DslConfigElement;
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
public class DslHttpDefaults extends DslConfigElement {

  private String url;
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
    this.url = url;
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
    if (url != null) {
      /*
       url is decomposed and not just set on path, to allow in samplers to override just path and
       reuse other default settings
       */
      JmeterUrl parsedUrl = JmeterUrl.valueOf(url);
      ret.setProperty(HTTPSamplerBase.PROTOCOL, parsedUrl.protocol());
      ret.setProperty(HTTPSamplerBase.DOMAIN, parsedUrl.host());
      ret.setProperty(HTTPSamplerBase.PORT, parsedUrl.port());
      ret.setProperty(HTTPSamplerBase.PATH, parsedUrl.path());
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

}
