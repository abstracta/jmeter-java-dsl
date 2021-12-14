package us.abstracta.jmeter.javadsl.wrapper.wrappers;

import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveGuiClass;
import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveName;

import java.util.Collections;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup;

/**
 * Is a {@link TestElementWrapper} for JMeter thread groups.
 *
 * @since 0.41
 */
public class DslThreadGroupWrapper extends BaseThreadGroup<DslThreadGroupWrapper> implements
    TestElementWrapper<DslThreadGroupWrapper> {

  private final TestElementWrapperHelper<AbstractThreadGroup> helper;

  public DslThreadGroupWrapper(String name, AbstractThreadGroup testElement,
      AbstractThreadGroupGui guiComponent) {
    super(solveName(name, testElement, guiComponent), solveGuiClass(testElement, guiComponent),
        Collections.emptyList());
    this.helper = new TestElementWrapperHelper<>(testElement, guiComponent);
  }

  @Override
  public DslThreadGroupWrapper children(ThreadGroupChild... children) {
    return super.children(children);
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

}
