package us.abstracta.jmeter.javadsl.bridge.serialization.constructs;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import us.abstracta.jmeter.javadsl.bridge.serialization.TestElementConstructorException;

public abstract class BaseBridgedObjectConstruct extends AbstractConstruct {

  protected static Map<String, Node> getNodeProperties(Node node, String tag) {
    if (!(node instanceof MappingNode)) {
      throw new TestElementConstructorException(tag, node,
          String.format("found a %s while expecting a map", node.getClass()));
    }
    return ((MappingNode) node).getValue().stream()
        .collect(Collectors.toMap(n -> ((ScalarNode) n.getKeyNode()).getValue(),
            NodeTuple::getValueNode, (u, v) -> u, LinkedHashMap::new));
  }

}
