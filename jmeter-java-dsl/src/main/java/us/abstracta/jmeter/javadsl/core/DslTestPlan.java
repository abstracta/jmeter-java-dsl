package us.abstracta.jmeter.javadsl.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;
import us.abstracta.jmeter.javadsl.core.engines.JmeterEnvironment;
import us.abstracta.jmeter.javadsl.core.testelements.TestElementContainer;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslThreadGroup;

/**
 * Represents a JMeter test plan, with associated thread groups and other children elements.
 *
 * @since 0.1
 */
public class DslTestPlan extends TestElementContainer<TestPlanChild> {

  private boolean tearDown = true;
  private boolean funcMode = false;
  private boolean serializeTGs = false;

  public DslTestPlan(List<TestPlanChild> children) {
    super("Test Plan", TestPlanGui.class, children);
  }

  @Override
  protected TestElement buildTestElement() {
    TestPlan ret = new TestPlan();
    ret.setUserDefinedVariables(new Arguments());
    ret.setTearDownOnShutdown(this.tearDown);
    ret.setTearDownOnShutdown(this.funcMode);
    ret.setTearDownOnShutdown(this.serializeTGs);
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
   * <p>
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
    JmeterEnvironment env = new JmeterEnvironment();
    try (FileOutputStream output = new FileOutputStream(filePath)) {
      HashTree tree = new ListedHashTree();
      buildTreeUnder(tree, new BuildTreeContext(tree));
      env.saveTree(tree, output);
    }
  }

  /**
   * Loads a test plan from the given JMX to be able to run it in embedded engine.
   *
   * @param filePath specifies the path where the JMX file is located.
   * @return loaded test plan.
   * @throws IOException when there is a problem loading to the file.
   * @since 0.3
   */
  public static DslTestPlan fromJmx(String filePath) throws IOException {
    JmeterEnvironment env = new JmeterEnvironment();
    HashTree tree = env.loadTree(new File(filePath));
    return new JmxTestPlan(tree);
  }

  /**
   * if true, thread groups will start consecutively.
   * One at one time in the order indicated in the plan
   *
   * @param serializeTGs enabling or disabling this feature (true/false).
   * @return this instance for fluent API usage.
   * @since 0.4
   */
  public DslTestPlan setSerialized(Boolean serializeTGs) {
    this.serializeTGs = serializeTGs;
    return this;
  }

  /**
   * If true, it will cause JMeter to record the data returned from
   * the server for each sample.
   * If you have selected a file in your test listeners,
   * this data will be written to file.
   *
   * @param funcMode enabling or disabling this mode (true/false).
   * @return this instance for fluent API usage.
   * @since 0.4
   */
  public DslTestPlan setGlobalFunctionalMode(Boolean funcMode) {
    this.funcMode = funcMode;
    return this;
  }

  /**
   * if true, the tearDown groups (if any)
   * will be run after graceful shutdown of the main threads.
   * The tearDown threads won't be run if the test is forcibly stopped.
   *
   * @param tearDown enabling or disabling this feature (true/false).
   * @return this instance for fluent API usage.
   * @since 0.4
   */
  public DslTestPlan setTearDownOnShutdown(Boolean tearDown) {
    this.tearDown = tearDown;
    return this;
  }

  private static class JmxTestPlan extends DslTestPlan {

    private final HashTree tree;

    private JmxTestPlan(HashTree tree) {
      super(Collections.emptyList());
      this.tree = tree;
    }

    @Override
    public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
      parent.putAll(tree);
      return context.getTestPlanTree();
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
