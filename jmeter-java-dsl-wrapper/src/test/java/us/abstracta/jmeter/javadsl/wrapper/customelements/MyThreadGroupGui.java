package us.abstracta.jmeter.javadsl.wrapper.customelements;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;

public class MyThreadGroupGui extends AbstractThreadGroupGui {

  @Override
  public String getLabelResource() {
    return null;
  }

  @Override
  public TestElement createTestElement() {
    MyThreadGroup ret = new MyThreadGroup();
    modifyTestElement(ret);
    return ret;
  }

  @Override
  public void modifyTestElement(TestElement element) {
    configureTestElement(element);
  }

}
