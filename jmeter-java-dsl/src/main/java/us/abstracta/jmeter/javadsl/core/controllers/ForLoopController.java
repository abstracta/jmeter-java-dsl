package us.abstracta.jmeter.javadsl.core.controllers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Allows running part of a test plan a given number of times inside one thread group iteration.
 * <p>
 * Internally this uses JMeter Loop Controller.
 * <p>
 * JMeter automatically creates a variable named {@code __jm__<controllerName>__idx} which contains
 * the index of the iteration starting with zero.
 *
 * @since 0.27
 */
public class ForLoopController extends BaseController<ForLoopController> {

  protected String count;

  public ForLoopController(String name, String count, List<ThreadGroupChild> children) {
    super(name != null ? name : "for", LoopControlPanel.class, children);
    this.count = count;
  }

  @Override
  protected TestElement buildTestElement() {
    LoopController ret = new LoopController();
    ret.setLoops(count);
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<LoopController> {

    public CodeBuilder(List<Method> builderMethods) {
      super(LoopController.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(LoopController testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement,
          "LoopController");
      return buildMethodCall(paramBuilder.nameParam(null), paramBuilder.intParam("loops"),
          new ChildrenParam<>(ThreadGroupChild[].class));
    }

  }

}
