package us.abstracta.jmeter.javadsl.core.postprocessors;

import java.util.Properties;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;
import us.abstracta.jmeter.javadsl.core.DslJsr223TestElement;
import us.abstracta.jmeter.javadsl.core.MultiLevelTestElement;

/**
 * Allows to run custom logic after getting a sample result.
 * <p>
 * This is a very powerful and flexible component that allows you to modify sample results (like
 * changing the flag if is success or not), jmeter variables, context settings, etc.
 * <p>
 * By default, provided script will be interpreted as groovy script, which is the default setting
 * for JMeter. If you need, you can use any of JMeter provided scripting languages (beanshell,
 * javascript, jexl, etc) by setting the {@link #language(String)} property.
 */
public class DslJsr223PostProcessor extends DslJsr223TestElement implements MultiLevelTestElement {

  private static final String DEFAULT_NAME = "JSR223 PostProcessor";

  public DslJsr223PostProcessor(String name, String script) {
    super(name, DEFAULT_NAME, script);
  }

  public DslJsr223PostProcessor(String name, PostProcessorScript script) {
    super(name, DEFAULT_NAME, script, PostProcessorVars.class);
  }

  @Override
  protected JSR223TestElement buildJsr223TestElement() {
    return new JSR223PostProcessor();
  }

  /**
   * Allows to use any java code as script.
   *
   * @see PostProcessorVars for a list of provided variables in script execution
   */
  public interface PostProcessorScript extends Jsr223Script<PostProcessorVars> {

  }

  public static class PostProcessorVars extends Jsr223ScriptVars {

    public SampleResult prev;

    public PostProcessorVars(SampleResult prev, JMeterContext ctx, JMeterVariables vars,
        Properties props, Sampler sampler, Logger log, String label) {
      super(ctx, vars, props, sampler, log, label);
      this.prev = prev;
    }

  }

}
