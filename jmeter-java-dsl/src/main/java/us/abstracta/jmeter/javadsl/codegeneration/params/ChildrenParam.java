package us.abstracta.jmeter.javadsl.codegeneration.params;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.core.assertions.DslAssertion;
import us.abstracta.jmeter.javadsl.core.configs.DslConfig;
import us.abstracta.jmeter.javadsl.core.configs.DslVariables;
import us.abstracta.jmeter.javadsl.core.controllers.DslController;
import us.abstracta.jmeter.javadsl.core.listeners.DslListener;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslPostProcessor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslPreProcessor;
import us.abstracta.jmeter.javadsl.core.samplers.DslSampler;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslThreadGroup;
import us.abstracta.jmeter.javadsl.core.timers.DslTimer;

/**
 * Is a parameter used to specify DSL test element children methods.
 * <p>
 * This is usually used in TestElementContainer instances which usually provide a builder method
 * with basic required parameters and children elements (eg: thread groups &amp; controllers).
 *
 * @param <T> The type of the children DSl test elements.
 * @since 0.45
 */
public class ChildrenParam<T> extends MethodParam {

  private static final Class<?>[][] EXECUTION_ORDERS = new Class[][]{
      {DslVariables.class},
      {DslConfig.class},
      {DslPreProcessor.class},
      {DslTimer.class},
      {DslThreadGroup.class, DslController.class, DslSampler.class},
      {DslPostProcessor.class},
      {DslAssertion.class},
      {DslListener.class}
  };

  private final List<MethodCall> children;

  public ChildrenParam(Class<T> childrenClass) {
    this(childrenClass, new ArrayList<>());
  }

  public ChildrenParam(Class<T> childrenClass, List<MethodCall> children) {
    super(checkChildrenType(childrenClass), null);
    this.children = children;
  }

  private static <T> Class<T> checkChildrenType(Class<T> childrenClass) {
    if (!childrenClass.isArray()) {
      throw new RuntimeException("You need always to provide an array class and not the raw "
          + "class for the children. Eg use TestPlanChild[].class");
    }
    return childrenClass;
  }

  @Override
  public boolean isDefault() {
    return children.isEmpty();
  }

  @Override
  public Set<String> getStaticImports() {
    return children.stream()
        .flatMap(c -> c.getStaticImports().stream())
        .collect(Collectors.toSet());
  }

  @Override
  public Set<String> getImports() {
    return children.stream()
        .flatMap(c -> c.getImports().stream())
        .collect(Collectors.toSet());
  }

  public Map<String, MethodCall> getMethodDefinitions() {
    return children.stream()
        .flatMap(c -> c.getMethodDefinitions().entrySet().stream())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
            (u, v) -> {
              throw new IllegalStateException("Duplicate key " + u);
            },
            LinkedHashMap::new));
  }

  @Override
  public String buildCode(String indent) {
    List<MethodCall> childrenCalls = children.stream()
        // order elements to provide the most intuitive representation and ease tests
        .sorted(Comparator.comparing(c -> findExecutionOrder(c.getReturnType())))
        .collect(Collectors.toList());
    String ret = childrenCalls.stream()
        .map(c -> c.buildCode(indent))
        .filter(s -> !s.isEmpty())
        .collect(Collectors.joining(",\n" + indent));
    ret = commentLastUncommentedComma(ret);
    return ret.isEmpty() ? ret : "\n" + indent + ret + "\n";
  }

  private String commentLastUncommentedComma(String ret) {
    return ret.replaceAll("(?s),((?:\n\\s*//[^\n]+)+)$", "//,$1");
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

  public void addChild(MethodCall child) {
    Class<?> childrenType = paramType.getComponentType();
    if (!childrenType.isAssignableFrom(child.getReturnType())) {
      throw new IllegalArgumentException("Trying to add a child of type " + child.getReturnType()
          + " that is not compatible with the declared ones : " + childrenType);
    }
    children.add(child);
  }

  public void replaceChild(MethodCall original, MethodCall replacement) {
    children.replaceAll(c -> c == original ? replacement : c);
  }

  public void prependChild(MethodCall child) {
    children.add(0, child);
  }

}
