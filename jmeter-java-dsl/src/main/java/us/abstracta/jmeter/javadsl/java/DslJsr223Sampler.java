package us.abstracta.jmeter.javadsl.java;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.jmeter.protocol.java.sampler.JSR223Sampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JSR223TestElement;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.samplers.BaseSampler.SamplerChild;
import us.abstracta.jmeter.javadsl.core.samplers.DslSampler;
import us.abstracta.jmeter.javadsl.core.testelements.DslJsr223TestElement;

/**
 * Allows sampling java APIs and custom logic.
 * <p>
 * This is a very powerful component, but using it makes code harder to maintain. When there is
 * another sampler available that satisfy your needs use it instead of this one.
 * <p>
 * By default, provided script will be interpreted as groovy script, which is the default setting
 * for JMeter. If you need, you can use any of JMeter provided scripting languages (beanshell,
 * javascript, jexl, etc.) by setting the {@link #language(String)} property.
 *
 * @since 0.22
 */
public class DslJsr223Sampler extends DslJsr223TestElement<DslJsr223Sampler> implements DslSampler {

  private static final String DEFAULT_NAME = "JSR223 Sampler";

  protected final List<SamplerChild> children = new ArrayList<>();

  public DslJsr223Sampler(String name, String script) {
    super(name, DEFAULT_NAME, script);
  }

  public DslJsr223Sampler(String name, SamplerScript script) {
    super(name, DEFAULT_NAME, script, SamplerVars.class,
        Collections.singletonMap("sampleResult", "SampleResult"));
  }

  /**
   * Allows specifying children test elements for the sampler, which allows for example extracting
   * information from response, assert response contents, etc.
   *
   * @param children list of test elements to add as children of this sampler.
   * @return the sampler for further configuration or usage.
   */
  public DslJsr223Sampler children(SamplerChild... children) {
    this.children.addAll(Arrays.asList(children));
    return this;
  }

  @Override
  protected JSR223TestElement buildJsr223TestElement() {
    return new JSR223Sampler();
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    HashTree ret = super.buildTreeUnder(parent, context);
    children.forEach(c -> context.buildChild(c, ret));
    return ret;
  }

  /**
   * Allows to use any java code as script.
   *
   * @see DslJsr223Sampler.SamplerVars for a list of provided variables in script execution
   */
  public interface SamplerScript extends Jsr223Script<DslJsr223Sampler.SamplerVars> {

  }

  public static class SamplerVars extends Jsr223ScriptVars {

    public final SampleResult sampleResult;

    public SamplerVars(SampleResult sampleResult, String label, SampleResult prev,
        JMeterContext ctx, JMeterVariables vars, Properties props, Sampler sampler, Logger log) {
      super(label, prev, ctx, vars, props, sampler, log);
      this.sampleResult = sampleResult;
    }

  }

  public static class CodeBuilder extends Jsr223TestElementCallBuilder<JSR223Sampler> {

    public CodeBuilder(List<Method> builderMethods) {
      super(JSR223Sampler.class, DEFAULT_NAME, builderMethods);
    }

  }

}
