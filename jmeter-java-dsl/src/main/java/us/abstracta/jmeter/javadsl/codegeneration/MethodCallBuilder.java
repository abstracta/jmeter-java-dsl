package us.abstracta.jmeter.javadsl.codegeneration;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;

/**
 * Builds a method call for a particular DSL test element.
 * <p>
 * Almost (there might be some exceptions) every DSL test element should have an associated
 * MethodCallBuilder (as inner class) which is in charge to generate the DSL code (in fact an
 * instance of MethodCall used to generate the code) for that test element when proper tree node is
 * found in a JMeter test plan.
 * <p>
 * Whenever you implement a new DSL test element remember creating a nested class named CodeBuilder
 * extending MethodCallBuilder with the proper logic for generating its DSL code. Check
 * {@link us.abstracta.jmeter.javadsl.core.DslTestPlan.CodeBuilder} for an example.
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
   * Allows checking if this builder can build method calls for the given context.
   * <p>
   * This method is invoked in every registered MethodCallBuilder, until one is found that matches.
   *
   * @param context provides information used to determine if a method call might be created by this
   *                builder. For example: JMeter test plan tree node test element, parent context, a
   *                map of entries to host custom information, etc.
   * @return true if this builder knows how to build a method call for the context, false otherwise.
   * @since 0.52
   */
  public abstract boolean matches(MethodCallContext context);

  /**
   * Generates the method call instance for the particular DSL test element.
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
   * @return generated method call for the DSL test element.
   */
  protected abstract MethodCall buildMethodCall(MethodCallContext context);

  /**
   * Builds a method call for the given set of parameters using one of registered builder methods.
   * <p>
   * This method is the starting point for creating the MethodCall returned by
   * {@link #buildMethodCall(MethodCallContext)}.
   *
   * @param params contains the list of parameters to find the proper builder method and associate
   *               to the method call. If the MethodCall accepts as parameter children DSL test
   *               elements, then remember adding an array class for {@link ChildrenParam} (eg:
   *               {@code new ChildrenParam<>(TestPlanChild[].class)}).
   * @return generated method call.
   * @throws UnsupportedOperationException if no builder method is found for the given parameters.
   */
  protected MethodCall buildMethodCall(MethodParam... params) {
    return MethodCall.fromBuilderMethod(findBuilderMethod(params), params);
  }

  private Method findBuilderMethod(MethodParam... params) {
    Method ret = MethodCall.findParamsMatchingMethod(builderMethods.stream(), params);
    if (ret == null) {
      throw MethodCall.buildNoMatchingMethodFoundException(
          builderMethods.get(0).getName() + " builder", params);
    }
    return ret;
  }

  /**
   * This method allows specifying an order over builders.
   * <p>
   * Low values for order will make builders execute first than higher values.
   * <p>
   * This is handy when the order is relevant, for instance if a builder is more generic than others
   * and should be used as fallback when others don't match.
   * <p>
   * No need for specifying an order for each builder. In most of the cases using the default value
   * is ok. Only overwrite when you need a specific order between builders (implementing fallback of
   * builders).
   *
   * @return a number used to order this builder among other existing values.
   * @since 0.60
   */
  public int order() {
    return 1;
  }

  protected static Stream<JMeterProperty> propertyIterator2Stream(PropertyIterator iter) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, 0), false);
  }

  protected static String getBuilderOptionName(Class<?> builderClass, String optionName) {
    String builderName = builderClass.getName();
    String builderNamePrefix = builderName.substring(builderName.lastIndexOf('.',
        builderName.length() - builderClass.getSimpleName().length() - 2));
    return  builderNamePrefix + "." + optionName;
  }

}
