package us.abstracta.jmeter.javadsl.core;

import java.util.List;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;

/**
 * This abstract class hosts the logic common to all samplers.
 *
 * In particular this class specifies that samplers are {@link ThreadGroupChild} and {@link
 * TestElementContainer} containing {@link SamplerChild}.
 *
 * For an example of an implementation of a sampler check {@link DslHttpSampler}.
 */
public abstract class DslSampler extends
    TestElementContainer<DslSampler.SamplerChild> implements ThreadGroupChild {

  protected DslSampler(String name, List<? extends DslSampler.SamplerChild> children) {
    super(name, children);
  }

  /**
   * Test elements which can be nested as children of a sampler in JMeter, should implement this
   * interface.
   */
  public interface SamplerChild extends DslTestElement {

  }

}
