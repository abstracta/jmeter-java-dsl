package us.abstracta.jmeter.javadsl.http2;

import com.blazemeter.jmeter.http2.sampler.HTTP2Sampler;
import com.blazemeter.jmeter.http2.sampler.gui.HTTP2SamplerGui;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam.EnumPropertyValue;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;
import us.abstracta.jmeter.javadsl.http.DslBaseHttpSampler;
import us.abstracta.jmeter.javadsl.http.DslCacheManager;
import us.abstracta.jmeter.javadsl.http.DslCookieManager;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;
import us.abstracta.jmeter.javadsl.http.HttpElementHelper;

/**
 * Allows configuring a BlazeMeter HTTP sampler with support for HTTP/1.1, HTTP/2, and HTTP/3.
 * <p>
 * This sampler uses the
 * <a href="https://github.com/Blazemeter/jmeter-http2-plugin">BlazeMeter HTTP plugin</a> and
 * requires Java 17+ at runtime. It supports the same HTTP request features as
 * {@link us.abstracta.jmeter.javadsl.http.DslHttpSampler} (headers, body, cookies, cache, etc.)
 * plus multi-protocol client configuration.
 *
 * @since 2.3
 */
public class DslHttp2Sampler extends DslHttpSampler {

  private static final String DEFAULT_NAME = "bzm - HTTP Sampler";
  private static final String PROFILE_PROPERTY = "HTTP2Sampler.profile";

  protected ClientProfile clientProfile;
  protected Boolean enableHttp3;
  protected Boolean enableHttp2;
  protected Boolean enableHttp1;
  protected Boolean alpnEnabled;
  protected Boolean automaticFallback;
  protected Boolean protocolErrorFallback;
  protected Boolean h2cUpgrade;
  protected Boolean http2PriorKnowledge;

  public DslHttp2Sampler(String name, String url) {
    super(name != null ? name : DEFAULT_NAME, url);
  }

  /**
   * Specifies the client profile which defines default protocol negotiation behavior.
   *
   * @param profile the client profile to use. When none is specified, the plugin default
   *                ({@link ClientProfile#BROWSER_LIKE}) is used.
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler clientProfile(ClientProfile profile) {
    this.clientProfile = profile;
    return this;
  }

  /**
   * Enables or disables HTTP/3 (QUIC) support for this sampler.
   *
   * @param enable when {@code true}, allows HTTP/3 when the origin advertises it via Alt-Svc.
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler enableHttp3(boolean enable) {
    this.enableHttp3 = enable;
    return this;
  }

  /**
   * Enables or disables HTTP/2 support for this sampler.
   *
   * @param enable when {@code true}, allows HTTP/2 negotiation via ALPN (HTTPS) or h2c (HTTP).
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler enableHttp2(boolean enable) {
    this.enableHttp2 = enable;
    return this;
  }

  /**
   * Enables or disables HTTP/1.1 support for this sampler.
   *
   * @param enable when {@code true}, allows HTTP/1.1 requests and fallback.
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler enableHttp1(boolean enable) {
    this.enableHttp1 = enable;
    return this;
  }

  /**
   * Enables or disables TLS ALPN for HTTPS connections.
   *
   * @param enable when {@code true}, enables ALPN for HTTP/1.1 and HTTP/2 protocol negotiation.
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler alpnEnabled(boolean enable) {
    this.alpnEnabled = enable;
    return this;
  }

  /**
   * Enables or disables automatic fallback between protocols (e.g. HTTP/3 to HTTP/2 to HTTP/1.1).
   *
   * @param enable when {@code true}, enables automatic protocol fallback.
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler automaticFallback(boolean enable) {
    this.automaticFallback = enable;
    return this;
  }

  /**
   * Enables or disables fallback to HTTP/1.1 when an HTTP/2 {@code protocol_error} occurs.
   *
   * @param enable when {@code true}, retries with HTTP/1.1 on protocol errors.
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler protocolErrorFallback(boolean enable) {
    this.protocolErrorFallback = enable;
    return this;
  }

  /**
   * Enables HTTP/1.1 to HTTP/2 cleartext (h2c) upgrade for {@code http://} origins.
   *
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler h2cUpgrade() {
    return h2cUpgrade(true);
  }

  /**
   * Same as {@link #h2cUpgrade()} but allowing to enable or disable the setting.
   *
   * @param enable when {@code true}, enables h2c upgrade negotiation.
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler h2cUpgrade(boolean enable) {
    this.h2cUpgrade = enable;
    return this;
  }

  /**
   * Forces HTTP/2 prior knowledge (h2c) for cleartext origins, skipping the upgrade negotiation.
   * <p>
   * Use only when the server is known to support h2c prior knowledge.
   *
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler http2PriorKnowledge() {
    return http2PriorKnowledge(true);
  }

  /**
   * Same as {@link #http2PriorKnowledge()} but allowing to enable or disable the setting.
   *
   * @param enable when {@code true}, forces h2c prior knowledge.
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler http2PriorKnowledge(boolean enable) {
    this.http2PriorKnowledge = enable;
    return this;
  }

  /**
   * Configures the sampler to use HTTP/2 only, disabling HTTP/1.1 and HTTP/3.
   * <p>
   * For {@code https://} origins, ALPN is enabled to negotiate HTTP/2 over TLS.
   *
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler http2Only() {
    return clientProfile(ClientProfile.BROWSER_LIKE_CUSTOM)
        .enableHttp1(false)
        .enableHttp2(true)
        .enableHttp3(false)
        .alpnEnabled(true)
        .automaticFallback(false);
  }

  /**
   * Configures the sampler to use HTTP/1.1 only, disabling HTTP/2 and HTTP/3.
   *
   * @return the sampler for further configuration or usage.
   */
  public DslHttp2Sampler http1Only() {
    return clientProfile(ClientProfile.BROWSER_LIKE_CUSTOM)
        .enableHttp1(true)
        .enableHttp2(false)
        .enableHttp3(false);
  }

