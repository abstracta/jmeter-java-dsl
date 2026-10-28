package us.abstracta.jmeter.javadsl.http;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.FixedParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.java.DslJsr223Sampler;

/**
 * Allows configuring cookies settings and adding custom cookies used by HTTP samplers.
 * <p>
 * Cookie settings (policy, clearing between iterations, enabling/disabling) apply to the scope
 * where the element is added (test plan, thread group or controller). To be considered, settings
 * have to be added before any HTTP sampler in such scope, and if multiple cookie managers with
 * settings apply to the same scope, only the first one is considered.
 * <p>
 * Custom cookies added with {@link #add(Cookie...)} are set differently depending on where the
 * element is located. Check {@link #add(Cookie...)} for details.
 *
 * @since 0.17
 */
public class DslCookieManager extends AutoEnabledHttpConfigElement {

  private static final String JMETER_COOKIE_CLASS =
      org.apache.jmeter.protocol.http.control.Cookie.class.getName();

  protected CookiePolicy cookiePolicy = CookiePolicy.STANDARD;
  protected boolean clearEachIteration = true;
  protected final List<Cookie> cookies = new ArrayList<>();

  public DslCookieManager() {
    super("HTTP Cookie Manager", CookiePanel.class);
  }

  /**
   * Disables HTTP cookies handling for the test plan.
   *
   * @return the DslCookieManager to allow fluent API usage.
   */
  public DslCookieManager disable() {
    enabled = false;
    return this;
  }

  /**
   * Allows to enable or disable clearing cookies between thread group iterations.
   * <p>
   * Cookies are cleared each iteration by default. If this is not desirable, for instance if
   * logging in once and then iterating through actions multiple times, use this to set to false.
   *
   * @param clear boolean to set clearing of cookies. By default, it is set to true.
   * @return the cookie manager for further configuration or usage.
   * @since 1.6
   */
  public DslCookieManager clearCookiesBetweenIterations(boolean clear) {
    this.clearEachIteration = clear;
    return this;
  }

  /**
   * Used to set the required cookie policy used to manage cookies.
   * <p>
   * You might need to change the 'standard' cookie policy if the application under test only
   * supports a specific cookie implementation.
   *
   * @param policy specifies the particular cookie policy to use. By default, it is set to standard
   *               cookie policy.
   * @return the cookie manager for further configuration or usage.
   * @see CookiePolicy
   * @since 1.6
   */
  public DslCookieManager cookiePolicy(DslCookieManager.CookiePolicy policy) {
    this.cookiePolicy = policy;
    return this;
  }

  /**
   * Adds HTTP cookies.
   * <p>
   * When this element is added as child of a test plan, the cookies are added as initial cookies
   * of the cookie manager, being available to all requests from the beginning of each thread
   * iteration.
   * <p>
   * When this element is added as child of a thread group or controller, the cookies are set at
   * that particular point of the test plan execution, so only requests executed after it include
   * them. This is implemented with a JSR223 sampler that adds the cookies to the thread cookie
   * manager and is excluded from collected statistics. Since it is a sampler, timers,
   * pre-processors, post-processors and assertions in its scope also apply to it.
   * <p>
   * Expired cookies are not added. Cookies names, values, domains and paths may use JMeter
   * expressions (eg: {@code ${MY_VAR}}).
   *
   * @param cookies specifies the cookies to add.
   * @return the cookie manager for further configuration or usage.
   * @see JmeterDsl#httpCookie(String, String, String)
   * @since 2.4
   */
  public DslCookieManager add(Cookie... cookies) {
    this.cookies.addAll(Arrays.asList(cookies));
    return this;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    if (cookies.isEmpty()) {
      return super.buildTreeUnder(parent, context);
    }
    if (!enabled) {
      throw new IllegalStateException(
          "Custom cookies can't be added to a disabled cookie manager.");
    }
    if (isTestPlanLevel(context)) {
      return super.buildTreeUnder(parent, context);
    }
    if (isDisabledInScope(context)) {
      throw new IllegalStateException("Custom cookies can't be added where cookies handling has "
          + "been disabled with httpCookies().disable().");
    }
    new DslCookieManager().registerDependency(context);
    context.buildChild(new DslJsr223Sampler("Set Cookies", buildCookiesScript()), parent);
    /*
     cookies are set by the sampler, so we use a copy without them for any non default settings
     to avoid setting cookies also as initial cookies of a nested cookie manager.
     */
    return isDefaultConfig() ? parent : copyWithoutCookies().buildTreeUnder(parent, context);
  }

