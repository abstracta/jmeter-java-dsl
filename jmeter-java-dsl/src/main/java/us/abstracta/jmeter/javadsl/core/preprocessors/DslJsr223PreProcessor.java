package us.abstracta.jmeter.javadsl.core.preprocessors;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.modifiers.JSR223PreProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorVars;
import us.abstracta.jmeter.javadsl.core.testelements.DslJsr223TestElement;

/**
 * Allows running custom logic before executing a sampler.
 * <p>
 * This is a very powerful and flexible component that allows you to modify variables, sampler,
 * context, etc., before running a sampler (for example to generate dynamic requests
 * programmatically).
 * <p>
 * By default, provided script will be interpreted as groovy script, which is the default setting
 * for JMeter. If you need, you can use any of JMeter provided scripting languages (beanshell,
 * javascript, jexl, etc.) by setting the {@link #language(String)} property.
 *
 * @since 0.7
 */
public class DslJsr223PreProcessor extends
    DslJsr223TestElement<DslJsr223PreProcessor, PreProcessorVars> implements DslPreProcessor {

  private static final String DEFAULT_NAME = "JSR223 PreProcessor";

  public DslJsr223PreProcessor(String name, String script) {
    super(name, DEFAULT_NAME, script);
  }

  public DslJsr223PreProcessor(String name, PreProcessorScript script) {
    super(name, DEFAULT_NAME, script);
  }

  public DslJsr223PreProcessor(String name, Class<? extends PreProcessorScript> scriptClass) {
    super(name, DEFAULT_NAME, scriptClass);
  }

  @Override
  protected JSR223TestElement buildJsr223TestElement() {
    return new JSR223PreProcessor();
  }

  @Override
  protected DslLambdaPreProcessor buildLambdaTestElement() {
    name = !DEFAULT_NAME.equals(name) ? name : "Lambda Pre Processor";
    return new DslLambdaPreProcessor();
  }

  public static class DslLambdaPreProcessor extends
      Jsr223DslLambdaTestElement<PreProcessorVars> implements PreProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DslLambdaPreProcessor.class);

    @Override
    public void process() {
      try {
        run(new PreProcessorVars(this));
      } catch (Exception e) {
        LOG.error("Problem in lambda {}", getName(), e);
      }
    }

  }

  /**
   * Allows to use any java code as script.
   *
   * @see PreProcessorVars for a list of provided variables in script execution
   * @since 0.10
   */
  public interface PreProcessorScript extends DslJsr223TestElement.Jsr223Script<PreProcessorVars> {

  }

  public static class PreProcessorVars extends DslJsr223TestElement.Jsr223ScriptVars {

    public PreProcessorVars(TestElement element) {
      super(element, JMeterContextService.getContext());
    }

  }

  public static class CodeBuilder extends Jsr223TestElementCallBuilder<JSR223PreProcessor> {

    public CodeBuilder(List<Method> builderMethods) {
      super(JSR223PreProcessor.class, DEFAULT_NAME, builderMethods);
    }

  }

}
