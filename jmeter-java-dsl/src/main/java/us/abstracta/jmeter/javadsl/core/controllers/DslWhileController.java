package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.List;
import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.control.gui.WhileControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.DslScriptBuilder;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.PropertyScriptBuilder;
import us.abstracta.jmeter.javadsl.core.PropertyScriptBuilder.PropertyScript;

/**
 * Allows running part of a test plan until a condition is met.
 *
 * The condition is evaluated in each iteration before and after all children elements are executed.
 * Keep this in mind in case you use conditions with side effects (like incrementing counters).
 *
 * JMeter automatically creates a variable named {@code __jm__<controllerName>__idx} which contains
 * the index of the iteration starting with zero.
 *
 * @since 0.27
 */
public class DslWhileController extends DslController {

  private final DslScriptBuilder conditionBuilder;

  public DslWhileController(String name, String condition, List<ThreadGroupChild> children) {
    super(solveName(name), WhileControllerGui.class, children);
    this.conditionBuilder = new PropertyScriptBuilder(condition);
  }

  public DslWhileController(String name, PropertyScript script, List<ThreadGroupChild> children) {
    super(solveName(name), WhileControllerGui.class, children);
    this.conditionBuilder = new PropertyScriptBuilder(script);
  }

  private static String solveName(String name) {
    return name != null ? name : "while";
  }

  @Override
  protected TestElement buildTestElement() {
    WhileController ret = new WhileController();
    String condition = conditionBuilder.build();
    ret.setCondition(condition);
    return ret;
  }

}
