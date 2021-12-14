package us.abstracta.jmeter.javadsl.wrapper.wrappers;

import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveGuiClass;
import static us.abstracta.jmeter.javadsl.wrapper.wrappers.TestElementWrapperHelper.solveName;

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.testelements.DslSampler;

/**
 * Is a {@link TestElementWrapper} for JMeter samplers.
 *
 * @since 0.41
 */
public class DslSamplerWrapper extends DslSampler<DslSamplerWrapper> implements
    TestElementWrapper<DslSamplerWrapper> {

  private final TestElementWrapperHelper<Sampler> helper;

  public DslSamplerWrapper(String name, Sampler testElement, AbstractSamplerGui guiComponent) {
    super(solveName(name, testElement, guiComponent), solveGuiClass(testElement, guiComponent));
    this.helper = new TestElementWrapperHelper<>(testElement, guiComponent);
  }

  public DslSamplerWrapper prop(String name, Object value) {
    helper.prop(name, value);
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    return helper.buildTestElement();
  }

}
