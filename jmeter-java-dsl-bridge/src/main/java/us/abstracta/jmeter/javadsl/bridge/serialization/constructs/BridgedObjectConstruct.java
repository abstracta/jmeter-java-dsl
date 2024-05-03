package us.abstracta.jmeter.javadsl.bridge.serialization.constructs;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.entity.ContentType;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
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
    builders.sort((b1, b2) -> {
      int ret = compareMoreParametersFirst(b1, b2);
      return ret != 0 ? ret : compareFirstStringParameters(b1, b2);
    });
  }

  private int compareMoreParametersFirst(BuilderMethod b1, BuilderMethod b2) {
    return b2.getParameters().length - b1.getParameters().length;
  }

  // prefer methods with string parameters (JMeter expressions) over specific types
  private int compareFirstStringParameters(BuilderMethod b1, BuilderMethod b2) {
    Parameter[] params1 = b1.getParameters();
    Parameter[] params2 = b2.getParameters();
    for (int i = 0; i < params1.length; i++) {
      if (params1[i].getType().equals(String.class) && !params2[i].getType()
          .equals(String.class)) {
        return -1;
      } else if (params2[i].getType().equals(String.class) && !params1[i].getType()
          .equals(String.class)) {
        return 1;
      }
    }
    return 0;
  }

  private static Map<Class<?>, Function<String, Object>> solveParsers() {
    Map<Class<?>, Function<String, Object>> ret = new HashMap<>();
    ret.put(int.class, Integer::parseInt);
    ret.put(long.class, Long::parseLong);
    ret.put(boolean.class, Boolean::parseBoolean);
    ret.put(double.class, Double::parseDouble);
    ret.put(Duration.class, Duration::parse);
    ret.put(ContentType.class, ContentType::parse);
    ret.put(String.class, s -> s);
    return ret;
  }

  @Override
  public Object construct(Node node) {
    Map<String, Node> nodeProperties = getNodeProperties(node, tag);
    Object ret = buildTestElement(nodeProperties, node);
    buildPropertiesFromList(nodeProperties.remove("_propsList"), ret);
    nodeProperties.forEach((propName, propNode) -> callPropertyMethod(propName, propNode, ret));
    return ret;
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

  private void buildPropertiesFromList(Node props, Object ret) {
    if (props == null) {
      return;
    }
    SequenceNode propsList = castNode(props, SequenceNode.class, "list", tag);
    propsList.getValue().forEach(s -> {
      // remove first character that is the tag marker (!)
      String propertyName = s.getTag().getValue().substring(1);
      callPropertyMethod(propertyName, s, ret);
    });
  }

  private void callPropertyMethod(String propName, Node propNode, Object ret) {
    Method propMethod = findPropertyMethod(propName, propNode, ret);
    try {
      Object[] args = buildArgs(propMethod, propNode);
      propMethod.invoke(ret, args);
    } catch (ReflectiveOperationException ex) {
      throw new RuntimeException(ex);
    }
  }

  private Method findPropertyMethod(String propName, Node propNode, Object testElement) {
    List<Method> candidates = Stream.of(testElement.getClass().getMethods())
        .filter(m -> propName.equals(m.getName()))
        // sorted to get the most specific ones in the class hierarchy first
        .sorted((m1, m2) -> {
          Class<?> m1Class = m1.getReturnType();
          Class<?> m2Class = m2.getReturnType();
          return m1Class == m2Class ? 0 : m1Class.isAssignableFrom(m2Class) ? 1 : -1;
        })
        .collect(Collectors.toList());
    if (candidates.isEmpty()) {
      throw new TestElementConstructorException(tag, propNode,
          "could not find a method for setting property " + propName);
    }
    return candidates.stream()
        // prefer the method accepting strings which is more generic (accepts Jmeter expressions).
        .filter(m -> m.getParameters().length >= 1
            && Arrays.stream(m.getParameters())
            .allMatch(p -> String.class.isAssignableFrom(p.getType())))
        .findFirst()
        .orElseGet(() -> candidates.stream()
            /*
            if there is a method receiving just a boolean, then use it, over the one without
            parameters, to avoid improper resolution when parameter is false and method without
            parameters sets the boolean to true
             */
            .filter(m -> m.getParameters().length == 1 && boolean.class.isAssignableFrom(
                m.getParameters()[0].getType()))
            .findFirst()
            /*
             otherwise just get the first one (which is the most specific one when multiple
             implementations are available in class hierarchy)
             */
            .orElseGet(() -> candidates.get(0)));
  }

  private Object[] buildArgs(Method propMethod, Node propNode) {
    Parameter[] params = propMethod.getParameters();
    if (params.length == 0) {
      return new Object[0];
    } else if (params.length == 1) {
      return new Object[]{constructParameter(extractSingleArgNode(propNode, params[0]), params[0])};
    } else {
      Map<String, Node> props = getNodeProperties(propNode, propNode.getTag().getValue());
      return Arrays.stream(propMethod.getParameters())
          .map(p -> constructParameter(props.get(p.getName()), p))
          .toArray();
    }
  }

  private Node extractSingleArgNode(Node propNode, Parameter param) {
    if (propNode instanceof MappingNode) {
      MappingNode mappingNode = (MappingNode) propNode;
      if (mappingNode.getValue().size() == 1 && ((ScalarNode) mappingNode.getValue().get(0)
          .getKeyNode()).getValue().equals(param.getName())) {
        return mappingNode.getValue().get(0).getValueNode();
      }
    }
    return propNode;
  }

}
