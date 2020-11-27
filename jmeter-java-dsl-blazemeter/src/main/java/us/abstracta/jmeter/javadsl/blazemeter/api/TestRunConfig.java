package us.abstracta.jmeter.javadsl.blazemeter.api;

public class TestRunConfig {

  private boolean isDebugRun;

  public TestRunConfig debugRun() {
    isDebugRun = true;
    return this;
  }

}
