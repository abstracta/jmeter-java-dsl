package us.abstracta.jmeter.javadsl.codegeneration;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.entity.ContentType;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.assertions.DslAssertion;
import us.abstracta.jmeter.javadsl.core.configs.DslConfig;
import us.abstracta.jmeter.javadsl.core.controllers.DslController;
import us.abstracta.jmeter.javadsl.core.listeners.DslListener;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslPostProcessor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslPreProcessor;
import us.abstracta.jmeter.javadsl.core.testelements.DslSampler;
import us.abstracta.jmeter.javadsl.core.testelements.MultiLevelTestElement;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslThreadGroup;
import us.abstracta.jmeter.javadsl.core.timers.DslTimer;

/**
 * Represents a method call, it's parameters and chained invocations.
 * <p>
 * It's main purpose is to generate the code for the method call, parameters and chained methods
 * invocations.
 *
 * @since 0.45
 */
public class MethodCall {

  private static final String INDENT = "  ";
  private static final Class<?>[][] EXECUTION_ORDERS = new Class[][]{
      {DslConfig.class},
      {DslPreProcessor.class},
      {DslTimer.class},
      {DslThreadGroup.class, DslController.class, DslSampler.class},
      {DslPostProcessor.class},
      {DslAssertion.class},
      {DslListener.class}
  };
  private static final MethodCall EMPTY_CALL = new MethodCall(null, Object.class);

  private final String methodName;
  private final Class<?> returnType;
  private final int executionOrder;
  private final Class<?> childrenType;
  private final List<MethodParam<?>> params;
  private final List<MethodCall> children = new ArrayList<>();
  private final List<MethodCall> chain = new ArrayList<>();
  // this is used to cache children method and avoid having to look it up in each request for child
  private MethodCall childrenMethod;

  private MethodCall(String methodName, Class<?> returnType, MethodParam<?>... params) {
    this.methodName = methodName;
    this.returnType = returnType;
    this.executionOrder = findExecutionOrder(returnType);
    this.params = Arrays.asList(params);
    if (params.length > 0 && params[params.length - 1] instanceof ChildrenParam) {
      int lastParamIndex = params.length - 1;
      childrenType = params[lastParamIndex].getType();
      childrenMethod = this;
    } else {
      this.childrenType = null;
    }
  }

  private static int findExecutionOrder(Class<?> returnType) {
    for (int i = 0; i < EXECUTION_ORDERS.length; i++) {
      if (Arrays.stream(EXECUTION_ORDERS[i])
          .anyMatch(c -> c.isAssignableFrom(returnType))) {
        return i;
      }
    }
    return -1;
  }

  protected static MethodCall from(Method method, MethodParam<?>... params) {
    return new MethodCall(method.getName(), method.getReturnType(), params);
  }

  /**
   * Generates a new instance for a static method within a given class that is applicable to a given
   * set of parameters.
   * <p>
   * This is usually used to get clas factory methods calls. Eg: Duration.ofSeconds.
   *
   * @param methodClass the class that contains the static method.
   * @param methodName  the name of the method to search for in the given class.
   * @param params      the parameters used to search the method in the given class and to associate
   *                    to the method call.
   * @return the newly created instance
   */
  public static MethodCall forStaticMethod(Class<?> methodClass, String methodName,
      MethodParam<?>... params) {
    Class<?>[] paramsTypes = Arrays.stream(params)
        .map(MethodParam::getType)
        .toArray(Class[]::new);
    Method method = MethodCall.findRequiredStaticMethod(methodClass, methodName, paramsTypes);
    return new MethodCall(methodClass.getSimpleName() + "." + method.getName(),
        method.getReturnType(), params);
  }

  private static Method findRequiredStaticMethod(Class<?> methodClass, String methodName,
      Class<?>... paramsTypes) {
    try {
      Method ret = methodClass.getDeclaredMethod(methodName, paramsTypes);
      if (!Modifier.isPublic(ret.getModifiers()) || !Modifier.isStatic(ret.getModifiers())) {
        throw new RuntimeException(
            "Can't access method " + ret + " which is no longer static or public. "
                + "Check that no dependencies or APIs have been changed.");
      }
      return ret;
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(
          "Can't find method " + methodClass.getName() + "." + methodName
              + " for parameter types " + Arrays.toString(paramsTypes)
              + ". Check that no dependencies or APIs have been changed.", e);
    }
  }

