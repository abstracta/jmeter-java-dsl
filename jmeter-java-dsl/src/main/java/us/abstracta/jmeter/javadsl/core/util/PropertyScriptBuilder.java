package us.abstracta.jmeter.javadsl.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;

/**
 * Contains logic to create a script to be contained in a JMeter property (like if conditions).
 *
 * @since 0.27
 */
public class PropertyScriptBuilder extends DslScriptBuilder {

  public PropertyScriptBuilder(String script) {
    super(script);
  }

  public PropertyScriptBuilder(PropertyScript script) {
    super(script, PropertyScriptVars.class, buildVarsMapping());
  }

  private static Map<String, String> buildVarsMapping() {
    // These bindings are required because groovy function may or may not have the variables defined
    HashMap<String, String> ret = new HashMap<>();
    addOptionalVarMapping("prev", ret);
    addOptionalVarMapping("sampler", ret);
    return ret;
  }

  private static void addOptionalVarMapping(String varName, HashMap<String, String> ret) {
    ret.put(varName, "binding.hasVariable('" + varName + "') ? " + varName + " : null");
  }

  public String build() {
    return scriptString != null ? scriptString : JmeterFunction.groovy(super.build());
  }

  /**
   * Allows to use any java code as property.
   *
   * @see PropertyScriptBuilder.PropertyScriptVars for a list of provided variables in script
   * execution
   */
  public interface PropertyScript extends DslScript<PropertyScriptVars, Boolean> {

  }

  public static class PropertyScriptVars extends DslScriptVars {

    public final String threadName;

    public PropertyScriptVars(String threadName, SampleResult prev, JMeterContext ctx,
        JMeterVariables vars, Properties props, Sampler sampler, Logger log) {
      super(prev, ctx, vars, props, sampler, log);
      this.threadName = threadName;
    }

  }

}
