package us.abstracta.jmeter.javadsl.bridge.serialization.constructs;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import us.abstracta.jmeter.javadsl.bridge.serialization.BridgedObjectConstructor;
import us.abstracta.jmeter.javadsl.bridge.serialization.BuilderMethod;
import us.abstracta.jmeter.javadsl.bridge.serialization.TestElementConstructorException;

public class BridgedObjectConstruct extends BaseBridgedObjectConstruct {

  private static final Map<Class<?>, Function<String, Object>> PARSERS = solveParsers();

  private final BridgedObjectConstructor constructor;
  private final String tag;
  private final List<BuilderMethod> builders;

  public BridgedObjectConstruct(BridgedObjectConstructor constructor, String tag,
      List<BuilderMethod> builders) {
    this.constructor = constructor;
    this.tag = tag;
    this.builders = builders;
    builders.sort(
        Comparator.<BuilderMethod, Integer>comparing(b -> b.getParameters().length).reversed());
  }

  private static Map<Class<?>, Function<String, Object>> solveParsers() {
    Map<Class<?>, Function<String, Object>> ret = new HashMap<>();
    ret.put(int.class, Integer::parseInt);
    ret.put(long.class, Long::parseLong);
    ret.put(boolean.class, Boolean::parseBoolean);
    ret.put(double.class, Double::parseDouble);
    ret.put(Duration.class, Duration::parse);
    ret.put(String.class, s -> s);
    return ret;
  }

  @Override
  public Object construct(Node node) {
    Map<String, Node> nodeProperties = getNodeProperties(node, tag);
    Object ret = buildTestElement(nodeProperties, node);
    nodeProperties.forEach((propName, propNode) -> {
      Method propMethod = findPropertyMethod(propName, propNode, ret);
      try {
        Object[] args = propMethod.getParameters().length == 1
            ? new Object[]{constructParameter(propNode, propMethod.getParameters()[0])}
            : new Object[]{};
        propMethod.invoke(ret, args);
      } catch (ReflectiveOperationException ex) {
        throw new RuntimeException(ex);
      }
    });
    return ret;
  }

  private Method findPropertyMethod(String propName, Node propNode, Object testElement) {
    List<Method> candidates = Stream.of(testElement.getClass().getMethods())
        .filter(m -> propName.equals(m.getName()))
        .collect(Collectors.toList());
    if (candidates.isEmpty()) {
      throw new TestElementConstructorException(tag, propNode,
          "could not find a method for setting property " + propName);
    }
    return candidates.stream()
        // prefer the method accepting strings which is more generic (accepts Jmeter expressions).
        .filter(m -> m.getParameters().length == 1
            && String.class.isAssignableFrom(m.getParameters()[0].getType()))
        .findAny()
        // otherwise just get any of the candidates
        .orElseGet(() -> candidates.get(0));
  }

  private Object buildTestElement(Map<String, Node> nodeProperties, Node node) {
    for (BuilderMethod m : builders) {
      Map<Parameter, Optional<Node>> paramNodes = extractParametersNodes(m, nodeProperties);
      if (allParametersFound(paramNodes)) {
        try {
          paramNodes.keySet().forEach(p -> nodeProperties.remove(p.getName()));
          Object[] args = buildBuilderArguments(paramNodes);
          return m.invoke(args);
        } catch (ReflectiveOperationException e) {
          throw new RuntimeException(e);
        }
      }
    }
    throw new TestElementConstructorException(tag, node, "could not find a proper builder");
  }

  private LinkedHashMap<Parameter, Optional<Node>> extractParametersNodes(BuilderMethod m,
      Map<String, Node> properties) {
    return Arrays.stream(m.getParameters())
        .collect(Collectors.toMap(p -> p,
            p -> Optional.ofNullable(properties.get(p.getName())), (u, v) -> u,
            LinkedHashMap::new));
  }

  private boolean allParametersFound(Map<Parameter, Optional<Node>> paramNodes) {
    return paramNodes.values().stream().allMatch(Optional::isPresent);
  }

  private Object[] buildBuilderArguments(Map<Parameter, Optional<Node>> paramNodes) {
    return paramNodes.entrySet().stream()
        .map(e -> constructParameter(e.getValue().get(), e.getKey()))
        .toArray(Object[]::new);
  }

  private Object constructParameter(Node node, Parameter parameter) {
    Class<?> paramType = parameter.getType();
    Function<String, Object> parser = PARSERS.get(paramType);
    if (parser != null && node instanceof ScalarNode) {
      return parser.apply(((ScalarNode) node).getValue());
    } else if (paramType.isEnum() && node instanceof ScalarNode) {
      return Enum.valueOf((Class) paramType, ((ScalarNode) node).getValue());
    } else {
      Object ret = constructor.constructObject(node);
      if (paramType.isAssignableFrom(ret.getClass())) {
        return ret;
      } else if (paramType.isArray() && ret instanceof List) {
        List<?> list = (List<?>) ret;
        Object arr = Array.newInstance(parameter.getType().getComponentType(), list.size());
        int i = 0;
        for (Object elem : list) {
          Array.set(arr, i++, elem);
        }
        return arr;
      } else if (paramType.isArray()
          && paramType.getComponentType().isAssignableFrom(ret.getClass())) {
        Object arr = Array.newInstance(parameter.getType().getComponentType(), 1);
        Array.set(arr, 0, ret);
        return arr;
      } else {
        throw new TestElementConstructorException(tag, node,
            String.format("expected a %s but got a %s", paramType.getName(),
                ret.getClass().getName()));
      }
    }
  }

}
