package us.abstracta.jmeter.javadsl.core;

import java.io.IOException;
import java.util.List;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;

/**
 * This class represents a JMeter test plan, with associated thread groups and other children
 * elements.
 */
public class DslTestPlan extends TestElementContainer<TestPlanChild> {

  public DslTestPlan(List<? extends TestPlanChild> children) {
    super(null, children);
  }

  @Override
  protected TestElement buildTestElement() {
    return new TestPlan();
  }

  /**
   * This method uses {@link EmbeddedJmeterEngine} to run the test plan.
   *
   * @return {@link TestPlanStats} containing all statistics of the test plan execution.
   * @throws IOException thrown when there is some problem running the plan.
   */
  public TestPlanStats run() throws IOException {
    return new EmbeddedJmeterEngine().run(this);
  }

  /**
   * Test elements that can be added directly as test plan children in JMeter, should implement this
   * interface.
   *
   * Check {@link DslThreadGroup} for an example.
   */
  public interface TestPlanChild extends DslTestElement {

  }

}
