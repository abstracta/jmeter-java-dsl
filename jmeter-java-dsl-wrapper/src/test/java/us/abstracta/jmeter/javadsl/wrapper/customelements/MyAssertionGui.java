package us.abstracta.jmeter.javadsl.wrapper.customelements;

import org.apache.jmeter.assertions.gui.AbstractAssertionGui;
import org.apache.jmeter.testelement.TestElement;

public class MyAssertionGui extends AbstractAssertionGui {

  @Override
  public String getLabelResource() {
    return null;
  }

  @Override
  public TestElement createTestElement() {
    MyAssertion ret = new MyAssertion();
    modifyTestElement(ret);
    return ret;
  }

  @Override
  public void modifyTestElement(TestElement element) {
    configureTestElement(element);
  }

}
