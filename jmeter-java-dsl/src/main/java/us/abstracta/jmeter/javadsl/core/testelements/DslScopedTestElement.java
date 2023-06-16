package us.abstracta.jmeter.javadsl.core.testelements;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam.EnumPropertyValue;

/**
 * Contains common logic for test elements that only process certain samples.
 *
 * @param <T> is the type of the test element that extends this class (to properly inherit fluent
 *            API methods).
 * @since 0.11
 */
public abstract class DslScopedTestElement<T extends DslScopedTestElement<T>> extends
    BaseTestElement {

  protected Scope scope = Scope.MAIN_SAMPLE;
  protected String scopeVariable;

  protected DslScopedTestElement(String name, Class<? extends JMeterGUIComponent> guiClass) {
    super(name, guiClass);
  }

  /**
   * Allows specifying if the element should be applied to main sample and/or sub samples.
   * <p>
   * When not specified the element will only apply to main sample.
   *
   * @param scope specifying to what sample result apply the element to.
   * @return the DSL element for further configuration or usage.
   * @see Scope
   */
  public T scope(Scope scope) {
    this.scope = scope;
    return (T) this;
  }

  /**
   * Allows specifying that the element should be applied to the contents of a given JMeter
   * variable.
   * <p>
   * This setting overrides any setting on scope and fieldToCheck.
   *
   * @param scopeVariable specifies the name of the variable to apply the element to.
   * @return the DSL element for further configuration or usage.
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
  public enum Scope implements EnumPropertyValue {
    /**
     * Applies the regular extractor to all samples (main and sub samples).
     */
    ALL_SAMPLES(AbstractScopedTestElement::setScopeAll, "all"),
    /**
     * Applies the regular extractor only to main sample (sub samples, like redirects, are not
     * included).
     */
    MAIN_SAMPLE(AbstractScopedTestElement::setScopeParent, "parent"),
    /**
     * Applies the regular extractor only to sub samples (redirects, embedded resources, etc.).
     */
    SUB_SAMPLES(AbstractScopedTestElement::setScopeChildren, "children");

    private final Consumer<AbstractScopedTestElement> applier;
    private final String propertyValue;

    Scope(Consumer<AbstractScopedTestElement> applier, String propertyValue) {
      this.applier = applier;
      this.propertyValue = propertyValue;
    }

    private void applyTo(AbstractScopedTestElement re) {
      applier.accept(re);
    }

    @Override
    public String propertyValue() {
      return propertyValue;
    }

  }

  /**
   * Abstracts common logic for
   * {@link us.abstracta.jmeter.javadsl.core.testelements.DslScopedTestElement} method call
   * builders.
   *
   * @param <T> is the type of test element class that is used to identify when this call builder
   *            should apply.
   * @since 0.62
   */
  protected abstract static class ScopedTestElementCallBuilder<T extends TestElement> extends
      SingleTestElementCallBuilder<T> {

    private final String scopePrefix;

    protected ScopedTestElementCallBuilder(Class<T> testElementClass, List<Method> builderMethods) {
      this("Sample", testElementClass, builderMethods);
    }

    protected ScopedTestElementCallBuilder(String scopePrefix, Class<T> testElementClass,
        List<Method> builderMethods) {
      super(testElementClass, builderMethods);
      this.scopePrefix = scopePrefix;
    }

    @Override
    protected MethodCall buildMethodCall(T testElement, MethodCallContext context) {
      MethodCall ret = buildScopedMethodCall(testElement);
      chainScopeTo(ret, testElement, scopePrefix);
      chainScopedElementAdditionalOptions(ret, testElement);
      return ret;
    }

    public static void chainScopeTo(MethodCall ret, TestElement testElement, String scopePrefix) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodParam scopeVar = paramBuilder.stringParam("Scope.variable");
      if (scopeVar.isDefault()) {
        ret.chain("scope", paramBuilder.enumParam(scopePrefix + ".scope", Scope.MAIN_SAMPLE));
      } else {
        ret.chain("scopeVariable", scopeVar);
      }
    }

    protected abstract MethodCall buildScopedMethodCall(T testElement);

    protected abstract void chainScopedElementAdditionalOptions(MethodCall ret, T testElement);

  }

}
