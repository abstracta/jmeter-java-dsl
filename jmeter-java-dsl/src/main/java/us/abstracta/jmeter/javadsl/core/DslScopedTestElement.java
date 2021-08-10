package us.abstracta.jmeter.javadsl.core;

import java.util.function.Consumer;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testelement.AbstractScopedTestElement;

/**
 * Contains common logic for test elements that only process certain samples.
 *
 * @param <T> is the type of the test element that extends this class (to properly inherit fluent
 * API methods).
 * @since 0.11
 */
public abstract class DslScopedTestElement<T> extends BaseTestElement {

  private Scope scope = Scope.MAIN_SAMPLE;
  private String scopeVariable;

  protected DslScopedTestElement(String name, Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass);
  }

  /**
   * Allows specifying if the extractor should be specified to main sample and/or sub samples.
   * <p>
   * When not specified the regular extractor will only apply to main sample.
   *
   * @param scope specifying to what sample result apply the regular extractor to.
   * @return the DslRegexExtractor to allow fluent usage and setting other properties.
   * @see Scope
   */
  public T scope(Scope scope) {
    this.scope = scope;
    return (T) this;
  }

  /**
   * Allows specifying that the regular extractor should be applied to the contents of a given
   * JMeter variable.
   * <p>
   * This setting overrides any setting on scope and fieldToCheck.
   *
   * @param scopeVariable specifies the name of the variable to apply the regular extractor to.
   * @return the DslRegexExtractor to allow fluent usage and setting other properties.
   */
  public T scopeVariable(String scopeVariable) {
    this.scopeVariable = scopeVariable;
    return (T) this;
  }

  protected void setScopeTo(AbstractScopedTestElement testElement) {
    scope.applyTo(testElement);
    if (scopeVariable != null) {
      testElement.setScopeVariable(scopeVariable);
    }
  }

  /**
   * Specifies to which samples apply the regular extractor to.
   */
  public enum Scope {
    /**
     * Applies the regular extractor to all samples (main and sub samples).
     */
    ALL_SAMPLES(AbstractScopedTestElement::setScopeAll),
    /**
     * Applies the regular extractor only to main sample (sub samples, like redirects, are not
     * included).
     */
    MAIN_SAMPLE(AbstractScopedTestElement::setScopeParent),
    /**
     * Applies the regular extractor only to sub samples (redirects, embedded resources, etc.).
     */
    SUB_SAMPLES(AbstractScopedTestElement::setScopeChildren);

    private final Consumer<AbstractScopedTestElement> applier;

    Scope(Consumer<AbstractScopedTestElement> applier) {
      this.applier = applier;
    }

    private void applyTo(AbstractScopedTestElement re) {
      applier.accept(re);
    }

  }

}
