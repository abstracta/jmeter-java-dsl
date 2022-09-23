package us.abstracta.jmeter.javadsl.parallel;

import com.blazemeter.jmeter.controller.ParallelControllerGui;
import com.blazemeter.jmeter.controller.ParallelSampler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.controllers.BaseController;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Allows grouping requests that need to execute in parallel.
 * <p>
 * This element uses
 * <a href="https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/parallel/Parallel.md">
 * Parallel Controller plugin</a>, check its documentation for more details.
 * <p>
 * By default, this element executes unlimited amount of parallel requests ang generate no
 * additional sample result. Check provided methods to change this behavior.
 *
 * @since 0.30
 */
public class ParallelController extends BaseController<ParallelController> {

  public static final String DEFAULT_NAME = "bzm - Parallel Controller";
  protected boolean generateParent = false;
  protected Integer maxThreads;

  public ParallelController(String name, List<ThreadGroupChild> children) {
    super(name == null ? DEFAULT_NAME : name, ParallelControllerGui.class, children);
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
   * <p>
   * Setting the name of the controller is particularly useful when using
   * {@link #generateParentSample(boolean)} to focus on transaction steps and properly identify
   * associated metrics.
   *
   * @param name     is the label assigned to the Parallel Controller, which will appear in
   *                 collected metrics when {@link #generateParentSample(boolean)} is used.
   * @param children test elements to execute in parallel.
   * @return the controller for further configuration or usage.
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
   * @return the controller for further configuration or usage.
   * @see #parallelController(ThreadGroupChild...)
   * @since 0.30.1
   */
  public static ParallelController parallelController() {
    return parallelController((String) null);
  }

  /**
   * Same as {@link #parallelController(String, ThreadGroupChild...)} but postponing children
   * specification to allow specifying additional settings first.
   *
   * @return the controller for further configuration or usage.
   * @see #parallelController(String, ThreadGroupChild...)
   * @since 0.30.1
   */
  public static ParallelController parallelController(String name) {
    return new ParallelController(name, Collections.emptyList());
  }

  /**
   * Specifies whether to generate a sample result containing children elements results as sub
   * results.
   * <p>
   * Take into consideration that when this option is enabled, then only the parallel controller
   * sample result metrics will appear in metrics like summary results and similar. When generate
   * parent sample is used, consider always using a name for the controller to properly identify it
   * in collected metrics.
   *
   * @return the controller for further configuration or usage.
   * @since 1.0
   */
  public ParallelController generateParentSample() {
    return generateParentSample(true);
  }

  /**
   * Same as {@link #generateParentSample()} but allowing to enable and disable the setting.
   * <p>
   * This is helpful when the resolution is taken at runtime.
   *
   * @param enable specifies to enable or disable the setting. By default, it is set to false.
   * @return the controller for further configuration or usage.
   * @see #generateParentSample()
   */
  public ParallelController generateParentSample(boolean enable) {
    this.generateParent = enable;
    return this;
  }

  /**
   * Allows limiting the number of threads used to execute children elements in parallel.
   *
   * @param maxThreads number of threads to use. When not specified, no limit is set.
   * @return the controller for further configuration or usage.
   */
  public ParallelController maxThreads(int maxThreads) {
    this.maxThreads = maxThreads;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    ParallelSampler ret = new ParallelSampler();
    ret.setGenerateParent(generateParent);
    if (maxThreads != null) {
      ret.setMaxThreadNumber(maxThreads);
      ret.setLimitMaxThreadNumber(true);
    }
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<ParallelSampler> {

    public CodeBuilder(List<Method> builderMethods) {
      super(ParallelSampler.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(ParallelSampler testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodCall ret = buildMethodCall(paramBuilder.nameParam(DEFAULT_NAME),
          new ChildrenParam<>(ThreadGroupChild[].class))
          .chain("generateParentSample", paramBuilder.boolParam("PARENT_SAMPLE", false));
      if (!paramBuilder.boolParam("LIMIT_MAX_THREAD_NUMBER", false).isDefault()) {
        ret.chain("maxThreads", paramBuilder.intParam("MAX_THREAD_NUMBER"));
      }
      return ret;
    }

  }

}