  private boolean isTestPlanLevel(BuildTreeContext context) {
    BuildTreeContext parent = context.getParent();
    return parent == null || parent.getTestElement() instanceof DslTestPlan;
  }

  private boolean isDefaultConfig() {
    return cookiePolicy == CookiePolicy.STANDARD && clearEachIteration;
  }

  private DslCookieManager copyWithoutCookies() {
    DslCookieManager ret = new DslCookieManager();
    ret.cookiePolicy = cookiePolicy;
    ret.clearEachIteration = clearEachIteration;
    return ret;
  }

  private String buildCookiesScript() {
    StringBuilder ret = new StringBuilder()
        .append("def managers = new org.apache.jorphan.collections.SearchByClass(")
        .append(CookieManager.class.getName()).append(".class)\n")
        .append("ctx.getThread().getTestTree().traverse(managers)\n")
        .append("if (managers.searchResults.isEmpty()) {\n")
        .append("  log.warn('No HTTP cookie manager found, so custom cookies are not set.')\n")
        .append("}\n")
        .append("def now = System.currentTimeMillis()\n")
        .append("for (def manager : managers.searchResults) {\n");
    for (Cookie cookie : cookies) {
      ret.append("  ");
      if (cookie.expires != null) {
        ret.append("if (now < ").append(cookie.expires.toEpochMilli()).append("L) ");
      }
      ret.append("manager.add(").append(cookie.buildGroovyConstructor()).append(")\n");
    }
    return ret.append("}\n")
        .append("SampleResult.setIgnore()\n")
        .append("return null\n")
        .toString();
  }

  @Override
  protected TestElement buildTestElement() {
    CookieManager ret = new CookieManager();
    ret.setClearEachIteration(clearEachIteration);
    ret.setCookiePolicy(cookiePolicy.propertyValue);
    Instant now = Instant.now();
    cookies.stream()
        .filter(c -> !c.isExpiredAt(now))
        .forEach(c -> ret.add(c.toJMeterCookie()));
    return ret;
  }

  public enum CookiePolicy implements EnumParam.EnumPropertyValue {

    /**
     * Compliant with <a href="https://www.rfc-editor.org/rfc/rfc6265">RFC 6265</a> using a relaxed
     * interpretation of HTTP state management.
     * <p>
     * This is the default value, and should work in most cases.
     *
     * @see org.apache.http.impl.cookie.RFC6265LaxSpec
     */
    STANDARD("standard"),
    /**
     * Compliant with <a href="https://www.rfc-editor.org/rfc/rfc6265">RFC 6265</a> using strict
     * adherence to state management of RFC 6265 section 4.
     * <p>
     * This might come in handy when you actually want to verify that a service is in strict
     * adherence to the RFC.
     *
     * @see org.apache.http.impl.cookie.RFC6265StrictSpec
     */
    STANDARD_STRICT("standard-strict"),
    /**
     * Selects RFC 2965, RFC 2109 or Netscape draft compliant implementation based on cookies
     * properties sent with the HTTP response.
     * <p>
     * This is helpful to test browser compatibility with old versions of RFCs. In general prefer
     * using the STANDARD cookie policy.
     *
     * @see org.apache.http.impl.cookie.DefaultCookieSpec
     */
    BEST_MATCH("best-match"),
    /**
     * Compliant with <a href="https://www.rfc-editor.org/rfc/rfc2965">RFC 2965</a>.
     * <p>
     * This RFC is obsolete and replaced by RFC 6265, so this option should only be used in legacy
     * applications that use this RFC.
     * <p>
     * You may use {@link #BEST_MATCH} as alternative which in general should also be compatible
     * with RFC 2965.
     *
     * @see org.apache.http.impl.cookie.RFC2965Spec
     */
    RFC2965("rfc2965"),
    /**
     * Compliant with <a href="https://www.rfc-editor.org/rfc/rfc2109">RFC 2109</a>.
     * <p>
     * This RFC is obsolete and replaced by RFC 6265, so this option should only be used in legacy
     * applications that use this RFC.
     * <p>
     * You may use {@link #BEST_MATCH} as alternative which in general should also be compatible
     * with RFC 2109.
     *
     * @see org.apache.http.impl.cookie.RFC2109Spec
     */
    RFC2109("rfc2109"),
    /**
     * Conforms to the original draft specification published by Netscape Communications.
     * <p>
     * This should be in general be avoided, unless is necessary to test legacy applications.
     * <p>
     * You may use {@link #BEST_MATCH} as alternative which in general should also be compatible
     * with Netscape policy.
     *
     * @see org.apache.http.impl.cookie.NetscapeDraftSpec
     */
    NETSCAPE("netscape");

