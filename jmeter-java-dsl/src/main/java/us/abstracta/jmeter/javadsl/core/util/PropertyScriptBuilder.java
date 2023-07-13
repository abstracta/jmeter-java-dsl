package us.abstracta.jmeter.javadsl.core.util;

import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import us.abstracta.jmeter.javadsl.core.util.DslScript.DslScriptRegistry;
import us.abstracta.jmeter.javadsl.core.util.DslScript.DslScriptVars;

/**
 * Contains logic to create a script to be contained in a JMeter property (like if conditions).
 *
 * @since 0.27
 */
public class PropertyScriptBuilder<T> {

  protected final String scriptString;
  protected final PropertyScript<T> script;
  protected final Class<? extends PropertyScript<T>> scriptClass;

  public PropertyScriptBuilder(String script) {
    this(script, null, null);
  }

  private PropertyScriptBuilder(String scriptString, PropertyScript<T> script,
      Class<? extends PropertyScript<T>> scriptClass) {
    this.script = script;
    this.scriptString = scriptString;
    this.scriptClass = scriptClass;
  }

  public PropertyScriptBuilder(PropertyScript<T> script) {
    this(null, script, null);
  }

  public PropertyScriptBuilder(Class<? extends PropertyScript<T>> scriptClass) {
    this(null, null, scriptClass);
  }

  public String build() {
    if (scriptString != null) {
      return scriptString;
    } else if (script != null) {
      return buildFunction(String.format("props.get('%s')", DslScriptRegistry.register(script)));
    } else {
      return buildFunction(String.format("new('%s')", scriptClass.getName()));
    }
  }

  private String buildFunction(String script) {
    // when trying with jexl3 got a lot worse performance, so using jexl2
    return JmeterFunction.from("__jexl2",
        script + String.format(".run(new('%s',ctx,log))", PropertyScriptVars.class.getName()));
  }

  /**
   * Allows to use any java code as property.
   *
   * @see PropertyScriptBuilder.PropertyScriptVars for a list of provided variables in script
   * execution
   */
  public interface PropertyScript<T> extends DslScript<PropertyScriptVars, T> {

  }

  public static class PropertyScriptVars extends DslScriptVars {

    public final String threadName;

    public PropertyScriptVars(JMeterContext ctx, Logger log) {
      super(ctx.getPreviousResult(), ctx, ctx.getVariables(), JMeterUtils.getJMeterProperties(),
          ctx.getCurrentSampler(), log);
      this.threadName = Thread.currentThread().getName();
    }

  }

}
