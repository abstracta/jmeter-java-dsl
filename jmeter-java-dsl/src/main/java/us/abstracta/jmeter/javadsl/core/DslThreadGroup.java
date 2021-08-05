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
 * <p>
 * Additional methods should be added in the future to support setting rump-up, start and end
 * scheduling.
 */
public class DslThreadGroup extends TestElementContainer<ThreadGroupChild> implements
    TestPlanChild {

  private final int threads;
  private final int iterations;
  private final Duration duration;
  private Duration rampUpPeriod = Duration.ZERO;

  public DslThreadGroup(String name, int threads, int iterations, List<ThreadGroupChild> children) {
    super(name != null ? name : "Thread Group", ThreadGroupGui.class, children);
    this.threads = threads;
    this.iterations = iterations;
    this.duration = null;
  }

  public DslThreadGroup(String name, int threads, Duration duration,
      List<ThreadGroupChild> children) {
    super(name != null ? name : "Thread Group", ThreadGroupGui.class, children);
    this.threads = threads;
    this.iterations = 0;
    this.duration = duration;
  }

  /**
   * Specifies the time taken to create the given number of threads.
   *
   * It is usually advised to set this property when working with considerable amount of threads to
   * avoid initial load of creating the threads to affect test results.
   *
   * JMeter will create a new thread every {@code rampUp.seconds * 1000 / threadCount} milliseconds.
   *
   * If you specify a thread duration time (instead of iterations), take into consideration that
   * ramp up is not considered as part of thread duration time. For example: if you have a thread
   * group duration of 10 seconds, and a ramp-up of 10 seconds, the last threads (and the test plan
   * run) will run at least (duration may vary depending on test plan contents) after 20 seconds of
   * starting the test.
   *
   * <b>Warning:</b> JMeter currently has <a href="https://bz.apache.org/bugzilla/show_bug.cgi?id=65031">a
   * bug</a> that causes the ramp-up to not exactly match above statement. In fact, if you have X
   * threads and specify a ramp-up of Y seconds, expect all threads to be started at Y - Y/X,
   * instead of Y.
   *
   * @param rampUpPeriod the period to use as ramp up. Since JMeter supports specifying ramp up in
   * seconds, if you specify a smaller granularity (like milliseconds) it will be rounded up to
   * seconds.
   */
  public DslThreadGroup rampUpPeriod(Duration rampUpPeriod) {
    this.rampUpPeriod = rampUpPeriod;
    return this;
  }

  /**
   * Allows specifying children the thread group (samplers, listeners, post processors, etc.).
   *
   * This method is just an alternative to the constructor specification of children, and is handy
   * when you want to keep general thread group settings together and then specify children (instead
   * of specifying threadCount &amp; duration/iterations, then children, and at the end alternative
   * settings like ramp-up period).
   *
   * @param children list of test elements to add as children of the thread group.
   * @return the altered thread group to allow for fluent API usage.
   */
  public DslThreadGroup children(ThreadGroupChild... children) {
    return (DslThreadGroup) super.children(children);
  }

  @Override
  public TestElement buildTestElement() {
    ThreadGroup ret = new ThreadGroup();
    ret.setNumThreads(threads);
    ret.setRampUp((int) extractDurationSeconds(rampUpPeriod));
    LoopController loopController = new LoopController();
    ret.setSamplerController(loopController);
    if (duration != null) {
      loopController.setLoops(-1);
      ret.setScheduler(true);
      ret.setDuration(extractDurationSeconds(duration));
    } else {
      loopController.setLoops(iterations);
    }
    return ret;
  }

  private long extractDurationSeconds(Duration duration) {
    return Math.round(Math.ceil((double) duration.toMillis() / 1000));
  }

  /**
   * Test elements that can be added as direct children of a thread group in jmeter should implement
   * this interface.
   */
  public interface ThreadGroupChild extends DslTestElement {

  }

}
