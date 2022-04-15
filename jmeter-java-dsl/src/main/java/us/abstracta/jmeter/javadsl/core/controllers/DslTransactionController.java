package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.gui.TransactionControllerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.ChildrenParam;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.StringParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Allows specifying JMeter transaction controllers which group different samples associated to same
 * transaction.
 * <p>
 * This is usually used when grouping different steps of a flow, for example group requests of login
 * flow, adding item to cart, purchase, etc. It provides aggregate metrics of all it's samples.
 *
 * @since 0.14
 */
public class DslTransactionController extends BaseController {

  private boolean includeTimers = false;
  private boolean generateParentSample = false;

  public DslTransactionController(String name, List<ThreadGroupChild> children) {
    super(name, TransactionControllerGui.class, children);
  }

  /**
   * Allows enabling/disabling inclusion of time spent in timers and pre- and post-processors in
   * sample results.
   *
   * @param includeTimers specifies, when set to true, to include timers and pre- and
   *                      post-processors time in sample results. By default, timers and pre- and
   *                      post-processors are not included in transaction sample results times.
   * @return the Transaction Controller for further configuration or usage.
   * @since 0.29
   */
  public DslTransactionController includeTimersAndProcessorsTime(boolean includeTimers) {
    this.includeTimers = includeTimers;
    return this;
  }

  /**
   * Allows enabling/disabling creation of sample result as parent of children samplers.
   * <p>
   * It is useful in some scenarios to get transaction sample results as parent of children samplers
   * to focus mainly in transactions and not be concerned about each particular request. Enabling
   * parent sampler helps in this regard, only reporting the transactions in summary reports, and
   * not the transaction children results.
   *
   * @param generateParentSample specifies to generate parent sample when true, or disable it. By
   *                             default parent sample generation is disabled.
   * @return the Transaction Controller for further configuration or usage.
   * @since 0.29
   */
  public DslTransactionController generateParentSample(boolean generateParentSample) {
    this.generateParentSample = generateParentSample;
    return this;
  }

  /**
   * Allows specifying transaction children elements (samplers, listeners, post processors, etc.).
   * <p>
   * This method is just an alternative to the constructor specification of children, and is handy
   * when you want to keep general transactions settings together and then specify children (instead
   * of specifying name and children, and at the end alternative settings like generating parent
   * sample).
   *
   * @param children list of test elements to add as children of the transaction.
   * @return the altered transaction.
   * @since 0.29
   */
  public DslTransactionController children(ThreadGroupChild... children) {
    super.addChildren(children);
    return this;
  }

  @Override
  public TestElement buildTestElement() {
    TransactionController ret = new TransactionController();
    ret.setGenerateParentSample(generateParentSample);
    // we can't use setIncludeTimers since it ignores true values
    ret.setProperty("TransactionController.includeTimers", includeTimers);
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<TransactionController> {

    public CodeBuilder(List<Method> builderMethods) {
      super(TransactionController.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(TransactionController testElement,
        MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement,
          "TransactionController");
      StringParam name = paramBuilder.nameParam(null);
      BoolParam parent = paramBuilder.boolParam("parent", false);
      BoolParam timers = paramBuilder.boolParam("includeTimers", false);
      if (parent.isDefault() && timers.isDefault()) {
        return buildMethodCall(name, new ChildrenParam<>(ThreadGroupChild[].class));
      } else {
        return buildMethodCall(name)
            .chain("generateParentSample", parent)
            .chain("includeTimersAndProcessorsTime", timers);
      }
    }

  }

}
