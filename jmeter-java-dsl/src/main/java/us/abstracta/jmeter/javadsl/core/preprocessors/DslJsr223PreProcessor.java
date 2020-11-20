package us.abstracta.jmeter.javadsl.core.preprocessors;

import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.util.JSR223TestElement;
import us.abstracta.jmeter.javadsl.core.DslJsr223TestElement;
import us.abstracta.jmeter.javadsl.core.DslSampler.SamplerChild;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * Allows to run custom logic before executing a sampler.
 *
 * This is a very powerful and flexible component that allows you to modify variables, sampler,
 * context, etc, before running a sampler (for example to generate dynamic requests
 * programmatically).
 *
 * By default, provided script will be interpreted as groovy script, which is the default setting
 * for JMeter. If you need, you can use any of JMeter provided scripting languages (beanshell,
 * javascript, jexl, etc) by setting the {@link #language(String)} property.
 */
public class DslJsr223PreProcessor extends DslJsr223TestElement implements TestPlanChild,
    ThreadGroupChild, SamplerChild {

  public DslJsr223PreProcessor(String script) {
    super("JSR223 PreProcessor", script);
  }

  @Override
  protected JSR223TestElement buildJsr223TestElement() {
    return new JSR223PreProcessor();
  }

}
