package us.abstracta.jmeter.javadsl.bridge.serialization.constructs;

import java.util.Map;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import us.abstracta.jmeter.javadsl.http.HttpHeaders;

public class HttpHeadersConstruct extends BaseBridgedObjectConstruct {

  @Override
  public Object construct(Node node) {
    Map<String, Node> properties = getNodeProperties(node, "httpHeaders");
    HttpHeaders ret = new HttpHeaders();
    properties.forEach((key, value) -> ret.header(key, ((ScalarNode) value).getValue()));
    return ret;
  }

}
