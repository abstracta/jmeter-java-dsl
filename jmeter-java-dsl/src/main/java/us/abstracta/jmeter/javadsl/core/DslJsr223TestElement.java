package us.abstracta.jmeter.javadsl.core;

import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JSR223TestElement;

public abstract class DslJsr223TestElement extends BaseTestElement {

  private final String script;
  private String language = "groovy";

  public DslJsr223TestElement(String name, String script) {
    super(name, TestBeanGUI.class);
    this.script = script;
  }

  public DslJsr223TestElement language(String language) {
    this.language = language;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    JSR223TestElement ret = buildJsr223TestElement();
    ret.setProperty("script", script);
    ret.setProperty("scriptLanguage", language);
    return ret;
  }

  protected abstract JSR223TestElement buildJsr223TestElement();

}
