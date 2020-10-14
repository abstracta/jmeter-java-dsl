package us.abstracta.jmeter.javadsl.blazemeter.api;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class TestConfig {

  private String name;
  private long projectId;
  private final boolean shouldSendReportEmail = false;
  private final Configuration configuration = new Configuration();
  private final List<Execution> overrideExecutions = Collections.singletonList(new Execution());

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

  public TestConfig totalUsers(Integer totalUsers) {
    this.overrideExecutions.get(0).concurrency = totalUsers;
    return this;
  }

  public TestConfig rampUp(Duration rampUp) {
    this.overrideExecutions.get(0).rampUp = buildDurationMinutesString(rampUp);
    return this;
  }

  private String buildDurationMinutesString(Duration duration) {
    return duration != null ? Math.ceil((double) duration.getSeconds() / 60) + "m" : null;
  }

  public TestConfig iterations(Integer iterations) {
    this.overrideExecutions.get(0).iterations = iterations;
    return this;
  }

  public TestConfig holdFor(Duration holdFor) {
    this.overrideExecutions.get(0).holdFor = buildDurationMinutesString(holdFor);
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

  private static class Execution {

    private Integer concurrency;
    private String rampUp;
    private Integer iterations;
    private String holdFor;

  }

}
