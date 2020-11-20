package us.abstracta.jmeter.javadsl.core.postprocessors;

import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.util.JSR223TestElement;
import us.abstracta.jmeter.javadsl.core.DslJsr223TestElement;
import us.abstracta.jmeter.javadsl.core.DslSampler.SamplerChild;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * Allows to run custom logic after getting a sample result.
 *
 * This is a very powerful and flexible component that allows you to modify sample results (like
 * changing the flag if is success or not), jmeter variables, context settings, etc.
 *
 * By default, provided script will be interpreted as groovy script, which is the default setting
 * for JMeter. If you need, you can use any of JMeter provided scripting languages (beanshell,
 * javascript, jexl, etc) by setting the {@link #language(String)} property.
 */
public class DslJsr223PostProcessor extends DslJsr223TestElement implements TestPlanChild,
    ThreadGroupChild, SamplerChild {

  public DslJsr223PostProcessor(String script) {
    super("JSR223 PostProcessor", script);
  }

  @Override
  protected JSR223TestElement buildJsr223TestElement() {
    return new JSR223PostProcessor();
  }

}
