package us.abstracta.jmeter.javadsl.parallel;

import com.blazemeter.jmeter.controller.ParallelControllerGui;
import com.blazemeter.jmeter.controller.ParallelSampler;
import java.util.Arrays;
import java.util.List;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.controllers.DslController;

/**
 * Allows grouping requests that need to execute in parallel.
 *
 * This element uses
 * <a href="https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/parallel/Parallel.md">
 * Parallel Controller plugin</a>, check its documentation for more details.
 *
 * By default, this element executes up to 6 parallel requests ang generate no additional sample
 * result. Check provided methods to change this behavior.
 *
 * @since 0.30
 */
public class ParallelController extends DslController {

  private boolean generateParent = false;
  private int maxThreadsCount = 6;

  public ParallelController(String name, List<ThreadGroupChild> children) {
    super(name == null ? "bzm - Parallel Controller" : name, ParallelControllerGui.class, children);
  }

  /**
   * Builds a Parallel Controller executing the given children elements in parallel.
   *
   * @param children test elements to execute in parallel.
   * @return the Parallel Controller for additional configuration and usage.
   */
  public static ParallelController parallelController(ThreadGroupChild... children) {
    return new ParallelController(null, Arrays.asList(children));
  }

  /**
   * Same as {@link #parallelController(ThreadGroupChild...)} but allowing to set a name on
   * controller.
   *
   * Setting the name of the controller is particularly useful when using {@link
   * #generateParentSample(boolean)} to focus on transaction steps and properly identify associated
   * metrics.
   *
   * @param name is the label assigned to the Parallel Controller, which will appear in collected
   * metrics when {@link #generateParentSample(boolean)} is used.
   * @param children test elements to execute in parallel.
   * @return the Parallel Controller for additional configuration and usage.
   * @see #parallelController(ThreadGroupChild...)
   */
  public static ParallelController parallelController(String name,
      ThreadGroupChild... children) {
    return new ParallelController(name, Arrays.asList(children));
  }

  /**
   * Specifies whether or not to generate a sample result containing children elements results as
   * sub results.
   *
   * Take into consideration that when this option is enabled, then only the parallel controller
   * sample result metrics will appear in metrics like summary results and similar. When generate
   * parent sample is used, consider always using a name for the controller to properly identify it
   * in collected metrics.
   *
   * @param generateParent when set to true a sample result containing children elements results as
   * sub results will be generated. This
   * @return the Parallel Controller for additional configuration and usage.
   */
  public ParallelController generateParentSample(boolean generateParent) {
    this.generateParent = generateParent;
    return this;
  }

  /**
   * Allows specifying how many threads should be used to execute the children elements in
   * parallel.
   *
   * @param maxThreadsCount number of threads to use. When not specified, default value is 6.
   * @return the Parallel Controller for additional configuration and usage.
   */
  public ParallelController maxThreadsCount(int maxThreadsCount) {
    this.maxThreadsCount = maxThreadsCount;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    ParallelSampler ret = new ParallelSampler();
    ret.setGenerateParent(generateParent);
    ret.setMaxThreadNumber(maxThreadsCount);
    return ret;
  }

}
