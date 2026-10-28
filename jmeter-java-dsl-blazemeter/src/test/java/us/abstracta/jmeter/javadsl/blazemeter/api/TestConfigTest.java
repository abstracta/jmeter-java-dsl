package us.abstracta.jmeter.javadsl.blazemeter.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.blazemeter.api.TestConfig.NetworkEmulation;

public class TestConfigTest {

  private final ObjectMapper mapper = new ObjectMapper()
      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

  @Test
  public void shouldNotIncludeOptionalSettingsWhenNotSet() throws Exception {
    TestConfig config = new TestConfig()
        .javaVersion(null)
        .jmeterVersion(null)
        .jmeterProperties(Collections.emptyMap())
        .networkEmulation(null)
        .dedicatedIps(null);
    assertThat(serializeConfiguration(config)).isEqualTo(mapper.readTree(
        "{\"type\":\"taurus\",\"scriptType\":\"jmeter\",\"filename\":null,\"threads\":null}"));
  }

  private JsonNode serializeConfiguration(TestConfig config) {
    return mapper.valueToTree(config).get("configuration");
  }

  @Test
  public void shouldIncludeSettingsWhenSet() throws Exception {
    Map<String, String> props = new LinkedHashMap<>();
    props.put("prop1", "val1");
    props.put("prop2", "val2");
    TestConfig config = new TestConfig()
        .javaVersion("17")
        .jmeterVersion("latest")
        .jmeterProperties(props)
        .networkEmulation(NetworkEmulation.custom(100, 1000, 5))
        .dedicatedIps(true);
    assertThat(serializeConfiguration(config)).isEqualTo(mapper.readTree(
        "{\"type\":\"taurus\",\"scriptType\":\"jmeter\",\"filename\":null,\"threads\":null,"
            + "\"javaVersion\":\"17\",\"dedicatedIpsEnabled\":true,"
            + "\"enableJMeterProperties\":true,\"enableNetworkEmulation\":true,"
            + "\"plugins\":{\"jmeter\":{\"version\":\"latest\"},"
            + "\"remoteControl\":[{\"key\":\"prop1\",\"value\":\"val1\"},"
            + "{\"key\":\"prop2\",\"value\":\"val2\"}],"
            + "\"network\":{\"preset\":\"Custom\",\"latency\":100,\"bandwidth\":1000,"
            + "\"packetloss\":5}}}"));
  }

  @Test
  public void shouldOnlyIncludePresetWhenNetworkEmulationPreset() {
    TestConfig config = new TestConfig()
        .networkEmulation(NetworkEmulation.preset("LTE"));
    assertThat(serializeConfiguration(config).get("plugins"))
        .isEqualTo(mapper.createObjectNode()
            .set("network", mapper.createObjectNode().put("preset", "LTE")));
  }

}
