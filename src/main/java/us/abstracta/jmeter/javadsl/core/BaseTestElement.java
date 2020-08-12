package us.abstracta.jmeter.javadsl.core;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

/**
 * This class provides the basic logic for all {@link DslTestElement}.
 *
 * In particular it currently allows to set the name of the TestElement and abstracts building of
 * the tree only requiring, from sub classes, to implement the logic to build the JMeter
 * TestElement. The test element name is particularly useful for later reporting and statistics
 * collection to differentiate metrics for each test element.
 *
 * Sub classes may overwrite {@link #buildTreeUnder} if they need additional logic (e.g: {@link
 * TestElementContainer}).
 */
public abstract class BaseTestElement implements DslTestElement {

  private final String name;

  protected BaseTestElement(String name) {
    this.name = name;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent) {
    TestElement testElement = buildTestElement();
    testElement.setName(name);
    return parent.add(testElement);
  }

  protected abstract TestElement buildTestElement();

}
