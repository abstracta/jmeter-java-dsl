package us.abstracta.jmeter.javadsl.bridge.serialization;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class BridgedObjectDeserializer {

  private static final Logger LOG = LoggerFactory.getLogger(BridgedObjectDeserializer.class);

  private final Yaml yaml = new Yaml(new BridgedObjectConstructor());

  public <T> T deserialize(Reader reader) {
    try {
      if (LOG.isDebugEnabled()) {
        String in = IOUtils.toString(reader);
        LOG.debug("Loading {}", in);
        reader = new StringReader(in);
      }
      return yaml.load(reader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
