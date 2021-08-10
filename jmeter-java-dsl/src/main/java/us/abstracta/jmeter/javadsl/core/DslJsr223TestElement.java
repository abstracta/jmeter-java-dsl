package us.abstracta.jmeter.javadsl.core;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;

/**
 * Abstracts common logic used by JSR223 test elements.
 *
 * @since 0.8
 */
public abstract class DslJsr223TestElement extends BaseTestElement {

  private static int scriptId = 1;

  private final String script;
  private String language = "groovy";
  /*
  this property is used to hold jmeter properties that will be set when building hash tree (when
  properties have already been loaded). Can't set them in constructor since JMeter properties are
  not yet loaded at that time.
   */
  private final Map<String, Object> jmeterProps = new HashMap<>();

  public DslJsr223TestElement(String name, String defaultName, String script) {
    super(name != null ? name : defaultName, TestBeanGUI.class);
    this.script = script;
  }

  public DslJsr223TestElement(String name, String defaultName, Jsr223Script<?> script,
      Class<?> varsClass) {
    super(name != null ? name : defaultName, TestBeanGUI.class);
    String buildScriptId = buildScriptId();
    jmeterProps.put(buildScriptId, script);
    this.script = buildScriptString(buildScriptId, varsClass);
    System.out.println(this.script);
  }

  private static String buildScriptId() {
    return "Jsr223Script" + scriptId++;
  }

  private static String buildScriptString(String scriptId, Class<?> varsClass) {
    return
        "// It is currently not supported to run scripts defined in Java code in JMeter GUI or"
            + " non Embedded Engine (eg: BlazeMeter).\n"
            + "def script = (" + Jsr223Script.class.getName() + ") props.get('" + scriptId + "')\n"
            + "script.run(new " + varsClass.getName()
            + "(" + buildConstructorParameters(varsClass) + "))";
  }

  private static String buildConstructorParameters(Class<?> varsClass) {
    return Arrays.stream(varsClass.getFields())
        .map(Field::getName)
        .collect(Collectors.joining(","));
  }

  public DslJsr223TestElement language(String language) {
    this.language = language;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    if (!jmeterProps.isEmpty()) {
      JMeterUtils.getJMeterProperties().putAll(jmeterProps);
    }
    JSR223TestElement ret = buildJsr223TestElement();
    ret.setProperty("script", script);
    ret.setProperty("scriptLanguage", language);
    return ret;
  }

  protected abstract JSR223TestElement buildJsr223TestElement();

  protected interface Jsr223Script<T extends Jsr223ScriptVars> {

    void run(T scriptVars) throws Exception;
  }

  protected abstract static class Jsr223ScriptVars {

    public JMeterContext ctx;
    public JMeterVariables vars;
    public Properties props;
    public Sampler sampler;
    public Logger log;
    @SuppressWarnings("checkstyle:membername")
    public String Label;

    public Jsr223ScriptVars(JMeterContext ctx, JMeterVariables vars, Properties props,
        Sampler sampler, Logger log, String label) {
      this.ctx = ctx;
      this.vars = vars;
      this.props = props;
      this.sampler = sampler;
      this.log = log;
      this.Label = label;
    }

  }

}
