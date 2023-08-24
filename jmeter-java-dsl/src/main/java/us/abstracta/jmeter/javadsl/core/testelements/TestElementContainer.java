package us.abstracta.jmeter.javadsl.core.testelements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

/**
 * Abstracts logic for {@link DslTestElement} that can nest other test elements.
 *
 * @param <C> is type of test elements that can be nested by this class.
 *            <p>
 *            Check {@link DslTestPlan} for an example.
 * @since 0.1
 */
public abstract class TestElementContainer<T extends TestElementContainer<T, C>,
    C extends DslTestElement> extends BaseTestElement {

  protected final List<C> children = new ArrayList<>();

  protected TestElementContainer(String name, Class<? extends JMeterGUIComponent> guiClass,
      List<C> children) {
    super(name, guiClass);
    this.children.addAll(children);
  }

  @SuppressWarnings("unchecked")
  protected T children(C... children) {
    this.children.addAll(Arrays.asList(children));
    return (T) this;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    HashTree ret = super.buildTreeUnder(parent, context);
    children.forEach(c -> context.buildChild(c, ret));
    return ret;
  }

}
