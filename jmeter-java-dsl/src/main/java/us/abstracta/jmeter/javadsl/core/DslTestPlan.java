package us.abstracta.jmeter.javadsl.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingUtilities;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine;
import us.abstracta.jmeter.javadsl.core.engines.EmbeddedJmeterEngine.EmbeddedJMeterEngineStopper;
import us.abstracta.jmeter.javadsl.core.engines.JmeterEnvironment;
import us.abstracta.jmeter.javadsl.core.engines.JmeterGui;
import us.abstracta.jmeter.javadsl.core.testelements.TestElementContainer;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslDefaultThreadGroup;

/**
 * Represents a JMeter test plan, with associated thread groups and other children elements.
 *
 * @since 0.1
 */
public class DslTestPlan extends TestElementContainer<DslTestPlan, TestPlanChild> {

  private static final String DEFAULT_NAME = "Test Plan";
  protected boolean serializeThreadGroups = false;
  protected boolean tearDownOnlyAfterMainThreadsDone = false;

  public DslTestPlan(List<TestPlanChild> children) {
    super(DEFAULT_NAME, TestPlanGui.class, children);
  }

  /**
   * Specifies to run thread groups one after the other, instead of running them in parallel.
   *
   * @return this instance for fluent API usage.
   * @since 0.40
   */
  public DslTestPlan sequentialThreadGroups() {
    return sequentialThreadGroups(true);
  }

  /**
   * Specifies to run thread groups one after the other, instead of running them in parallel.
   * <p>
   * Same as {@link #sequentialThreadGroups()} but allowing to enable/disable the setting during
   * runtime.
   *
   * @param enable specifies to run thread groups one after the other when set to true, or in
   *               parallel when false. By default, is set to false.
   * @return the test plan for further configuration or usage.
   * @since 0.65
   */
  public DslTestPlan sequentialThreadGroups(boolean enable) {
    this.serializeThreadGroups = enable;
    return this;
  }

  /**
   * Allows running tear down thread groups only after main thread groups ends cleanly (due to
   * iterations or time limit).
   * <p>
   * By default, JMeter automatically executes tear down thread groups when a test plan stops due to
   * unscheduled event like sample error when stop test is configured in thread group, invocation of
   * `ctx.getEngine().askThreadsToStop()` in jsr223 element, etc. This method allows to disable such
   * behavior not running teardown thread groups in such cases, which might be helpful if teardown
   * thread group has only to run on clean test plan completion.
   *
   * @return the test plan for further configuration or usage.
   * @since 0.40
   */
  public DslTestPlan tearDownOnlyAfterMainThreadsDone() {
    return tearDownOnlyAfterMainThreadsDone(true);
  }

  /**
   * Allows running tear down thread groups only after main thread groups ends cleanly (due to
   * iterations or time limit).
   * <p>
   * By default, JMeter automatically executes tear down thread groups when a test plan stops due to
   * unscheduled event like sample error when stop test is configured in thread group, invocation of
   * `ctx.getEngine().askThreadsToStop()` in jsr223 element, etc. This method allows to disable such
   * behavior not running teardown thread groups in such cases, which might be helpful if teardown
   * thread group has only to run on clean test plan completion.
   *
   * @return this instance for fluent API usage.
   * @since 0.65
   */
  public DslTestPlan tearDownOnlyAfterMainThreadsDone(boolean enabled) {
    this.tearDownOnlyAfterMainThreadsDone = enabled;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    TestPlan ret = new TestPlan();
    ret.setUserDefinedVariables(new Arguments());
    ret.setTearDownOnShutdown(!this.tearDownOnlyAfterMainThreadsDone);
    ret.setSerialized(this.serializeThreadGroups);
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

  @Override
  public void showInGui() {
    try {
      JmeterEnvironment env = new JmeterEnvironment();
      env.initLocale();
      CompletableFuture<JmeterGui> guiFuture = new CompletableFuture<>();
      /*
       Need to invoke this logic in swing event queue for proper order of execution of events
       and avoid NPE while updating RSyntaxTextArea in test plan load (e.g.: when test plan contains
       an ifController).
       */
      SwingUtilities.invokeAndWait(() -> {
        try {
          JmeterGui gui = new JmeterGui();
          guiFuture.complete(gui);
          HashTree tree = new ListedHashTree();
          new BuildTreeContext().buildTreeFor(this, tree);
          env.updateSearchPath(tree);
          gui.load(tree);
        } catch (IllegalUserActionException | IOException e) {
          throw new RuntimeException(e);
        }
      });
      guiFuture.get().awaitClose();
    } catch (IOException | ExecutionException | InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
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
      BuildTreeContext context = new BuildTreeContext();
      context.setTestStopper(new EmbeddedJMeterEngineStopper());
      context.buildTreeFor(this, tree);
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

  private static class JmxTestPlan extends DslTestPlan {

    private final HashTree tree;

    private JmxTestPlan(HashTree tree) {
      super(Collections.emptyList());
      this.tree = tree;
    }

    @Override
    public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
      parent.putAll(tree);
      HashTree testPlanTree = parent.values().iterator().next();
      children.forEach(c -> context.buildChild(c, testPlanTree));
      return testPlanTree;
    }

  }

  /**
   * Allows adding additional children elements to a test plan.
   * <p>
   * This might be helpful for adding elements to a test plan loaded from a JMX file (like adding
   * htmlReport, resultsTreeVisualizer or dashboard) or if you want to create a test plan and then
   * incrementally adding children to it.
   *
   * @param children elements to add to the test plan.
   * @return the test plan for further usage (run, show, etc).
   * @since 0.56
   */
  public DslTestPlan children(TestPlanChild... children) {
    return super.children(children);
  }

  /**
   * Test elements that can be added directly as test plan children in JMeter should implement this
   * interface.
   * <p>
   * Check {@link DslDefaultThreadGroup} for an example.
   */
  public interface TestPlanChild extends DslTestElement {

  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<TestPlan> {

    public CodeBuilder(List<Method> builderMethods) {
      super(TestPlan.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(TestPlan testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement, "TestPlan");
      return buildMethodCall(new ChildrenParam<>(TestPlanChild[].class))
          .chain("sequentialThreadGroups",
              paramBuilder.boolParam("serialize_threadgroups", false))
          .chain("tearDownOnlyAfterMainThreadsDone",
              paramBuilder.boolParam("tearDown_on_shutdown", true));
    }

  }

}
