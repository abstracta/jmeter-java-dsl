package us.abstracta.jmeter.javadsl.wrapper.customelements;

import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

public class MySamplerGui extends AbstractSamplerGui {

  @Override
  public String getLabelResource() {
    return null;
  }

  @Override
  public TestElement createTestElement() {
    MySampler ret = new MySampler();
    modifyTestElement(ret);
    return ret;
  }

  @Override
  public void modifyTestElement(TestElement element) {
    configureTestElement(element);
  }

}
