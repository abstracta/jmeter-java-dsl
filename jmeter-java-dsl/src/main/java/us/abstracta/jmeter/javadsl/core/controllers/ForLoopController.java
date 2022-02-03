package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.List;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Allows running part of a test plan a given number of times inside one thread group iteration.
 *
 * Internally this uses JMeter Loop Controller.
 *
 * JMeter automatically creates a variable named {@code __jm__<controllerName>__idx} which contains
 * the index of the iteration starting with zero.
 *
 * @since 0.27
 */
public class ForLoopController extends BaseController {

  private final String count;

  public ForLoopController(String name, String count, List<ThreadGroupChild> children) {
    super(name != null ? name : "for", LoopControlPanel.class, children);
    this.count = count;
  }

  @Override
  protected TestElement buildTestElement() {
    LoopController ret = new LoopController();
    ret.setLoops(count);
    return ret;
  }

}
