package us.abstracta.jmeter.javadsl.core;

import java.util.List;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * This class represents the standard thread group test element included by JMeter.
 *
 * Additional methods should be added in the future to support setting rump-up, start and end
 * scheduling.
 */
public class DslThreadGroup extends TestElementContainer<ThreadGroupChild> implements
    TestPlanChild {

  private final int threads;
  private final int iterations;

  public DslThreadGroup(String name, int threads, int iterations,
      List<? extends ThreadGroupChild> children) {
    super(name, children);
    this.threads = threads;
    this.iterations = iterations;
  }

  @Override
  public TestElement buildTestElement() {
    ThreadGroup ret = new ThreadGroup();
    ret.setNumThreads(threads);
    LoopController loopController = new LoopController();
    loopController.setLoops(iterations);
    ret.setSamplerController(loopController);
    return ret;
  }

  /**
   * Test elements that can be added as direct children of a thread group in jmeter, should
   * implement this interface.
   */
  public interface ThreadGroupChild extends DslTestElement {

  }

}
