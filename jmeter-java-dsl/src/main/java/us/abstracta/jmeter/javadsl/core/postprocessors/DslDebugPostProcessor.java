package us.abstracta.jmeter.javadsl.core.postprocessors;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.extractor.DebugPostProcessor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
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

  protected boolean includeSamplerProperties = false;
  protected boolean includeVariables = true;
  protected boolean includeJmeterProperties = false;
  protected boolean includeSystemProperties = false;

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
   * Specifies to include parent sampler properties (parameters of the sampler) in sub sample
   * response body.
   * <p>
   * This is just a shorter way to enable {@link #samplerProperties(boolean)}.
   *
   * @return the post processor for further configuration or usage.
   * @see #samplerProperties(boolean)
   * @since 1.0
   */
  public DslDebugPostProcessor samplerProperties() {
    return samplerProperties(true);
  }

  /**
   * Specifies if parent sampler properties (parameters of the sampler) should or not be included in
   * sub sample response body.
   * <p>
   * This method is useful when you want to review some dynamic sampler property.
   *
   * @param include if true, sampler properties will be included in sub sample response body,
   *                otherwise they won't. By default, sampler properties are not included.
   * @return the post processor for further configuration or usage.
   */
  public DslDebugPostProcessor samplerProperties(boolean include) {
    includeSamplerProperties = include;
    return this;
  }

  /**
   * Specifies to include JMeter properties (test plan parameters, or info shared by threads) in sub
   * sample response body.
   * <p>
   * This is just a shorter way to enable {@link #jmeterProperties(boolean)}.
   *
   * @return the post processor for further configuration or usage.
   * @see #jmeterProperties(boolean)
   * @since 1.0
   */
  public DslDebugPostProcessor jmeterProperties() {
    return jmeterProperties(true);
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
   * @return the post processor for further configuration or usage.
   */
  public DslDebugPostProcessor jmeterProperties(boolean include) {
    includeJmeterProperties = include;
    return this;
  }

  /**
   * Specifies to include system properties (JVM parameters and properties) in sub sample response
   * body.
   * <p>
   * This is just a shorter way to enable {@link #systemProperties(boolean)}.
   *
   * @return the post processor for further configuration or usage.
   * @see #systemProperties(boolean)
   * @since 1.0
   */
  public DslDebugPostProcessor systemProperties() {
    return systemProperties(true);
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
   * @return the post processor for further configuration or usage.
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

  public static class CodeBuilder extends SingleTestElementCallBuilder<DebugPostProcessor> {

    public CodeBuilder(List<Method> builderMethods) {
      super(DebugPostProcessor.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(DebugPostProcessor testElement,
        MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      return buildMethodCall()
          .chain("jmeterVariables", paramBuilder.boolParam("displayJMeterVariables", true))
          .chain("samplerProperties", paramBuilder.boolParam("displaySamplerProperties", false))
          .chain("jmeterProperties", paramBuilder.boolParam("displayJMeterProperties", false))
          .chain("systemProperties", paramBuilder.boolParam("displaySystemProperties", false));
    }

  }

}
