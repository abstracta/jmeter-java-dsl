package us.abstracta.jmeter.javadsl.core.testelements;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.core.testelements.DslJsr223TestElement.Jsr223ScriptVars;
import us.abstracta.jmeter.javadsl.core.util.DslScript;
import us.abstracta.jmeter.javadsl.core.util.DslScript.DslScriptRegistry;
import us.abstracta.jmeter.javadsl.core.util.DslScript.DslScriptVars;

/**
 * Abstracts common logic used by JSR223 test elements.
 *
 * @since 0.8
 */
public abstract class DslJsr223TestElement<T extends DslJsr223TestElement<T, V>,
    V extends Jsr223ScriptVars> extends BaseTestElement {

  protected static final String DEFAULT_LANGUAGE = "groovy";

  protected final String scriptString;
  protected final Jsr223Script<?> script;
  protected final Class<? extends Jsr223Script<?>> scriptClass;
  protected String language = DEFAULT_LANGUAGE;

  public DslJsr223TestElement(String name, String defaultName, Jsr223Script<?> script) {
    this(name, defaultName, null, script, null);
  }

  private DslJsr223TestElement(String name, String defaultName, String scriptString,
      Jsr223Script<?> script, Class<? extends Jsr223Script<?>> scriptClass) {
    super(name != null ? name : defaultName, TestBeanGUI.class);
    this.script = script;
    this.scriptString = scriptString;
    this.scriptClass = scriptClass;
  }

  public DslJsr223TestElement(String name, String defaultName, String script) {
    this(name, defaultName, script, null, null);
  }

  public DslJsr223TestElement(String name, String defaultName,
      Class<? extends Jsr223Script<?>> scriptClass) {
    this(name, defaultName, null, null, scriptClass);
  }

  public T language(String language) {
    this.language = language;
    return (T) this;
  }

  @Override
  protected TestElement buildTestElement() {
    if (scriptString != null) {
      JSR223TestElement ret = buildJsr223TestElement();
      ret.setScriptLanguage(language);
      ret.setScript(scriptString);
      return ret;
    } else {
      Jsr223DslLambdaTestElement<?> ret = buildLambdaTestElement();
      ret.setScriptId(script != null ? DslScriptRegistry.register(script) : scriptClass.getName());
      return ret;
    }
  }

  protected abstract JSR223TestElement buildJsr223TestElement();

  protected abstract Jsr223DslLambdaTestElement<V> buildLambdaTestElement();

  public abstract static class Jsr223DslLambdaTestElement<V extends Jsr223ScriptVars> extends
      AbstractTestElement implements TestBean, ThreadListener, TestIterationListener,
      LoopIterationListener {

    private static final String SCRIPT_ID_PROP = "SCRIPT_ID";
    private Jsr223Script<V> script;

    public Jsr223DslLambdaTestElement() {
      setComment(
          "Check https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas for instructions on how to run this element in remote engines (like BlazeMeter) or in JMeter standalone GUI.");
    }

    public void setScriptId(String scriptId) {
      setProperty(SCRIPT_ID_PROP, scriptId);
    }

    public String getScriptId() {
      return getPropertyAsString(SCRIPT_ID_PROP);
    }

    @Override
    public void threadStarted() {
      script = getScript();
      if (script instanceof ThreadListener) {
        ((ThreadListener) script).threadStarted();
      }
    }

    private Jsr223Script<V> getScript() {
      String scriptId = getScriptId();
      Jsr223Script<V> script = DslScriptRegistry.findLambdaScript(scriptId);
      try {
        return script != null ? script : (Jsr223Script<V>) Class.forName(scriptId).newInstance();
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void testIterationStart(LoopIterationEvent event) {
      if (script instanceof TestIterationListener) {
        ((TestIterationListener) script).testIterationStart(event);
      }
    }

    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
      if (script instanceof LoopIterationListener) {
        ((LoopIterationListener) script).iterationStart(iterEvent);
      }
    }

    public void run(V vars) throws Exception {
      script.run(vars);
    }

    @Override
    public void threadFinished() {
      if (script instanceof ThreadListener) {
        ((ThreadListener) script).threadFinished();
      }
    }

  }

  protected interface Jsr223Script<V extends Jsr223ScriptVars> extends DslScript<V, Void> {

    @Override
    default Void run(V vars) throws Exception {
      runScript(vars);
      return null;
    }

    void runScript(V vars) throws Exception;

  }

  public static class Jsr223ScriptVars extends DslScriptVars {

    public final String label;

    public Jsr223ScriptVars(TestElement testElement, JMeterContext ctx) {
      super(ctx.getPreviousResult(), ctx, ctx.getVariables(), JMeterUtils.getJMeterProperties(),
          ctx.getCurrentSampler(),
          LoggerFactory.getLogger(testElement.getClass().getName() + "." + testElement.getName()));
      this.label = testElement.getName();
    }

  }

  public static class Jsr223TestElementCallBuilder<T extends TestElement> extends
      SingleTestElementCallBuilder<T> {

    private final String defaultName;

    protected Jsr223TestElementCallBuilder(Class<T> testElementClass, String defaultName,
        List<Method> builderMethods) {
      super(testElementClass, builderMethods);
      this.defaultName = defaultName;
    }

    @Override
    protected MethodCall buildMethodCall(T testElement,
        MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      return buildMethodCall(paramBuilder.nameParam(defaultName),
          paramBuilder.stringParam("script"))
          .chain("language", paramBuilder.stringParam("scriptLanguage", DEFAULT_LANGUAGE));
    }

  }

}
