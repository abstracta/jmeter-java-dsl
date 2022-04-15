package us.abstracta.jmeter.javadsl.parallel;

import com.blazemeter.jmeter.controller.ParallelControllerGui;
import com.blazemeter.jmeter.controller.ParallelSampler;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.controllers.BaseController;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Allows grouping requests that need to execute in parallel.
 *
 * This element uses
 * <a href="https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/parallel/Parallel.md">
 * Parallel Controller plugin</a>, check its documentation for more details.
 *
 * By default, this element executes unlimited amount of parallel requests ang generate no
 * additional sample result. Check provided methods to change this behavior.
 *
 * @since 0.30
 */
public class ParallelController extends BaseController {

  private boolean generateParent = false;
  private Integer maxThreads;

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
   * Same as {@link #parallelController(ThreadGroupChild...)} but postponing children specification
   * to allow specifying additional settings first.
   *
   * @return he Parallel Controller for additional configuration and usage.
   * @see #parallelController(ThreadGroupChild...)
   * @since 0.30.1
   */
  public static ParallelController parallelController() {
    return parallelController(new ParallelController(null, Collections.emptyList()));
  }

  /**
   * Same as {@link #parallelController(String, ThreadGroupChild...)} but postponing children
   * specification to allow specifying additional settings first.
   *
   * @return he Parallel Controller for additional configuration and usage.
   * @see #parallelController(String, ThreadGroupChild...)
   * @since 0.30.1
   */
  public static ParallelController parallelController(String name) {
    return new ParallelController(name, Collections.emptyList());
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
   * Allows limiting the number of threads used to execute children elements in parallel.
   *
   * @param maxThreads number of threads to use. When not specified, no limit is set.
   * @return the Parallel Controller for additional configuration and usage.
   */
  public ParallelController maxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
    return this;
  }

  /**
   * Allows specifying controller children elements (samplers, listeners, post processors, etc.).
   *
   * This method is just an alternative to the constructor specification of children, and is handy
   * when you want to specify controller settings and then specify children.
   *
   * @param children list of test elements to add as children of the controller.
   * @return he Parallel Controller for additional configuration and usage.
   * @since 0.30.1
   */
  public ParallelController children(ThreadGroupChild... children) {
    addChildren(children);
    return this;
  }

  @Override
  public TestElement buildTestElement() {
    ParallelSampler ret = new ParallelSampler();
    ret.setGenerateParent(generateParent);
    if (maxThreads != null) {
      ret.setMaxThreadNumber(maxThreads);
      ret.setLimitMaxThreadNumber(true);
    }
    return ret;
  }

}
