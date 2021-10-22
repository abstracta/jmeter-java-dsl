package us.abstracta.jmeter.javadsl.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.BeanInfoSupport;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JSR223BeanInfoSupport;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;
import us.abstracta.jmeter.javadsl.core.DslScriptBuilder.DslScript;
import us.abstracta.jmeter.javadsl.core.DslScriptBuilder.DslScriptVars;

/**
 * Abstracts common logic used by JSR223 test elements.
 *
 * @since 0.8
 */
public abstract class DslJsr223TestElement extends BaseTestElement {

  private final DslScriptBuilder scriptBuilder;
  private String language = "groovy";

  public DslJsr223TestElement(String name, String defaultName, String script) {
    super(name != null ? name : defaultName, TestBeanGUI.class);
    this.scriptBuilder = new DslScriptBuilder(script);
  }

  public DslJsr223TestElement(String name, String defaultName, Jsr223Script<?> script,
      Class<?> varsClass, Map<String, String> varsNameMapping) {
    super(name != null ? name : defaultName, TestBeanGUI.class);
    this.scriptBuilder = new DslScriptBuilder(script, varsClass,
        mapWithEntry("label", "Label", varsNameMapping));
  }

  private static <K, V> Map<K, V> mapWithEntry(K key, V value, Map<K, V> map) {
    HashMap<K, V> ret = new HashMap<>(map);
    ret.put(key, value);
    return ret;
  }

  public DslJsr223TestElement language(String language) {
    this.language = language;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    JSR223TestElement ret = buildJsr223TestElement();
    ret.setScriptLanguage(language);
    ret.setScript(scriptBuilder.build());
    return ret;
  }

  protected abstract JSR223TestElement buildJsr223TestElement();

  @Override
  protected BeanInfoSupport getBeanInfo() {
    return getJsr223BeanInfo();
  }

  protected abstract JSR223BeanInfoSupport getJsr223BeanInfo();

  protected interface Jsr223Script<T extends Jsr223ScriptVars> extends DslScript<T, Void> {

    default Void run(T scriptVars) throws Exception {
      runScript(scriptVars);
      return null;
    }

    void runScript(T scriptVars) throws Exception;

  }

  protected static class Jsr223ScriptVars extends DslScriptVars {

    public final String label;

    public Jsr223ScriptVars(String label, SampleResult prev, JMeterContext ctx,
        JMeterVariables vars, Properties props, Sampler sampler, Logger log) {
      super(prev, ctx, vars, props, sampler, log);
      this.label = label;
    }

  }

}
