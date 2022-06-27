package us.abstracta.jmeter.javadsl.codegeneration;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.testelements.DslScopedTestElement.Scope;

/**
 * Abstracts common logic for
 * {@link us.abstracta.jmeter.javadsl.core.testelements.DslScopedTestElement} method call builders.
 *
 * @param <T> is the type of test element class that is used to identify when this call builder
 *            should apply.
 * @since 0.62
 */
public abstract class ScopedTestElementCallBuilder<T extends TestElement> extends
    SingleTestElementCallBuilder<T> {

  private final String scopePrefix;

  protected ScopedTestElementCallBuilder(Class<T> testElementClass,
      List<Method> builderMethods) {
    this("Scope", testElementClass, builderMethods);
  }

  protected ScopedTestElementCallBuilder(String scopePrefix, Class<T> testElementClass,
      List<Method> builderMethods) {
    super(testElementClass, builderMethods);
    this.scopePrefix = scopePrefix;
  }

  protected MethodCall chainScope(TestElement testElement, MethodCall ret) {
    TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
    MethodParam scopeVar = paramBuilder.stringParam("Scope.variable");
    if (scopeVar.isDefault()) {
      return ret.chain("scope", paramBuilder.enumParam(scopePrefix + ".scope", Scope.MAIN_SAMPLE));
    } else {
      return ret.chain("scopeVariable", scopeVar);
    }
  }

}
