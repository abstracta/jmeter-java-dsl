package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.control.ThroughputController;
import org.apache.jmeter.control.gui.ThroughputControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Allows running only given percent of times given test plan elements.
 * <p>
 * Internally this uses JMeter Throughput Controller (which has misleading name) with percent
 * executions option.
 * <p>
 * The execution of elements is deterministic, holding execution until percentage is reached. For
 * example, if the percent is 25, the execution of child elements will look like: [skip, skip, skip,
 * run, skip, skip, skip, run, ...].
 * <p>
 * Execution of children is always run as an atomic set (each time/iteration either all or none of
 * the children are run).
 *
 * @since 0.25
 */
public class PercentController extends BaseController {

  private final String percent;
  private boolean perThread;

  public PercentController(String percent, List<ThreadGroupChild> children) {
    super("Percent Selector Controller", ThroughputControllerGui.class, children);
    this.percent = percent;
  }

  /**
   * Specifies if percent control should be applied per thread or shared by all threads.
   * <p>
   * This might be useful when a more deterministic behavior is required per thread, and each thread
   * execution has to be controlled, instead of relying on in general threads execution percentage.
   *
   * @param perThread specifies, when true, to applies percent control per thread. Otherwise, it
   *                  controls percent execution across all threads. When not specified, the latter
   *                  is used.
   * @return the percent controller for further configuration or usage.
   * @since 0.63
   */
  public PercentController perThread(boolean perThread) {
    this.perThread = perThread;
    return this;
  }

  /**
   * Allows specifying controller children elements (samplers, listeners, post processors, etc.).
   * <p>
   * This method is just an alternative to the constructor specification of children, and is handy
   * when you want to keep general controller settings together and then specify children (instead
   * of specifying percent and children, and at the end alternative settings like per thread
   * percentage control).
   *
   * @param children list of test elements to add as children of the transaction.
   * @return the altered transaction.
   * @since 0.63
   */
  public PercentController children(ThreadGroupChild... children) {
    super.addChildren(children);
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    ThroughputController ret = new ThroughputController();
    ret.setStyle(ThroughputController.BYPERCENT);
    ret.setPercentThroughput(percent);
    ret.setPerThread(perThread);
    return ret;
  }

  public static class CodeBuilder extends MethodCallBuilder {

    public CodeBuilder(List<Method> builderMethods) {
      super(builderMethods);
    }

    @Override
    public boolean matches(MethodCallContext context) {
      if (!(context.getTestElement() instanceof ThroughputController)) {
        return false;
      }
      ThroughputController controller = (ThroughputController) context.getTestElement();
      return controller.getStyle() == ThroughputController.BYPERCENT;
    }

    @Override
    protected MethodCall buildMethodCall(MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(context.getTestElement(),
          "ThroughputController");
      MethodCall ret = buildMethodCall(paramBuilder.floatParam("percentThroughput"),
          new ChildrenParam<>(ThreadGroupChild[].class));
      ret.chain("perThread", paramBuilder.boolParam("perThread", false));
      return ret;
    }

  }

}
