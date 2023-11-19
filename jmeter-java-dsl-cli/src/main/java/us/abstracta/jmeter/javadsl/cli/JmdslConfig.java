package us.abstracta.jmeter.javadsl.cli;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class JmdslConfig extends JmdslApplyDefaults {

  public static final String CONFIG_OPTION = "--config";
  public static final String DEFAULT_CONFIG_FILE = ".jmdsl.yml";
  protected static final String PICOCLI_NO_DEFAULT_VALUE_MARKER = "__no_default_value__";

  private RecorderCommand recorder;

  // This is required by jackson for deserialization
  public JmdslConfig() {
  }

  public JmdslConfig(RecorderCommand recorder) {
    this.recorder = recorder;
  }

  public static JmdslConfig fromConfigFile(File configFile) throws IOException {
    if (configFile.getPath().equals(DEFAULT_CONFIG_FILE) && !configFile.exists()) {
      return null;
    }
    return new ObjectMapper(new YAMLFactory())
        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
        .readValue(configFile, JmdslConfig.class);
  }

  public void updateWithDefaultsFrom(JmdslConfig other) {
    if (other == null) {
      return;
    }
    applyDefaultsFromTo(other.recorder, this.recorder);
  }

}
