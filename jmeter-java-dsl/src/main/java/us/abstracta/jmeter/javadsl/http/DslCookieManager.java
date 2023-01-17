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
 * @since 0.17
 */
public class DslCookieManager extends AutoEnabledHttpConfigElement {

  protected String cookiePolicy;
  protected boolean clearEachIteration = false;

  public enum CookiePolicy {
    STANDARDSTRICT("standard-strict"),
    IGNORECOOKIES("ignoreCookies"),
    NETSCAPE("netscape"),
    RFC2109("rfc2109"),
    RFC2965("rfc2965"),
    BESTMATCH("best-match"),
    COMPATABILITY("compatability");

    private final String cookiePolicy;

    CookiePolicy(String cookiePolicy) {

      this.cookiePolicy = cookiePolicy;
    }

    public String getCookiePolicy() {

      return this.cookiePolicy;
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

  public DslCookieManager setClearingBetweenIterations(boolean clear) {
    this.clearEachIteration = clear;
    return this;
  }

  public DslCookieManager setCookiePolicy(DslCookieManager.CookiePolicy policy) {

    this.cookiePolicy = policy.getCookiePolicy();
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