  @Override
  protected TestElement buildConfiguredTestElement() {
    TestElement ret = buildTestElement();
    return BaseTestElement.configureTestElement(ret, name, HTTP2SamplerGui.class);
  }

  @Override
  protected TestElement buildTestElement() {
    if (JMeterUtils.getProperty(DslBaseHttpSampler.RESET_CONNECTIONS_BETWEEN_ITERATIONS_PROP)
        == null) {
      JMeterUtils.setProperty(DslBaseHttpSampler.RESET_CONNECTIONS_BETWEEN_ITERATIONS_PROP,
          String.valueOf(false));
    }
    HTTP2Sampler ret = new HTTP2Sampler();
    HttpElementHelper.modifyTestElementUrl(ret, protocol, host, port, path);
    HttpElementHelper.modifyTestElementTimeouts(ret, connectionTimeout, responseTimeout);
    HttpElementHelper.modifyTestElementProxy(ret, proxyUrl, proxyUser, proxyPassword);
    configureHttp2Sampler(ret);
    return ret;
  }

  private void configureHttp2Sampler(HTTP2Sampler elem) {
    HTTPSamplerProxy proxy = configureHttpTestElement(new HTTPSamplerProxy());
    copyHttpSamplerProperties(proxy, elem);
    if (clientProfile != null) {
      elem.setProfile(clientProfile.propertyValue);
    }
    if (enableHttp3 != null) {
      elem.setEnableHttp3(enableHttp3);
    }
    if (enableHttp2 != null) {
      elem.setEnableHttp2(enableHttp2);
    }
    if (enableHttp1 != null) {
      elem.setEnableHttp1(enableHttp1);
    }
    if (alpnEnabled != null) {
      elem.setAlpnEnabled(alpnEnabled);
    }
    if (automaticFallback != null) {
      elem.setFallbackEnabled(automaticFallback);
    }
    if (protocolErrorFallback != null) {
      elem.setProtocolErrorFallbackEnabled(protocolErrorFallback);
    }
    if (h2cUpgrade != null) {
      elem.setHttp1UpgradeEnabled(h2cUpgrade);
    }
    if (http2PriorKnowledge != null) {
      elem.setHttp2PriorKnowledgeEnabled(http2PriorKnowledge);
    }
  }

