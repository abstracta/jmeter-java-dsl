package us.abstracta.jmeter.javadsl.core;

import java.util.Arrays;
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

  private List<? extends T> children;

  protected TestElementContainer(String name, Class<? extends JMeterGUIComponent> guiClass,
      List<? extends T> children) {
    super(name, guiClass);
    this.children = children;
  }

  @SafeVarargs
  protected final void setChildren(T... children) {
    this.children = Arrays.asList(children);
  }

  protected final void setChildren(List<? extends T> children) {
    this.children = children;
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
