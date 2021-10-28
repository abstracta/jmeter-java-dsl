package us.abstracta.jmeter.javadsl.core.postprocessors;

import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import us.abstracta.jmeter.javadsl.core.DslScopedTestElement;
import us.abstracta.jmeter.javadsl.core.MultiLevelTestElement;

/**
 * Contains common logic for post processors which extract some value into a variable.
 *
 * @since 0.28
 */
public abstract class DslVariableExtractor<T> extends DslScopedTestElement<T> implements
    MultiLevelTestElement {

  protected final String varName;
  protected int matchNumber = 1;
  protected String defaultValue;

  public DslVariableExtractor(String varName, String name,
      Class<? extends AbstractPostProcessorGui> guiClass) {
    super(name, guiClass);
    this.varName = varName;
  }

}
