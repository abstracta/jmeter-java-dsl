package us.abstracta.jmeter.javadsl.core;

import java.util.ArrayList;
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

  private final List<T> children = new ArrayList<>();

  protected TestElementContainer(String name, Class<? extends JMeterGUIComponent> guiClass,
      List<T> children) {
    super(name, guiClass);
    this.children.addAll(children);
  }

  /**
   * Allows specifying children test elements for the sampler, which allow for example extracting
   * information from HTTP response, alter HTTP request, assert HTTP response contents, etc.
   *
   * @param children list of test elements to add as children of this sampler.
   * @return the altered sampler to allow for fluent API usage.
   */
  protected TestElementContainer<T> children(T... children) {
    this.children.addAll(Arrays.asList(children));
    return this;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    HashTree ret = super.buildTreeUnder(parent, context);
    children.forEach(c -> c.buildTreeUnder(ret, context));
    return ret;
  }

}
