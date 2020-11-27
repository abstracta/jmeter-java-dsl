package us.abstracta.jmeter.javadsl.core;

import java.util.List;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jorphan.collections.HashTree;

/**
 * Abstracts logic for {@link DslTestElement} that can nest other test elements.
 *
 * @param <T> is type of test elements that can be nested by this class.
 *
 * Check {@link DslTestPlan} for an example.
 */
public abstract class TestElementContainer<T extends DslTestElement> extends BaseTestElement {

  private final List<T> children;

  protected TestElementContainer(String name, Class<? extends JMeterGUIComponent> guiClass,
      List<T> children) {
    super(name, guiClass);
    this.children = children;
  }

  protected void addChild(T children) {
    this.children.add(children);
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent) {
    HashTree ret = super.buildTreeUnder(parent);
    if (children != null) {
      children.forEach(c -> c.buildTreeUnder(ret));
    }
    return ret;
  }

}
