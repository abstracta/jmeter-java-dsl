package us.abstracta.jmeter.javadsl.core;

import java.time.Duration;
import java.util.List;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * Represents the standard thread group test element included by JMeter.
 *
 * Additional methods should be added in the future to support setting rump-up, start and end
 * scheduling.
 */
public class DslThreadGroup extends TestElementContainer<ThreadGroupChild> implements
    TestPlanChild {

  private final int threads;
  private final int iterations;
  private final Duration duration;

  public DslThreadGroup(String name, int threads, int iterations,
      List<? extends ThreadGroupChild> children) {
    super(name != null ? name : "Thread Group", ThreadGroupGui.class, children);
    this.threads = threads;
    this.iterations = iterations;
    this.duration = null;
  }

  public DslThreadGroup(String name, int threads, Duration duration,
      List<? extends ThreadGroupChild> children) {
    super(name != null ? name : "Thread Group", ThreadGroupGui.class, children);
    this.threads = threads;
    this.iterations = 0;
    this.duration = duration;
  }

  @Override
  public TestElement buildTestElement() {
    ThreadGroup ret = new ThreadGroup();
    ret.setNumThreads(threads);
    LoopController loopController = new LoopController();
    ret.setSamplerController(loopController);
    if (duration != null) {
      loopController.setLoops(-1);
      ret.setScheduler(true);
      ret.setDuration(Math.round(Math.ceil((double) duration.toMillis() / 1000)));
    } else {
      loopController.setLoops(iterations);
    }
    return ret;
  }

  /**
   * Test elements that can be added as direct children of a thread group in jmeter should implement
   * this interface.
   */
  public interface ThreadGroupChild extends DslTestElement {

  }

}
