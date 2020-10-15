package us.abstracta.jmeter.javadsl.core;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;

/**
 * Represents a JMeter test plan, with associated thread groups and other children elements.
 */
public class DslTestPlan extends TestElementContainer<TestPlanChild> {

  public DslTestPlan(List<? extends TestPlanChild> children) {
    super("Test Plan", TestPlanGui.class, children);
  }

  @Override
  protected TestElement buildTestElement() {
    TestPlan ret = new TestPlan();
    ret.setUserDefinedVariables(new Arguments());
    return ret;
  }

  /**
   * Uses {@link EmbeddedJmeterEngine} to run the test plan.
   *
   * @return {@link TestPlanStats} containing all statistics of the test plan execution.
   * @throws IOException thrown when there is some problem running the plan.
   */
  public TestPlanStats run() throws IOException {
    return new EmbeddedJmeterEngine().run(this);
  }

  /**
   * Allows to run the test plan in a given engine.
   *
   * This method is just a simple method which provides fluent API to run the test plans in a given
   * engine.
   *
   * @see DslJmeterEngine#run(DslTestPlan)
   */
  public TestPlanStats runIn(DslJmeterEngine engine)
      throws IOException, InterruptedException, TimeoutException {
    return engine.run(this);
  }

  /**
   * Saves the given test plan as JMX, which allows it to be loaded in JMeter GUI.
   *
   * @param filePath specifies where to store the JMX of the test plan.
   * @throws IOException when there is a problem saving to the file.
   */
  public void saveAsJmx(String filePath) throws IOException {
    EmbeddedJmeterEngine.saveTestPlanToJmx(this, filePath);
  }

  public static DslTestPlan fromJmx(String filePath) throws IOException {
    return EmbeddedJmeterEngine.loadTestPlanFromJmx(filePath);
  }

  public static DslTestPlan fromTree(HashTree tree) {
    return new JmxTestPlan(tree);
  }

  private static class JmxTestPlan extends DslTestPlan {

    private final HashTree tree;

    private JmxTestPlan(HashTree tree) {
      super(null);
      this.tree = tree;
    }

    @Override
    public HashTree buildTreeUnder(HashTree parent) {
      parent.putAll(tree);
      return tree.values().iterator().next();
    }

  }

  /**
   * Test elements that can be added directly as test plan children in JMeter should implement this
   * interface.
   *
   * Check {@link DslThreadGroup} for an example.
   */
  public interface TestPlanChild extends DslTestElement {

  }

}
