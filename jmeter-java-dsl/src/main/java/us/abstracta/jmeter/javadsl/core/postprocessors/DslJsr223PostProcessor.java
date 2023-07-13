package us.abstracta.jmeter.javadsl.core.postprocessors;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor.PostProcessorVars;
import us.abstracta.jmeter.javadsl.core.testelements.DslJsr223TestElement;

/**
 * Allows running custom logic after getting a sample result.
 * <p>
 * This is a very powerful and flexible component that allows you to modify sample results (like
 * changing the flag if is success or not), jmeter variables, context settings, etc.
 * <p>
 * By default, provided script will be interpreted as groovy script, which is the default setting
 * for JMeter. If you need, you can use any of JMeter provided scripting languages (beanshell,
 * javascript, jexl, etc.) by setting the {@link #language(String)} property.
 *
 * @since 0.6
 */
public class DslJsr223PostProcessor extends
    DslJsr223TestElement<DslJsr223PostProcessor, PostProcessorVars> implements DslPostProcessor {

  private static final String DEFAULT_NAME = "JSR223 PostProcessor";

  public DslJsr223PostProcessor(String name, String script) {
    super(name, DEFAULT_NAME, script);
  }

  public DslJsr223PostProcessor(String name, PostProcessorScript script) {
    super(name, DEFAULT_NAME, script);
  }

  public DslJsr223PostProcessor(String name, Class<? extends PostProcessorScript> scriptClass) {
    super(name, DEFAULT_NAME, scriptClass);
  }

  @Override
  protected JSR223TestElement buildJsr223TestElement() {
    return new JSR223PostProcessor();
  }

  @Override
  protected DslLambdaPostProcessor buildLambdaTestElement() {
    name = !DEFAULT_NAME.equals(name) ? name : "Lambda Post Processor";
    return new DslLambdaPostProcessor();
  }

  public static class DslLambdaPostProcessor extends
      Jsr223DslLambdaTestElement<PostProcessorVars> implements PostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DslLambdaPostProcessor.class);

    @Override
    public void process() {
      try {
        run(new PostProcessorVars(this));
      } catch (Exception e) {
        LOG.error("Problem in lambda {}", getName(), e);
      }
    }

  }

  /**
   * Allows to use any java code as script.
   *
   * @see PostProcessorVars for a list of provided variables in script execution
   * @since 0.10
   */
  public interface PostProcessorScript extends
      DslJsr223TestElement.Jsr223Script<PostProcessorVars> {

  }

  public static class PostProcessorVars extends DslJsr223TestElement.Jsr223ScriptVars {

    public PostProcessorVars(TestElement element) {
      super(element, JMeterContextService.getContext());
    }

  }

  public static class CodeBuilder extends Jsr223TestElementCallBuilder<JSR223PostProcessor> {

    public CodeBuilder(List<Method> builderMethods) {
      super(JSR223PostProcessor.class, DEFAULT_NAME, builderMethods);
    }

  }

}