  private void copyHttpSamplerProperties(HTTPSamplerBase source, HTTP2Sampler target) {
    target.setMethod(source.getMethod());
    target.setArguments(cloneArguments(source.getArguments()));
    target.setDoMultipart(source.getUseMultipart());
    target.setHTTPFiles(source.getHTTPFiles());
    if (source.getContentEncoding() != null) {
      target.setContentEncoding(source.getContentEncoding());
    }
    target.setFollowRedirects(source.getFollowRedirects());
    target.setUseKeepAlive(source.getUseKeepAlive());
    if (source.getPropertyAsBoolean(HTTPSamplerBase.IMAGE_PARSER, false)) {
      HttpElementHelper.modifyTestElementEmbeddedResources(target, true,
          source.getPropertyAsString(HTTPSamplerBase.EMBEDDED_URL_RE),
          source.getPropertyAsString(HTTPSamplerBase.EMBEDDED_URL_EXCLUDE_RE));
    }
  }

  private Arguments cloneArguments(Arguments args) {
    Arguments ret = new Arguments();
    for (int i = 0; i < args.getArgumentCount(); i++) {
      ret.addArgument((HTTPArgument) args.getArgument(i).clone());
    }
    return ret;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    files.forEach(f -> f.setPath(context.processAssetFile(f.getPath())));
    if (path == null && urlBuilder != null) {
      path = urlBuilder.build();
    }
    HashTree ret = parent.add(buildConfiguredTestElement());
    if (!headers.isEmpty()) {
      context.buildChild(headers, ret);
    }
    new DslCookieManager().registerDependency(context);
    new DslCacheManager().registerDependency(context);
    return ret;
  }

  /**
   * Defines client profiles that bundle default protocol negotiation settings.
   */
  public enum ClientProfile implements EnumPropertyValue {
    /**
     * Browser-like profile with HTTP/3, HTTP/2, HTTP/1.1 and automatic fallback enabled.
     */
    BROWSER_LIKE("browser-like"),
    /**
     * Browser-compatible profile for less common browsers.
     */
    BROWSER_COMPATIBLE("browser-compatible"),
    /**
     * Legacy profile oriented to older systems.
     */
    LEGACY("legacy"),
    /**
     * Custom profile where per-sampler protocol toggles are used.
     */
    BROWSER_LIKE_CUSTOM("browser-like-custom");

    public final String propertyValue;

    ClientProfile(String propertyValue) {
      this.propertyValue = propertyValue;
    }

    @Override
    public String propertyValue() {
      return propertyValue;
    }
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<HTTP2Sampler> {

    public CodeBuilder(List<Method> builderMethods) {
      super(HTTP2Sampler.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(HTTP2Sampler testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodParam name = paramBuilder.nameParam(DEFAULT_NAME);
      MethodParam protocol = paramBuilder.stringParam(HTTPSamplerBase.PROTOCOL);
      MethodParam domain = paramBuilder.stringParam(HTTPSamplerBase.DOMAIN);
      MethodParam port = paramBuilder.intParam(HTTPSamplerBase.PORT);
      MethodParam path = paramBuilder.stringParam(HTTPSamplerBase.PATH, "/");
      MethodParam url = HttpElementHelper.buildUrlParam(protocol, domain, port, path);
      MethodCall ret = buildMethodCall(name, url);
      context.findBuilder(DslCacheManager.CodeBuilder.class).registerDependency(context, ret);
      context.findBuilder(DslCookieManager.CodeBuilder.class).registerDependency(context, ret);
      if (url.equals(path)) {
        ret.chain("protocol", protocol)
            .chain("host", domain)
            .chain("port", port);
      }
      ret.chain("clientProfile",
          paramBuilder.enumParam(PROFILE_PROPERTY, ClientProfile.BROWSER_LIKE));
      chainOptionalBool(ret, testElement.getEnableHttp3(), "enableHttp3");
      chainOptionalBool(ret, testElement.getEnableHttp2(), "enableHttp2");
      chainOptionalBool(ret, testElement.getEnableHttp1(), "enableHttp1");
      chainOptionalBool(ret, testElement.getAlpnEnabled(), "alpnEnabled");
      chainOptionalBool(ret, testElement.getFallbackEnabled(), "automaticFallback");
      chainOptionalBool(ret, testElement.getProtocolErrorFallbackEnabled(),
          "protocolErrorFallback");
      if (testElement.isHttp1UpgradeEnabled()) {
        ret.chain("h2cUpgrade", new BoolParam(true, false));
      }
      chainOptionalBool(ret, testElement.getHttp2PriorKnowledgeEnabled(), "http2PriorKnowledge");
      return ret;
    }

    private void chainOptionalBool(MethodCall ret, Boolean value, String methodName) {
      if (value != null) {
        ret.chain(methodName, new BoolParam(value, null));
      }
    }
  }

}
