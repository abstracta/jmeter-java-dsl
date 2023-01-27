package us.abstracta.jmeter.javadsl.http;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam;

/**
 * Allows configuring cookies settings used by HTTP samplers.
 * <p>
 * This element can only be added as child of test plan, and currently allows only to disable HTTP
 * cookies handling which is enabled by default (emulating browser behavior).
 * <p>
 * This element has to be added before any http sampler to be considered, and if you add multiple
 * instances of cookie manager to a test plan, only the first one will be considered.
 *
 * @since 0.17
 */
public class DslCookieManager extends AutoEnabledHttpConfigElement {

  protected CookiePolicy cookiePolicy = CookiePolicy.STANDARD;
  protected boolean clearEachIteration = true;

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

  @Override
  protected TestElement buildTestElement() {
    CookieManager ret = new CookieManager();
    ret.setClearEachIteration(clearEachIteration);
    ret.setCookiePolicy(cookiePolicy.propertyValue);
    return ret;
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
      if (!clearBetweenIterations.isDefault() || !policy.isDefault()) {
        return buildMethodCall()
            .chain("clearCookiesBetweenIterations", clearBetweenIterations)
            .chain("cookiePolicy", policy);
      } else {
        return super.buildMethodCall(context);
      }
    }

  }

}