    private final String propertyValue;

    CookiePolicy(String cookiePolicy) {
      this.propertyValue = cookiePolicy;
    }

    @Override
    public String propertyValue() {
      return propertyValue;
    }

  }

  /**
   * Represents an HTTP cookie to be added with {@link DslCookieManager#add(Cookie...)}.
   * <p>
   * Prefer using {@link JmeterDsl#httpCookie(String, String, String)} to create instances.
   *
   * @since 2.4
   */
  public static class Cookie {

    private final String name;
    private final String value;
    private final String domain;
    private String path = "/";
    private boolean secure;
    private Instant expires;

    /**
     * Creates a cookie for the given domain.
     *
     * @param name   specifies the name of the cookie.
     * @param value  specifies the value of the cookie.
     * @param domain specifies the domain (eg: myservice.com) of requests to send the cookie to.
     *               The cookie is also sent to subdomains of the given domain.
     */
    public Cookie(String name, String value, String domain) {
      this.name = name;
      this.value = value;
      this.domain = domain;
    }

    /**
     * Restricts the cookie to requests whose path starts with the given one.
     *
     * @param path specifies the path. By default, it is set to "/".
     * @return the cookie for further configuration or usage.
     */
    public Cookie path(String path) {
      this.path = path;
      return this;
    }

    /**
     * Specifies if the cookie should only be sent in HTTPS requests.
     *
     * @param secure specifies whether to restrict the cookie to HTTPS. By default, it is false.
     * @return the cookie for further configuration or usage.
     */
    public Cookie secure(boolean secure) {
      this.secure = secure;
      return this;
    }

    /**
     * Specifies the instant after which the cookie is no longer sent.
     * <p>
     * Cookies already expired when they are set are not added.
     *
     * @param expires specifies the expiration instant. By default, cookies don't expire.
     * @return the cookie for further configuration or usage.
     */
    public Cookie expires(Instant expires) {
      this.expires = expires;
      return this;
    }

    private boolean isExpiredAt(Instant instant) {
      return expires != null && !expires.isAfter(instant);
    }

    org.apache.jmeter.protocol.http.control.Cookie toJMeterCookie() {
      return new org.apache.jmeter.protocol.http.control.Cookie(name, value, domain, path, secure,
          getExpiresEpochSecond(), true, true);
    }

    private long getExpiresEpochSecond() {
      return expires != null ? expires.getEpochSecond() : 0;
    }

    private String buildGroovyConstructor() {
      return String.format(Locale.ROOT, "new %s(%s, %s, %s, %s, %b, %dL, true, true)",
          JMETER_COOKIE_CLASS,
          groovyString(name), groovyString(value), groovyString(domain), groovyString(path),
          secure, getExpiresEpochSecond());
    }

    private static String groovyString(String value) {
      return "'" + value.replace("\\", "\\\\")
          .replace("'", "\\'")
          .replace("\r", "\\r")
          .replace("\n", "\\n") + "'";
    }

