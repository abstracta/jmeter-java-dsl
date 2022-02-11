package us.abstracta.jmeter.javadsl.core.postprocessors;

import org.apache.jmeter.extractor.DebugPostProcessor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;

/**
 * Adds a sub result to a given sampler result, including jmeter variables, jmeter properties, etc.,
 * which are handy when debugging test plans.
 * <p>
 * This element is particularly helpful when debugging extractors, since it allows checking
 * generated JMeter variables.
 * <p>
 * This element by default will only include JMeter variables in sub sample response body, which
 * differs from JMeter default configuration, since is the most used scenario and avoids unnecessary
 * memory or disk usage.
 * <p>
 * In general use IDE debugger in first steps of debugging a test plan using a thread group
 * configuration with only 1 thread. If you need to get more information on a test plan with
 * multiple threads during or after test plan execution, then use this element, in combination with
 * <pre>resultsTreeVisualizer</pre> or <pre>jtlWriter</pre> saving sub results and response bodies
 * in xml format, avoiding stopping in each thread break point and avoid affecting the performance
 * test metrics.
 *
 * @since 0.47
 */
public class DslDebugPostProcessor extends BaseTestElement implements DslPostProcessor {

  private boolean includeSamplerProperties = false;
  private boolean includeVariables = true;
  private boolean includeJmeterProperties = false;
  private boolean includeSystemProperties = false;

  public DslDebugPostProcessor() {
    super("Debug PostProcessor", TestBeanGUI.class);
  }

  /**
   * Specifies if JMeter variables should or not be included in sub sample response body.
   * <p>
   * This method is useful when you want to disable inclusion of JMeter variables and enable some
   * other info instead (like JMeter properties) and keep memory and disk usage to the minimum.
   * Another scenario might be to dynamically include them or not according to some test plan
   * parameter.
   *
   * @param include if true, JMeter variables will be included in sub sample response body,
   *                otherwise they won't. By default, jmeter variables are included.
   * @return the debug post processor for further configuration or usage.
   */
  public DslDebugPostProcessor jmeterVariables(boolean include) {
    includeVariables = include;
    return this;
  }

  /**
   * Specifies if parent sampler properties (parameters of the sampler) should or not be included in
   * sub sample response body.
   * <p>
   * This method is useful when you want to review some dynamic sampler property.
   *
   * @param include if true, sampler properties will be included in sub sample response body,
   *                otherwise they won't. By default, sampler properties are not included.
   * @return the debug post processor for further configuration or usage.
   */
  public DslDebugPostProcessor samplerProperties(boolean include) {
    includeSamplerProperties = include;
    return this;
  }

  /**
   * Specifies if JMeter properties (test plan parameters, or info shared by threads) should or not
   * be included in sub sample response body.
   * <p>
   * This method is useful when you want to review some test plan parameter provided through JMeter
   * property, or when you want to review some property shared and modified by test plan threads.
   *
   * @param include if true, JMeter properties will be included in sub sample response body,
   *                otherwise they won't. By default, JMeter properties are not included.
   * @return the debug post processor for further configuration or usage.
   */
  public DslDebugPostProcessor jmeterProperties(boolean include) {
    includeJmeterProperties = include;
    return this;
  }

  /**
   * Specifies if system properties (JVM parameters and properties) should or not be included in sub
   * sample response body.
   * <p>
   * This method is useful when you want to review some JVM parameter or property while executing
   * the test plan. In general, since system properties are not usually modified on runtime, it is
   * recommended to only enable this in a sampler that only runs once in a test plan (not inside
   * thread groups with multiple iterations), to avoid unnecessary memory or disk usage
   *
   * @param include if true, system properties will be included in sub sample response body,
   *                otherwise they won't. By default, system properties are not included.
   * @return the debug post processor for further configuration or usage.
   */
  public DslDebugPostProcessor systemProperties(boolean include) {
    includeSystemProperties = include;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    DebugPostProcessor ret = new DebugPostProcessor();
    ret.setDisplayJMeterVariables(includeVariables);
    ret.setDisplaySamplerProperties(includeSamplerProperties);
    ret.setDisplayJMeterProperties(includeJmeterProperties);
    ret.setDisplaySystemProperties(includeSystemProperties);
    return ret;
  }

}
