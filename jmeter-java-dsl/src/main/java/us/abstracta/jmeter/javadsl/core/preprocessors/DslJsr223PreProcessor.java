package us.abstracta.jmeter.javadsl.core.preprocessors;

import java.util.Properties;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;
import us.abstracta.jmeter.javadsl.core.DslJsr223TestElement;
import us.abstracta.jmeter.javadsl.core.MultiLevelTestElement;

/**
 * Allows running custom logic before executing a sampler.
 * <p>
 * This is a very powerful and flexible component that allows you to modify variables, sampler,
 * context, etc., before running a sampler (for example to generate dynamic requests
 * programmatically).
 * <p>
 * By default, provided script will be interpreted as groovy script, which is the default setting
 * for JMeter. If you need, you can use any of JMeter provided scripting languages (beanshell,
 * javascript, jexl, etc.) by setting the {@link #language(String)} property.
 *
 * @since 0.7
 */
public class DslJsr223PreProcessor extends DslJsr223TestElement implements MultiLevelTestElement {

  private static final String DEFAULT_NAME = "JSR223 PreProcessor";

  public DslJsr223PreProcessor(String name, String script) {
    super(name, DEFAULT_NAME, script);
  }

  public DslJsr223PreProcessor(String name, PreProcessorScript script) {
    super(name, DEFAULT_NAME, script, PreProcessorVars.class);
  }

  @Override
  protected JSR223TestElement buildJsr223TestElement() {
    return new JSR223PreProcessor();
  }

  /**
   * Allows to use any java code as script.
   *
   * @see PreProcessorVars for a list of provided variables in script execution
   * @since 0.10
   */
  public interface PreProcessorScript extends Jsr223Script<PreProcessorVars> {

  }

  public static class PreProcessorVars extends Jsr223ScriptVars {

    public PreProcessorVars(JMeterContext ctx, JMeterVariables vars, Properties props,
        Sampler sampler, Logger log, String label) {
      super(ctx, vars, props, sampler, log, label);
    }

  }

}
