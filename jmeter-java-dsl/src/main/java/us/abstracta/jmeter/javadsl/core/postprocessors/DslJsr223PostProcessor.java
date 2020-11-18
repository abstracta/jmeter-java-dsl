package us.abstracta.jmeter.javadsl.core.postprocessors;

import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.DslSampler.SamplerChild;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * Allows to run custom logic after getting a sample result.
 *
 * This is a very powerful and flexible component that allows you to modify sample results (like
 * changing the flag if is success or not), jmeter variables, context settings, etc.
 *
 * By default, provided script will be interpreted as groovy script, which is the most performant
 * and default setting for JMeter. If you need, you can use any of JMeter provided scripting
 * languages (beanshell, javascript, jexl, etc) by setting the {@link #language(String)} property.
 */
public class DslJsr223PostProcessor extends BaseTestElement implements TestPlanChild,
    ThreadGroupChild,
    SamplerChild {

  private final String script;
  private String language = "groovy";

  public DslJsr223PostProcessor(String script) {
    super("JSR223 PostProcessor", TestBeanGUI.class);
    this.script = script;
  }

  public void language(String language) {
    this.language = language;
  }

  @Override
  protected TestElement buildTestElement() {
    JSR223PostProcessor ret = new JSR223PostProcessor();
    ret.setProperty("script", script);
    ret.setProperty("scriptLanguage", language);
    return ret;
  }

}
