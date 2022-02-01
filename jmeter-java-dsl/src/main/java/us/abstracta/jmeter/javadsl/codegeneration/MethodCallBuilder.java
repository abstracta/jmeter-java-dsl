package us.abstracta.jmeter.javadsl.codegeneration;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Builds a method call for a particular DSL test element.
 * <p>
 * Almost (there might be some exceptions) every DSL test element should have an associated
 * MethodCallBuilder (as inner class) which is in charge to generate the DSL code (in fact an
 * instance of MethodCall used to generate the code) for that test element when proper tree node is
 * found in a JMeter test plan.
 * <p>
 * Whenever you implement a new DSL test element remember creating a nested class named CodeBuilder
 * extending MethodCallBuilder with the proper logic for generating its DSL code. Check {@link
 * us.abstracta.jmeter.javadsl.core.DslTestPlan.CodeBuilder} for an example.
 *
 * @since 0.45
 */
public abstract class MethodCallBuilder {

  protected final List<Method> builderMethods;

  /**
   * This constructor is used by the DslCodeGenerator to instantiate MethodCallBuilders providing
   * them their associated builder methods found in DSL classes registered on the DslCodeGenerator.
   *
   * @param builderMethods the list of builder methods found in the DSL classes associated to this
   *                       MethodCallBuilder parent DSL test element
   */
  protected MethodCallBuilder(List<Method> builderMethods) {
    this.builderMethods = builderMethods;
  }

  /**
   * Generates the method call instance for the particular DSL test element.
   * <p>
   * This method is invoked in every registered MethodCallBuilder, until one is found that doesn't
   * return null. You have to take this into consideration if you implement a MethodCallBuilder
   * since you should return null for every JMeter test plan tree node test element for which your
   * MethodCallBuilder doesn't have to apply.
   * <p>
   * You can check {@link us.abstracta.jmeter.javadsl.core.DslTestPlan.CodeBuilder} to get an idea
   * of how a general implementation of this method looks like
   * <p>
   * If you find a scenario where your MethodCallBuilder applies to the given context, but no code
   * has to be generated for it, then use {@link MethodCall#emptyCall()} to return a call that
   * generates no code.
   *
   * @param context provides all information that might be required to generate the method call. For
   *                example: JMeter test plan tree node test element, parent context, a map of
   *                entries to host custom information, etc.
   * @return generated method call for the DSL test element, or null if the method call builder does
   * not apply to the given context (test plan tree).
   */
  protected abstract MethodCall buildMethodCall(MethodCallContext context);

  /**
   * Builds a method call for the given set of parameters using one of registered builder methods.
   * <p>
   * This method is the starting point for creating the MethodCall returned by {@link
   * #buildMethodCall(MethodCallContext)}.
   *
   * @param params contains the list of parameters to find the proper builder method and associate
   *               to the method call. If the MethodCall accepts as parameter children DSL test
   *               elements, then remember adding an array class for {@link
   *               us.abstracta.jmeter.javadsl.codegeneration.MethodParam.ChildrenParam} (eg: {@code
   *               new ChildrenParam<>(TestPlanChild[].class)}).
   * @return generated method call.
   * @throws UnsupportedOperationException if no builder method is found for the given parameters.
   */
  protected MethodCall buildMethodCall(MethodParam<?>... params) {
    return MethodCall.from(findBuilderMethod(params), params);
  }

  private Method findBuilderMethod(MethodParam<?>... params) {
    Method ret = MethodCall.findParamsMatchingMethod(builderMethods.stream(), params);
    if (ret == null) {
      throw MethodCall.buildNoMatchingMethodFoundException(
          builderMethods.get(0).getName() + " builder", params);
    }
    return ret;
  }

}
