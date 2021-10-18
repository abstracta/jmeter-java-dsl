package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.List;
import org.apache.jmeter.control.ThroughputController;
import org.apache.jmeter.control.gui.ThroughputControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * Allows running only given percent of times given test plan elements.
 *
 * Internally this uses JMeter Throughput Controller (which has misleading name) with percent
 * executions option.
 *
 * The execution of elements is deterministic, holding execution until percentage is reached. For
 * example, if the percent is 25, the execution of child elements will look like: [skip, skip, skip,
 * run, skip, skip, skip, run, ...].
 *
 * Execution of children is always run as an atomic set (each time/iteration either all or none of
 * the children are run).
 *
 * @since 0.25
 */
public class PercentController extends DslController {

  private final float percent;

  public PercentController(float percent, List<ThreadGroupChild> children) {
    super("Percent Selector Controller", ThroughputControllerGui.class, children);
    this.percent = percent;
  }

  @Override
  protected TestElement buildTestElement() {
    ThroughputController ret = new ThroughputController();
    ret.setStyle(ThroughputController.BYPERCENT);
    ret.setPercentThroughput(percent);
    ret.setPerThread(false);
    return ret;
  }

}
