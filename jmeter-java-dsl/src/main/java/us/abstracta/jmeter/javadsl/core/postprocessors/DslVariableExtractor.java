package us.abstracta.jmeter.javadsl.core.postprocessors;

import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import us.abstracta.jmeter.javadsl.core.testelements.DslScopedTestElement;

/**
 * Contains common logic for post processors which extract some value into a variable.
 *
 * @since 0.28
 */
public abstract class DslVariableExtractor<T extends DslVariableExtractor<T>> extends
    DslScopedTestElement<T> implements DslPostProcessor {

  protected String varName;
  protected int matchNumber = 1;
  protected String defaultValue;

  public DslVariableExtractor(String name, Class<? extends AbstractPostProcessorGui> guiClass,
      String varName) {
    super(name, guiClass);
    this.varName = varName;
  }

}
