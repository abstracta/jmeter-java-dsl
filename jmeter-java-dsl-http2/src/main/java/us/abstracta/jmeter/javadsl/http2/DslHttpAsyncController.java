package us.abstracta.jmeter.javadsl.http2;

import com.blazemeter.jmeter.http2.control.HTTP2Controller;
import com.blazemeter.jmeter.http2.control.gui.HTTP2ControllerGUI;
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
 * Allows grouping BlazeMeter HTTP samplers that need to execute concurrently within a thread
 * iteration.
 * <p>
 * This element uses the
 * <a href="https://github.com/Blazemeter/jmeter-http2-plugin">BlazeMeter HTTP plugin</a>
 * {@code bzm - HTTP Async Controller}. Check the plugin documentation for more details.
 * <p>
 * By default, this element does not generate a parent sample result. Check provided methods to
 * change this behavior.
 *
 * @since 2.3
 */
public class DslHttpAsyncController extends BaseController<DslHttpAsyncController> {

  public static final String DEFAULT_NAME = "bzm - HTTP Async Controller";
  private static final String GENERATE_PARENT_SAMPLE_PROPERTY =
      "blazemeter.http.controller.generateParentSample";
  private static final String LIMIT_MAX_PARALLEL_PROPERTY =
      "blazemeter.http.controller.limitMaxParallel";
  private static final String MAX_CONCURRENT_PROPERTY =
      "blazemeter.http.controller.maxConcurrentAsyncInController";
  private static final String INCLUDE_TIMERS_PROPERTY = "TransactionController.includeTimers";

  protected boolean generateParentSample;
  protected boolean limitMaxParallel;
  protected Integer maxParallelExecutions;
  protected boolean includeTimers;

  public DslHttpAsyncController(String name, List<ThreadGroupChild> children) {
    super(name == null ? DEFAULT_NAME : name, HTTP2ControllerGUI.class, children);
  }

  /**
   * Builds an HTTP Async Controller executing the given children elements concurrently.
   *
   * @param children test elements to execute concurrently.
   * @return the controller for further configuration or usage.
   */
  public static DslHttpAsyncController httpAsyncController(ThreadGroupChild... children) {
    return httpAsyncController(null, children);
  }

  /**
   * Same as {@link #httpAsyncController(ThreadGroupChild...)} but allowing to set a name on the
   * controller.
   *
   * @param name     is the label assigned to the controller in collected metrics when
   *                 {@link #generateParentSample()} is used.
   * @param children test elements to execute concurrently.
   * @return the controller for further configuration or usage.
   */
  public static DslHttpAsyncController httpAsyncController(String name,
      ThreadGroupChild... children) {
    return new DslHttpAsyncController(name, Arrays.asList(children));
  }

  /**
   * Same as {@link #httpAsyncController(ThreadGroupChild...)} but postponing children specification
   * to allow specifying additional settings first.
   *
   * @return the controller for further configuration or usage.
   */
  public static DslHttpAsyncController httpAsyncController() {
    return httpAsyncController((String) null);
  }

  /**
   * Same as {@link #httpAsyncController(String, ThreadGroupChild...)} but postponing children
   * specification to allow specifying additional settings first.
   *
   * @return the controller for further configuration or usage.
   */
  public static DslHttpAsyncController httpAsyncController(String name) {
    return new DslHttpAsyncController(name, Collections.emptyList());
  }

  /**
   * Specifies whether to generate a sample result containing children elements results as sub
   * results.
   *
   * @return the controller for further configuration or usage.
   */
  public DslHttpAsyncController generateParentSample() {
    return generateParentSample(true);
  }

  /**
   * Same as {@link #generateParentSample()} but allowing to enable and disable the setting.
   *
   * @param enable specifies to enable or disable the setting. By default, it is set to false.
   * @return the controller for further configuration or usage.
   */
  public DslHttpAsyncController generateParentSample(boolean enable) {
    this.generateParentSample = enable;
    return this;
  }

  /**
   * Enables limiting the number of concurrent BlazeMeter HTTP sampler executions.
   *
   * @param maxParallelExecutions maximum number of concurrent executions. Must be at least 1.
   * @return the controller for further configuration or usage.
   */
  public DslHttpAsyncController maxParallelExecutions(int maxParallelExecutions) {
    this.limitMaxParallel = true;
    this.maxParallelExecutions = maxParallelExecutions;
    return this;
  }

  /**
   * Specifies whether to include timer and pre/post-processor duration in the generated parent
   * sample. Only applies when {@link #generateParentSample()} is enabled.
   *
   * @param enable when {@code true}, includes timer and pre/post-processor time in parent sample.
   * @return the controller for further configuration or usage.
   */
  public DslHttpAsyncController includeTimers(boolean enable) {
    this.includeTimers = enable;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    HTTP2Controller ret = new HTTP2Controller();
    ret.setGenerateParentSample(generateParentSample);
    ret.setLimitMaxParallel(limitMaxParallel);
    if (maxParallelExecutions != null) {
      ret.setMaxConcurrentAsyncInController(maxParallelExecutions);
    }
    ret.setIncludeTimers(includeTimers);
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<HTTP2Controller> {

    public CodeBuilder(List<Method> builderMethods) {
      super(HTTP2Controller.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(HTTP2Controller testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodCall ret = buildMethodCall(paramBuilder.nameParam(DEFAULT_NAME),
          new ChildrenParam<>(ThreadGroupChild[].class))
          .chain("generateParentSample",
              paramBuilder.boolParam(GENERATE_PARENT_SAMPLE_PROPERTY, false));
      if (!paramBuilder.boolParam(LIMIT_MAX_PARALLEL_PROPERTY, false).isDefault()) {
        ret.chain("maxParallelExecutions", paramBuilder.intParam(MAX_CONCURRENT_PROPERTY));
      }
      ret.chain("includeTimers", paramBuilder.boolParam(INCLUDE_TIMERS_PROPERTY, false));
      return ret;
    }
  }

}
