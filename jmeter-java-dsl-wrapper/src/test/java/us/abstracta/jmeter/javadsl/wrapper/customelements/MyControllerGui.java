package us.abstracta.jmeter.javadsl.wrapper.customelements;

import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.testelement.TestElement;

public class MyControllerGui extends AbstractControllerGui {

  @Override
  public String getLabelResource() {
    return null;
  }

  @Override
  public TestElement createTestElement() {
    MyController ret = new MyController();
    modifyTestElement(ret);
    return ret;
  }

  @Override
  public void modifyTestElement(TestElement element) {
    configureTestElement(element);
  }

}
