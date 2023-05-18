package us.abstracta.jmeter.javadsl.blazemeter.api;

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

  private static class Configuration {

    private final String type = "taurus";
    private final String scriptType = "jmeter";
    private String filename;
    private Integer threads;

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
