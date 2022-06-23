package us.abstracta.jmeter.javadsl.wrapper.customelements;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.testbeans.TestBean;

public class MyThreadGroupTestBean extends MyThreadGroup implements TestBean {

  // overwrite it to avoid failing when jmeter tries to load properties into field
  @Override
  public void setSamplerController(LoopController c) {
  }

}
