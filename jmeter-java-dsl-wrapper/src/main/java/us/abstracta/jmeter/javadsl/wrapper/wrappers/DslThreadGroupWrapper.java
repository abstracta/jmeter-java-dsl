package us.abstracta.jmeter.javadsl.wrapper.wrappers;

import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveGuiClass;
import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveName;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;

/**
 * Is a {@link TestElementWrapper} for JMeter thread groups.
 *
 * @since 0.41
 */
public class DslThreadGroupWrapper extends BaseThreadGroup<DslThreadGroupWrapper> implements
    TestElementWrapper<DslThreadGroupWrapper> {

  protected final TestElementWrapperHelper<AbstractThreadGroup> helper;

  public DslThreadGroupWrapper(String name, AbstractThreadGroup testElement,
      AbstractThreadGroupGui guiComponent) {
    super(solveName(name, testElement, guiComponent), solveGuiClass(testElement, guiComponent),
        Collections.emptyList());
    this.helper = new TestElementWrapperHelper<>(testElement, guiComponent);
  }

  @Override
  public DslThreadGroupWrapper prop(String name, Object value) {
    helper.prop(name, value);
    return this;
  }

  @Override
  protected AbstractThreadGroup buildThreadGroup() {
    return helper.buildTestElement();
  }

  public static class CodeBuilder extends TestElementWrapperCallBuilder<AbstractThreadGroup> {

    public CodeBuilder(List<Method> builderMethods) {
      super(AbstractThreadGroup.class, AbstractThreadGroupGui.class, builderMethods);
      ignoredProperties.add(AbstractThreadGroup.ON_SAMPLE_ERROR);
    }

    @Override
    protected MethodCall buildMethodCall(AbstractThreadGroup testElement,
        MethodCallContext context) {
      MethodCall ret = super.buildMethodCall(testElement, context);
      ret.chain("sampleErrorAction",
          new TestElementParamBuilder(testElement).enumParam(AbstractThreadGroup.ON_SAMPLE_ERROR,
              SampleErrorAction.CONTINUE));
      return ret;
    }

  }

}
