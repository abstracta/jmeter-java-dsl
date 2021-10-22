package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.jmeter.control.IfController;
import org.apache.jmeter.control.gui.IfControllerPanel;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import us.abstracta.jmeter.javadsl.core.DslScriptBuilder;
import us.abstracta.jmeter.javadsl.core.DslScriptBuilder.DslScript;
import us.abstracta.jmeter.javadsl.core.DslScriptBuilder.DslScriptVars;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * Allows to conditionally run part of a test plan according to certain condition.
 *
 * @since 0.27
 */
public class DslIfController extends DslController {

  private final DslScriptBuilder conditionBuilder;

  public DslIfController(String condition, List<ThreadGroupChild> children) {
    super("If Controller", IfControllerPanel.class, children);
    this.conditionBuilder = new DslScriptBuilder(condition);
  }

  public DslIfController(ConditionScript script, List<ThreadGroupChild> children) {
    super("If Controller", IfControllerPanel.class, children);
    this.conditionBuilder = new DslScriptBuilder(script, ConditionVars.class, buildVarsMapping());
  }

  private Map<String, String> buildVarsMapping() {
    // These bindings are required because groovy script may or may not have the variables defined
    HashMap<String, String> ret = new HashMap<>();
    addOptionalVarMapping("prev", ret);
    addOptionalVarMapping("sampler", ret);
    return ret;
  }

  private void addOptionalVarMapping(String varName, HashMap<String, String> ret) {
    ret.put(varName, "binding.hasVariable('" + varName + "') ? " + varName + " : null");
  }

  @Override
  protected TestElement buildTestElement() {
    IfController ret = new IfController();
    ret.setUseExpression(true);
    String condition = conditionBuilder.buildAsProperty();
    ret.setCondition(condition);
    return ret;
  }

  /**
   * Allows to use any java code as condition.
   *
   * @see DslIfController.ConditionVars for a list of provided variables in script execution
   */
  public interface ConditionScript extends DslScript<ConditionVars, Boolean> {

  }

  public static class ConditionVars extends DslScriptVars {

    public final String threadName;

    public ConditionVars(String threadName, SampleResult prev, JMeterContext ctx,
        JMeterVariables vars, Properties props, Sampler sampler, Logger log) {
      super(prev, ctx, vars, props, sampler, log);
      this.threadName = threadName;
    }

  }

}
