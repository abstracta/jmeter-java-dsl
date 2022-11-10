package us.abstracta.jmeter.javadsl.codegeneration;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.entity.ContentType;
import us.abstracta.jmeter.javadsl.codegeneration.params.BoolParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.testelements.MultiLevelTestElement;

/**
 * Represents a method call, it's parameters and chained invocations.
 * <p>
 * It's main purpose is to generate the code for the method call, parameters and chained methods
 * invocations.
 *
 * @since 0.45
 */
public class MethodCall {

  /**
   * As of 1.2 use {@link Indentation#INDENT} instead.
   */
  @Deprecated
  public static final String INDENT = Indentation.INDENT;
  private static final MethodCall EMPTY_METHOD_CALL = new EmptyMethodCall();

  protected final String methodName;
  private final Class<?> returnType;
  private MethodCall childrenMethod;
  private ChildrenParam<?> childrenParam;
  private final List<MethodParam> params;
  private final List<MethodCall> chain = new ArrayList<>();
  private final Set<String> requiredStaticImports = new HashSet<>();
  private boolean commented;

  protected MethodCall(String methodName, Class<?> returnType, MethodParam... params) {
    this.methodName = methodName;
    this.returnType = returnType;
    this.params = Arrays.asList(params);
  }

  public static MethodCall fromBuilderMethod(Method method, MethodParam... params) {
    MethodCall ret = from(method, params);
    ret.requiredStaticImports.add(method.getDeclaringClass().getName());
    return ret;
  }

