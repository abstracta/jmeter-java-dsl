package us.abstracta.jmeter.javadsl.http;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.testelement.TestElement;

/**
 * Allows configuring cookies settings used by HTTP samplers.
 * <p>
 * This element can only be added as child of test plan, and currently allows only to disable HTTP
 * cookies handling which is enabled by default (emulating browser behavior).
 * <p>
 * This element has to be added before any http sampler to be considered, and if you add multiple
 * instances of cookie manager to a test plan, only the first one will be considered.
 *
 * Clearing cookies on each iteration is defaulted to true but setClearCookiesBetweenIterations()
 * can be used to set to false if required.
 * The default cookie policy is 'standard' but setCookiePolicy() can be used to specify the
 * required cookie policy
 *
 * @since 0.17
 */
public class DslCookieManager extends AutoEnabledHttpConfigElement {

  protected String cookiePolicy;
  protected boolean clearEachIteration = true;

  public enum CookiePolicy {
    /**
     *Compliant with the well-behaved profile defined by RFC 6265, section 4.
     */
    STANDARDSTRICT("standard-strict"),

    /**
     * All cookies are ignored. Same as delete or disable Cookie Manager.
     */
    IGNORECOOKIES("ignoreCookies"),

    /**
     * Corresponds to the original draft specification published by Netscape Communications.
     */
    NETSCAPE("netscape"),

    /**
     * Select RFC 2965, RFC 2109 or Netscape draft compliant implementation based on cookies
     * properties sent with the HTTP response.
     */
    DEFAULT("default"),

    /**
     * Compliant with the specification defined by RFC 2109.
     */
    RFC2109("rfc2109"),

    /**
     * Compliant with the specification defined by RFC 2965.
     */
    RFC2965("rfc2965"),

    /**
     *
     */
    BESTMATCH("best-match"),

    /**
     * Simulates the behavior of older versions of browsers like Mozilla FireFox and Internet
     * Explorer
     */
    COMPATABILITY("compatability");

    private final String cookiePolicy;

    CookiePolicy(String cookiePolicy) {

      this.cookiePolicy = cookiePolicy;
    }
  }

  public DslCookieManager() {
    super("HTTP Cookie Manager", CookiePanel.class);
  }

  /**
   * disables HTTP cookies handling for the test plan.
   *
   * @return the DslCookieManager to allow fluent API usage.
   */
  public DslCookieManager disable() {
    enabled = false;
    return this;
  }

  /**
   * Cookies are cleared each iteration by default. If this is not desirable, for instance if
   * logging in once and then iterating through actions multiple times, use this to set to false.
   * @param clear boolean to set clearing of cookies
   * @return the cookie manager for further configuration or usage.
   * @since 1.6
   */
  public DslCookieManager clearCookiesBetweenIterations(boolean clear) {
    this.clearEachIteration = clear;
    return this;
  }

  /**
   * Used to set the required cookie policy If 'standard' cookie types are not suitable.
   * @param policy ENUM for the cookie policy
   * @return the cookie manager for further configuration or usage.
   * @since 1.6
   */
  public DslCookieManager cookiePolicy(DslCookieManager.CookiePolicy policy) {

    this.cookiePolicy = policy.cookiePolicy;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    CookieManager ret = new CookieManager();
    ret.setClearEachIteration(clearEachIteration);
    ret.setCookiePolicy(cookiePolicy);
    return ret;
  }

  public static class CodeBuilder extends AutoEnabledHttpConfigElement.CodeBuilder<CookieManager> {

    public CodeBuilder(List<Method> builderMethods) {
      super(CookieManager.class, builderMethods);
    }
  }
}
