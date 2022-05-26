package us.abstracta.jmeter.javadsl.core.samplers;

import java.time.Duration;
import java.util.Collections;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.util.JMeterUtils;
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
public abstract class BaseSampler<T extends BaseSampler<?>> extends
    TestElementContainer<BaseSampler.SamplerChild> implements DslSampler {

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
    return (T) addChildren(children);
  }

  /**
   * Test elements which can be nested as children of a sampler in JMeter, should implement this
   * interface.
   */
  public interface SamplerChild extends DslTestElement {

  }


  /**
   * Allows to apply APDEX configuration for exact sampler.
   * Exact information about APDEX you can find here: https://en.wikipedia.org/wiki/Apdex
   *
   * IMPORTANT
   * Use with {@link us.abstracta.jmeter.javadsl.core.listeners.HtmlReporter}
   *
   * @param satisfied - "satisfied" threshold; all samples with a response time below this threshold will count totally
   * @param tolerated - "tolerated" threshold; number samples with a response time between this and "satisfied" thresholds
   *                    will count as half of their total number; and any samples with a response time higher than this
   *                    threshold would not count at all
   * @return the sampler itself as BaseSampler
   *
   * @since 0.57
   */
  public BaseSampler<T> apdex(Duration satisfied, Duration tolerated) {
    String propName = ReportGeneratorConfiguration.REPORT_GENERATOR_KEY_PREFIX + ".apdex_per_transaction";
    String prop = JMeterUtils.getProperty(propName);

    if (!prop.isEmpty()) prop += ";\\";
    prop += this.name + ":" + satisfied.getSeconds() * 1000 + "|" + tolerated.getSeconds() * 1000 + ";\\";
    JMeterUtils.setProperty(propName, prop);

    return this;
  }

}