  private static MethodCall from(Method method, MethodParam... params) {
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
      MethodParam... params) {
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

  public static MethodCall buildUnsupported() {
    return new MethodCall("unsupported", MultiLevelTestElement.class);
  }

  public void setCommented(boolean commented) {
    this.commented = commented;
  }

  public boolean isCommented() {
    return commented;
  }

  /**
   * Generates a method call that should be ignored (no code should be generated).
   * <p>
   * This is helpful when some MethodCallBuilder supports a given test element conversion, but no
   * associated generated DSL code should be included.
   *
   * @return the empty method call.
   */
  public static MethodCall emptyCall() {
    return EMPTY_METHOD_CALL;
  }

  private static class EmptyMethodCall extends MethodCall {

    protected EmptyMethodCall() {
      super(null, MultiLevelTestElement.class);
    }

    @Override
    public MethodCall child(MethodCall child) {
      // Just ignoring children
      return this;
    }

    @Override
    public String buildCode(String indent) {
      return "";
    }

  }

  public Set<String> getStaticImports() {
    Set<String> ret = new HashSet<>(requiredStaticImports);
    params.stream()
        .filter(p -> !p.isIgnored())
        .forEach(p -> ret.addAll(p.getStaticImports()));
    chain.forEach(c -> ret.addAll(c.getStaticImports()));
    getMethodDefinitions().values()
        .forEach(m -> ret.addAll(m.getStaticImports()));
    return ret;
  }

  public Set<String> getImports() {
    Set<String> ret = new HashSet<>();
    params.stream()
        .filter(p -> !p.isIgnored())
        .forEach(p -> ret.addAll(p.getImports()));
    chain.forEach(c -> ret.addAll(c.getImports()));
    getMethodDefinitions().values()
        .forEach(m -> {
          ret.add(m.getReturnType().getName());
          ret.addAll(m.getImports());
        });
    return ret;
  }

  public Map<String, MethodCall> getMethodDefinitions() {
    return params.stream()
        .filter(p -> !p.isIgnored())
        .flatMap(p -> p.getMethodDefinitions().entrySet().stream())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  public Class<?> getReturnType() {
    return returnType;
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
   * <p>
   * <b>Warning:</b> You should only use this method after applying any required chaining.
   *
   * @param child specifies the method call to be added as child call of this call.
   * @return the current call instance for further configuration.
   */
  public MethodCall child(MethodCall child) {
    solveChildrenParam().addChild(child);
    return this;
  }

  private ChildrenParam<?> solveChildrenParam() {
    if (childrenMethod == null) {
      MethodParam lastParam = params.isEmpty() ? null : params.get(params.size() - 1);
      if (lastParam instanceof ChildrenParam && chain.isEmpty()) {
        childrenMethod = this;
        childrenParam = (ChildrenParam<?>) lastParam;
      } else {
        childrenMethod = findChildrenMethod();
        chain.add(childrenMethod);
        childrenParam = (ChildrenParam<?>) childrenMethod.params.get(0);
      }
    }
    return childrenParam;
  }

  private MethodCall findChildrenMethod() {
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
    return new ChildrenMethodCall(childrenMethod);
  }

  private static class ChildrenMethodCall extends MethodCall {

    protected ChildrenMethodCall(Method method) {
      super(method.getName(), method.getReturnType(),
          new ChildrenParam<>(method.getParameterTypes()[0]));
    }

    @Override
    public String buildCode(String indent) {
      String paramsCode = buildParamsCode(indent + INDENT);
      return paramsCode.isEmpty() ? "" : methodName + "(" + paramsCode + indent + ")";
    }

  }

  public void replaceChild(MethodCall original, MethodCall replacement) {
    solveChildrenParam().replaceChild(original, replacement);
  }

  /**
   * Allows chaining a method call to this call.
   * <p>
   * This method is useful when adding property configuration methods (like
   * {@link DslTestPlan#sequentialThreadGroups()}) or other chained methods that further configure
   * the element (like
   * {@link us.abstracta.jmeter.javadsl.http.DslHttpSampler#post(String, ContentType)}.
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
  public MethodCall chain(String methodName, MethodParam... params) {
    // this eases chaining don't having to check in client code for this condition
    if (params.length > 0 && Arrays.stream(params).allMatch(MethodParam::isDefault)) {
      return this;
    }
    /*
    when chaining methods with booleans in some cases the parameter is required, and in some others
    is not.
     */
    Method method = null;
    if (params.length == 1 && params[0] instanceof BoolParam) {
      method = findMethodInClassHierarchyMatchingParams(methodName, returnType, new MethodParam[0]);
      if (method != null) {
        params = new MethodParam[0];
      }
    }
    if (method == null) {
      method = findMethodInClassHierarchyMatchingParams(methodName, returnType, params);
    }
    if (method == null) {
      throw buildNoMatchingMethodFoundException(
          "public '" + methodName + "' method in " + returnType.getName(), params);
    }
    chain.add(MethodCall.from(method, params));
    return this;
  }

  private Method findMethodInClassHierarchyMatchingParams(String methodName, Class<?> methodClass,
      MethodParam[] params) {
    Method ret = null;
    while (ret == null && methodClass != Object.class) {
      ret = findMethodInClassMatchingParams(methodName, methodClass, params);
      methodClass = methodClass.getSuperclass();
    }
    return ret;
  }

  private Method findMethodInClassMatchingParams(String methodName, Class<?> methodClass,
      MethodParam[] params) {
    Stream<Method> chainableMethods = Arrays.stream(methodClass.getDeclaredMethods())
        .filter(m -> methodName.equals(m.getName()) && Modifier.isPublic(m.getModifiers())
            && m.getReturnType().isAssignableFrom(methodClass));
    return findParamsMatchingMethod(chainableMethods, params);
  }

  protected static Method findParamsMatchingMethod(Stream<Method> methods,
      MethodParam[] params) {
    List<MethodParam> finalParams = Arrays.stream(params)
        .filter(p -> !p.isIgnored())
        .collect(Collectors.toList());
    return methods
        .filter(m -> methodMatchesParameters(m, finalParams))
        .findAny()
        .orElse(null);
  }

  private static boolean methodMatchesParameters(Method m, List<MethodParam> params) {
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
      String methodCondition, MethodParam[] params) {
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

  public String buildCode(String indent) {
    StringBuilder ret = new StringBuilder();
    ret.append(methodName)
        .append("(");
    String childIndent = indent + INDENT;
    String paramsCode = buildParamsCode(childIndent);
    ret.append(paramsCode);
    boolean hasChildren = paramsCode.endsWith("\n");
    if (hasChildren) {
      ret.append(indent);
    }
    ret.append(")");
    String chainedCode = buildChainedCode(childIndent);
    if (!chainedCode.isEmpty()) {
      if (!hasChildren) {
        ret.append("\n")
            .append(childIndent);
      }
      ret.append(".");
      ret.append(chainedCode);
    }
    return commented ? commented(ret.toString(), indent) : ret.toString();
  }

  private String commented(String str, String indent) {
    return "//" + str.replace("\n" + indent, "\n" + indent + "//");
  }

  public String buildAssignmentCode(String indent) {
    String ret = buildCode(indent);
    String indentedParenthesis = INDENT + ")";
    return chain.isEmpty() && ret.endsWith(indentedParenthesis)
        ? ret.substring(0, ret.length() - indentedParenthesis.length()) + ")"
        : ret;
  }

  protected String buildParamsCode(String indent) {
    String ret = params.stream()
        .filter(p -> !p.isIgnored())
        .map(p -> p.buildCode(indent))
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining(", "));
    return ret.replace(", \n", ",\n").replaceAll("\n\\s*\n", "\n");
  }

  private String buildChainedCode(String indent) {
    return chain.stream()
        .map(c -> c.buildCode(indent))
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining("\n" + indent + "."));
  }

}
