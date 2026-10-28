package us.abstracta.jmeter.javadsl.blazemeter.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.threads.ThreadGroup;

public class TestConfig {

  private String name;
  private long projectId;
  private final boolean shouldSendReportEmail = false;
  private final Configuration configuration = new Configuration();
  private final List<ExecutionConfig> overrideExecutions = new ArrayList<>();

  public TestConfig name(String name) {
    this.name = name;
    return this;
  }

  public TestConfig projectId(long projectId) {
    this.projectId = projectId;
    return this;
  }

  public TestConfig jmxFile(File jmxFile) {
    this.configuration.filename = jmxFile.getName();
    return this;
  }

  public TestConfig execConfig(ExecutionConfig config) {
    overrideExecutions.clear();
    overrideExecutions.add(config);
    return this;
  }

  public TestConfig threadsPerEngine(Integer threadsPerEngine) {
    this.configuration.threads = threadsPerEngine;
    return this;
  }

  public TestConfig javaVersion(String javaVersion) {
    configuration.javaVersion = javaVersion;
    return this;
  }

  public TestConfig jmeterVersion(String jmeterVersion) {
    if (jmeterVersion != null) {
      configuration.plugins().jmeter = new JmeterPlugin(jmeterVersion);
    }
    return this;
  }

  public TestConfig jmeterProperties(Map<String, String> properties) {
    if (!properties.isEmpty()) {
      configuration.enableJMeterProperties = true;
      configuration.plugins().remoteControl = properties.entrySet().stream()
          .map(e -> new JmeterProperty(e.getKey(), e.getValue()))
          .collect(Collectors.toList());
    }
    return this;
  }

  public TestConfig networkEmulation(NetworkEmulation networkEmulation) {
    if (networkEmulation != null) {
      configuration.enableNetworkEmulation = true;
      configuration.plugins().network = networkEmulation;
    }
    return this;
  }

  public TestConfig dedicatedIps(Boolean enabled) {
    configuration.dedicatedIpsEnabled = enabled;
    return this;
  }

  private static class Configuration {

    private final String type = "taurus";
    private final String scriptType = "jmeter";
    private String filename;
    private Integer threads;
    @JsonInclude(Include.NON_NULL)
    private String javaVersion;
    @JsonInclude(Include.NON_NULL)
    private Boolean dedicatedIpsEnabled;
    @JsonInclude(Include.NON_NULL)
    private Boolean enableJMeterProperties;
    @JsonInclude(Include.NON_NULL)
    private Boolean enableNetworkEmulation;
    @JsonInclude(Include.NON_NULL)
    private Plugins plugins;

    private Plugins plugins() {
      if (plugins == null) {
        plugins = new Plugins();
      }
      return plugins;
    }

  }

  @JsonInclude(Include.NON_NULL)
  private static class Plugins {

    private JmeterPlugin jmeter;
    private List<JmeterProperty> remoteControl;
    private NetworkEmulation network;

  }

  private static class JmeterPlugin {

    private final String version;

    private JmeterPlugin(String version) {
      this.version = version;
    }

  }

  private static class JmeterProperty {

    private final String key;
    private final String value;

    private JmeterProperty(String key, String value) {
      this.key = key;
      this.value = value;
    }

  }

  @JsonInclude(Include.NON_NULL)
  public static class NetworkEmulation {

    private static final String CUSTOM_PRESET = "Custom";

    private final String preset;
    private final Integer latency;
    private final Integer bandwidth;
    private final Integer packetloss;

    private NetworkEmulation(String preset, Integer latency, Integer bandwidth,
        Integer packetloss) {
      this.preset = preset;
      this.latency = latency;
      this.bandwidth = bandwidth;
      this.packetloss = packetloss;
    }

    public static NetworkEmulation preset(String preset) {
      return new NetworkEmulation(preset, null, null, null);
    }

    public static NetworkEmulation custom(int latencyMillis, int bandwidthKbps,
        int packetLossPercent) {
      return new NetworkEmulation(CUSTOM_PRESET, latencyMillis, bandwidthKbps, packetLossPercent);
    }

  }

  public static class ExecutionConfig {

    private Integer concurrency;
    private String rampUp;
    private Integer iterations;
    private String holdFor;
    private Map<String, Integer> locations;
    private Map<String, Integer> locationsPercents;

    public ExecutionConfig(Integer totalUsers, Duration rampUp, Integer iterations,
        Duration holdFor) {
      concurrency = totalUsers;
      this.rampUp = buildDurationMinutesString(rampUp);
      this.iterations = iterations;
      this.holdFor = buildDurationMinutesString(holdFor);
    }

    private String buildDurationMinutesString(Duration duration) {
      return duration != null ? Math.round(Math.ceil((double) duration.getSeconds() / 60)) + "m"
          : null;
    }

    public static ExecutionConfig fromThreadGroup(ThreadGroup threadGroup) {
      if (threadGroup == null) {
        return new ExecutionConfig(1, Duration.ZERO, null, Duration.ofSeconds(10));
      }
      LoopController loop = (LoopController) threadGroup.getSamplerController();
      return new ExecutionConfig(threadGroup.getNumThreads(),
          Duration.ofSeconds(threadGroup.getRampUp()),
          loop.getLoops() != 0 ? loop.getLoops() : null,
          threadGroup.getDuration() != 0 ? Duration.ofSeconds(threadGroup.getDuration()) : null);
    }

    public void setLocationsPercents(Map<String, Integer> locationsPercents) {
      this.locationsPercents = locationsPercents;
      this.locations = locationsPercents.entrySet().stream()
          .collect(Collectors.toMap(Entry::getKey,
              e -> (int) Math.round(((double) e.getValue()) / 100 * concurrency)));
    }

  }

}
