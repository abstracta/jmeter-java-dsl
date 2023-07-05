package us.abstracta.jmeter.javadsl.bridge.serialization;

import java.io.Reader;
import org.yaml.snakeyaml.Yaml;

public class BridgedObjectDeserializer {

  private final Yaml yaml = new Yaml(new BridgedObjectConstructor());

  public <T> T deserialize(Reader reader) {
    return yaml.load(reader);
  }

}
