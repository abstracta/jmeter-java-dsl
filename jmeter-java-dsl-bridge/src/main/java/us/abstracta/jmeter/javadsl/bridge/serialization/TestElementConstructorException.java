package us.abstracta.jmeter.javadsl.bridge.serialization;

import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.nodes.Node;

public class TestElementConstructorException extends ConstructorException {

  public TestElementConstructorException(String elementTypeName, Node node, String problem) {
    super("while constructing a " + elementTypeName, node.getStartMark(),
        problem, node.getEndMark());
  }

}
