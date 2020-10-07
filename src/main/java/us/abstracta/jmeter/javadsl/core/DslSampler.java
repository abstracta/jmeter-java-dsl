package us.abstracta.jmeter.javadsl.core;

import java.util.List;
import org.apache.jmeter.gui.JMeterGUIComponent;
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

  /**
   * @deprecated use {@link #DslSampler(String, Class, List)} instead to properly support saving to
   * valid jmx.
   */
  @Deprecated
  protected DslSampler(String name, List<? extends DslSampler.SamplerChild> children) {
    this(name, null, children);
  }

  protected DslSampler(String name, Class<? extends JMeterGUIComponent> guiClass,
      List<? extends DslSampler.SamplerChild> children) {
    super(name, guiClass, children);
  }

  /**
   * Test elements which can be nested as children of a sampler in JMeter, should implement this
   * interface.
   */
  public interface SamplerChild extends DslTestElement {

  }

}
