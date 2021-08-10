package us.abstracta.jmeter.javadsl.http;

import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.TestPlanSingletonChildElement;

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
public class DslCookieManager extends TestPlanSingletonChildElement {

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

  @Override
  protected TestElement buildTestElement() {
    CookieManager ret = new CookieManager();
    ret.setClearEachIteration(true);
    return ret;
  }

}