    static Cookie fromJMeterCookie(org.apache.jmeter.protocol.http.control.Cookie jmeterCookie) {
      Cookie ret = new Cookie(jmeterCookie.getName(), jmeterCookie.getValue(),
          jmeterCookie.getDomain())
          .path(jmeterCookie.getPath())
          .secure(jmeterCookie.getSecure());
      long expires = jmeterCookie.getExpires();
      if (expires > 0) {
        ret.expires(Instant.ofEpochSecond(expires));
      }
      return ret;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Cookie cookie = (Cookie) o;
      return secure == cookie.secure && Objects.equals(name, cookie.name)
          && Objects.equals(value, cookie.value) && Objects.equals(domain, cookie.domain)
          && Objects.equals(path, cookie.path) && Objects.equals(expires, cookie.expires);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, value, domain, path, secure, expires);
    }

    @Override
    public String toString() {
      return "Cookie{"
          + "name='" + name + '\''
          + ", value='" + value + '\''
          + ", domain='" + domain + '\''
          + ", path='" + path + '\''
          + ", secure=" + secure
          + ", expires=" + expires
          + '}';
    }

  }

  public static class CodeBuilder extends AutoEnabledHttpConfigElement.CodeBuilder<CookieManager> {

    public CodeBuilder(List<Method> builderMethods) {
      super(CookieManager.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      TestElement testElement = context.getTestElement();
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement,
          "CookieManager");
      MethodParam clearBetweenIterations = paramBuilder.boolParam("clearEachIteration",
          true);
      MethodParam policy = paramBuilder.enumParam("policy", CookiePolicy.STANDARD);
      CookiesParam cookies = new CookiesParam(((CookieManager) testElement).getCookies());
      if (!clearBetweenIterations.isDefault() || !policy.isDefault() || !cookies.isDefault()) {
        return buildMethodCall()
            .chain("clearCookiesBetweenIterations", clearBetweenIterations)
            .chain("cookiePolicy", policy)
            .chain("add", cookies);
      } else {
        return super.buildMethodCall(context);
      }
    }

  }

  private static class CookiesParam extends MethodParam {

    private final List<MethodCall> cookieCalls;

    private CookiesParam(CollectionProperty cookies) {
      super(Cookie[].class, null);
      List<MethodCall> calls = new ArrayList<>();
      cookies.forEach(prop -> calls.add(buildCookieCall(Cookie.fromJMeterCookie(
          (org.apache.jmeter.protocol.http.control.Cookie) prop.getObjectValue()))));
      this.cookieCalls = calls;
    }

    private static MethodCall buildCookieCall(Cookie cookie) {
      try {
        Method factory = JmeterDsl.class.getMethod("httpCookie", String.class, String.class,
            String.class);
        return MethodCall.fromBuilderMethod(factory, new StringParam(cookie.name),
                new StringParam(cookie.value), new StringParam(cookie.domain))
            .chain("path", new StringParam(cookie.path, "/"))
            .chain("secure", new BoolParam(cookie.secure, false))
            .chain("expires", new InstantParam(cookie.expires));
      } catch (NoSuchMethodException e) {
        // this should never happen since httpCookie is part of the DSL
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean isDefault() {
      return cookieCalls.isEmpty();
    }

    @Override
    public Set<String> getImports() {
      return cookieCalls.stream()
          .flatMap(c -> c.getImports().stream())
          .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getStaticImports() {
      return cookieCalls.stream()
          .flatMap(c -> c.getStaticImports().stream())
          .collect(Collectors.toSet());
    }

    @Override
    protected String buildCode(String indent) {
      return cookieCalls.stream()
          .map(c -> c.buildCode(indent))
          .collect(Collectors.joining(", "));
    }

  }

  private static class InstantParam extends FixedParam<Instant> {

    private InstantParam(Instant value) {
      super(Instant.class, value, null);
    }

    @Override
    public Set<String> getImports() {
      return Collections.singleton(Instant.class.getName());
    }

    @Override
    public String buildCode(String indent) {
      return Instant.class.getSimpleName() + ".ofEpochSecond(" + value.getEpochSecond() + "L)";
    }

  }

}
