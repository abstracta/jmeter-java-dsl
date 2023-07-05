package us.abstracta.jmeter.javadsl.bridge;

import us.abstracta.jmeter.javadsl.core.DslJmeterEngine;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class TestPlanExecution {

  private final DslJmeterEngine engine;
  private final DslTestPlan testPlan;

  public TestPlanExecution(DslJmeterEngine engine, DslTestPlan testPlan) {
    this.engine = engine;
    this.testPlan = testPlan;
  }

  public DslJmeterEngine getEngine() {
    return engine;
  }

  public DslTestPlan getTestPlan() {
    return testPlan;
  }

}