  /**
   * Generates a new method call which is ignored when trying to add it as child of another method.
   * <p>
   * This is useful when you know a code generator does support generating code for a given element
   * but no call has to be included in DSL code. Eg: httpCache().
   *
   * @return the empty call instance.
   */
  public static MethodCall emptyCall() {
    return EMPTY_CALL;
  }

  protected boolean isEmptyCall() {
    return this == EMPTY_CALL;
  }

  protected static MethodCall buildUnsupported() {
    return new MethodCall("unsupported", UnsupportedTestElement.class);
  }

  private static class UnsupportedTestElement implements MultiLevelTestElement {

    public void children(DslTestElement... child) {
    }

    @Override
    public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
      return null;
    }

    @Override
    public void showInGui() {
    }

  }

  /**
   * Allows adding a child call to this call.
   * <p>
   * This method should only be used in seldom scenarios where you need to manually add children
   * calls. In most of the cases this is not necessary, since DSL framework automatically takes care
   * of JMeter children conversion.
   * <p>
   * If the call defines a {@link ChildrenParam} parameter, then children are just added as
   * parameters of the call. Otherwise, a children method will be looked into the class retunrned by
   * this method, and if there is, then chained into this call and used to register provided child
   * element.
   *
   * <b>Warning:</b> You should only use this method after applying any required chaining.
   *
   * @param child specifies the method call to be added as child call of this call.
   * @return the current call instance, or chained children method for further addition of children.
   */
  public MethodCall child(MethodCall child) {
    if (childrenMethod == null) {
      childrenMethod = findChildrenMethod();
    }
    Class<?> childrenType = childrenMethod.childrenType.getComponentType();
    if (!childrenType.isAssignableFrom(child.returnType)) {
      throw new IllegalArgumentException("Trying to add a child of type " + child.returnType
          + " that is not compatible with the declared ones for the method " + methodName + ": "
          + childrenType);
    }
    childrenMethod.children.add(child);
    if (childrenMethod != this
        && (chain.isEmpty() || chain.get(chain.size() - 1) != childrenMethod)) {
      chain.add(childrenMethod);
    }
    return childrenMethod;
  }

  private MethodCall findChildrenMethod() {
    if (childrenType != null) {
      return this;
    }
    Method childrenMethod = null;
    Class<?> methodHolder = returnType;
    while (childrenMethod == null && methodHolder != Object.class) {
      childrenMethod = Arrays.stream(methodHolder.getDeclaredMethods())
          .filter(m -> Modifier.isPublic(m.getModifiers()) && "children".equals(m.getName())
              && m.getParameterCount() == 1)
          .findAny()
          .orElse(null);
      methodHolder = methodHolder.getSuperclass();
    }
    if (childrenMethod == null) {
      throw new IllegalStateException("No children method found for " + returnType + ". "
          + "This might be due to unexpected test plan structure or missing method in test element"
          + ". Please create an issue in GitHub repository if you find any of these cases.");
    }
    return new MethodCall(childrenMethod.getName(), childrenMethod.getReturnType(),
        new ChildrenParam<>(childrenMethod.getParameterTypes()[0]));
  }

  /**
   * Allows chaining a method call to this call.
   * <p>
   * This method is useful when adding property configuration methods (like {@link
   * DslTestPlan#sequentialThreadGroups()}) or other chained methods that further configure the
   * element (like {@link us.abstracta.jmeter.javadsl.http.DslHttpSampler#post(String,
   * ContentType)}.
   * <p>
   * This method abstracts some common logic regarding chaining. For example: if chained method only
   * contains a parameter and its value is the default one, then method is not chained, since it is
   * not necessary. It also takes care of handling boolean parameters which chained method may or
   * may not include a boolean parameter.
   *
   * @param methodName is the name of the method contained in the returned instance of this method
   *                   call, which has to be chained to this method call.
   * @param params     is the list of parameters used to find the method and associated to the
   *                   chained method call. Take into consideration that the exact same number and
   *                   type of parameters must be specified for the method to be found, otherwise an
   *                   exception will be generated.
   * @return this call instance for further chaining or addition of children elements.
   * @throws UnsupportedOperationException when no method with given names and/or parameters can be
   *                                       found to be chained in current method call.
   */
  public MethodCall chain(String methodName, MethodParam<?>... params) {
    // this eases chaining don't having to check in client code for this condition
    if (params.length == 1 && params[0].isDefault()) {
      return this;
    }
    Method method = findMethodInClassHierarchyMatchingParams(methodName, returnType, params);
    /*
    when chaining methods with booleans in some cases the parameter is required, and in some others
    is not.
     */
    if (method == null && params.length == 1 && params[0] instanceof MethodParam.BoolParam) {
      method = findMethodInClassHierarchyMatchingParams(methodName, returnType, new MethodParam[0]);
      if (method != null) {
        params = new MethodParam[0];
      }
    }
    if (method == null) {
      throw buildNoMatchingMethodFoundException(
          "public '" + methodName + "' method in " + returnType.getName(), params);
    }
    chain.add(MethodCall.from(method, params));
    return this;
  }

  private Method findMethodInClassHierarchyMatchingParams(String methodName, Class<?> methodClass,
      MethodParam<?>[] params) {
    Method ret = null;
    while (ret == null && methodClass != Object.class) {
      ret = findMethodInClassMatchingParams(methodName, methodClass, params);
      methodClass = methodClass.getSuperclass();
    }
    return ret;
  }

  private Method findMethodInClassMatchingParams(String methodName, Class<?> methodClass,
      MethodParam<?>[] params) {
    Stream<Method> chainableMethods = Arrays.stream(methodClass.getDeclaredMethods())
        .filter(m -> methodName.equals(m.getName()) && Modifier.isPublic(m.getModifiers())
            && m.getReturnType() == methodClass);
    return findParamsMatchingMethod(chainableMethods, params);
  }

  protected static Method findParamsMatchingMethod(Stream<Method> methods,
      MethodParam<?>[] params) {
    List<MethodParam<?>> finalParams = Arrays.stream(params)
        .filter(p -> !p.isIgnored())
        .collect(Collectors.toList());
    return methods
        .filter(m -> methodMatchesParameters(m, finalParams))
        .findAny()
        .orElse(null);
  }

  private static boolean methodMatchesParameters(Method m, List<MethodParam<?>> params) {
    if (m.getParameterCount() != params.size()) {
      return false;
    }
    Class<?>[] paramTypes = m.getParameterTypes();
    for (int i = 0; i < params.size(); i++) {
      if (!params.get(i).getType().isAssignableFrom(paramTypes[i])) {
        return false;
      }
    }
    return true;
  }

  protected static UnsupportedOperationException buildNoMatchingMethodFoundException(
      String methodCondition, MethodParam<?>[] params) {
    return new UnsupportedOperationException(
        "No " + methodCondition + " method was found for parameters " + Arrays.toString(params)
            + ". This is probably due to some change in DSL not reflected in associated code "
            + "builder.");
  }

  /**
   * Allows extracting from a given call the list of chained method calls and re assign them to this
   * call.
   * <p>
   * This is usually helpful when you provide in a DSL element alias methods for children elements.
   * Eg: {@link us.abstracta.jmeter.javadsl.http.DslHttpSampler#header(String, String)}.
   *
   * @param other is the call to extract the chained methods from.
   */
  public void reChain(MethodCall other) {
    this.chain.addAll(other.chain);
  }

  /**
   * Generates the code for this method call and all associated parameters, children elements and
   * chained methods.
   *
   * @return the generated code.
   */
  public String buildCode() {
    return buildCode("");
  }

  private String buildCode(String indent) {
    StringBuilder ret = new StringBuilder();
    ret.append(methodName)
        .append("(");
    ret.append(params.stream()
        .filter(p -> !p.isIgnored() && !(p instanceof ChildrenParam))
        .map(MethodParam::buildCode)
        .collect(Collectors.joining(", ")));
    String childIndent = indent + INDENT;
    List<MethodCall> children = this.children.stream()
        // order elements to provide the most intuitive representation and ease tests
        .sorted(Comparator.comparing(c -> c.executionOrder))
        .collect(Collectors.toList());
    if (!children.isEmpty()) {
      if (ret.charAt(ret.length() - 1) != '(') {
        ret.append(",");
      }
      ret.append("\n")
          .append(childIndent);
      ret.append(children.stream()
          .map(c -> c.buildCode(childIndent))
          .collect(Collectors.joining(",\n" + childIndent))
      );
      ret.append("\n")
          .append(indent);
    }
    ret.append(")");
    if (!chain.isEmpty()) {
      if (children.isEmpty()) {
        ret.append("\n")
            .append(childIndent);
      }
      ret.append(".");
      ret.append(chain.stream()
          .map(c -> c.buildCode(childIndent))
          .collect(Collectors.joining("\n" + childIndent + "."))
      );
    }
    return ret.toString();
  }

}
