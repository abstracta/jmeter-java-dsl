package us.abstracta.jmeter.javadsl.core.testelements;

import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;

/**
 * Is a TestElement which should only be added as Test plan child, and only the first instance is
 * take into consideration.
 *
 * Elements of this instance are automatically added by other test plan elements. To avoid such
 * behavior, the instance can be disabled by setting its enabled attribute to false.
 *
 * Check subclasses for examples of its usage.
 *
 * @since 0.17
 */
public abstract class TestPlanSingletonChildElement extends BaseTestElement implements
    TestPlanChild {

  protected boolean enabled = true;

  protected TestPlanSingletonChildElement(String name,
      Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass);
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    String contextEntry = getClass().getSimpleName();
    Object entry = context.getEntry(contextEntry);
    if (entry == null) {
      context.setEntry(contextEntry, this);
      if (enabled) {
        super.buildTreeUnder(context.getTestPlanTree(), context);
      }
    }
    return parent;
  }

}
