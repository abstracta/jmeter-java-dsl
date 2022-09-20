package us.abstracta.jmeter.javadsl.core.samplers;

import java.util.Collections;
import org.apache.jmeter.gui.JMeterGUIComponent;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.testelements.TestElementContainer;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

/**
 * Hosts common logic to all samplers.
 * <p>
 * In particular, it specifies that samplers are {@link ThreadGroupChild} and {@link
 * TestElementContainer} containing {@link SamplerChild}.
 * <p>
 * For an example of an implementation of a sampler check {@link DslHttpSampler}.
 *
 * @since 0.1
 */
public abstract class BaseSampler<T extends BaseSampler<T>> extends
    TestElementContainer<T, BaseSampler.SamplerChild> implements DslSampler {

  protected BaseSampler(String name, Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass, Collections.emptyList());
  }

  /**
   * Allows specifying children test elements for the sampler, which allow for example extracting
   * information from response, alter request, assert response contents, etc.
   *
   * @param children list of test elements to add as children of this sampler.
   * @return the altered sampler to allow for fluent API usage.
   */
  public T children(SamplerChild... children) {
    return super.children(children);
  }

  /**
   * Test elements which can be nested as children of a sampler in JMeter, should implement this
   * interface.
   */
  public interface SamplerChild extends DslTestElement {

  }

}
