package us.abstracta.jmeter.javadsl.engines;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.JmeterDsl;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslJmeterEngine;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.engines.JmeterEnvironment;
import us.abstracta.jmeter.javadsl.core.engines.TestStopper;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.AutoStopTestBean;

/**
 * Contains common logic to ease creation of remote engines (like BlazeMeter).
 *
 * @param <C> specifies the type of the class used to interact with the remote engine API.
 * @param <S> specifies the type of test plan stats returned by this engine.
 * @since 1.10
 */
public abstract class BaseRemoteEngine<C extends BaseRemoteEngineApiClient, S extends TestPlanStats>
    implements DslJmeterEngine {

  private static final Logger LOG = LoggerFactory.getLogger(BaseRemoteEngine.class);
  protected C apiClient;

  @Override
  public TestPlanStats run(DslTestPlan testPlan)
      throws IOException, InterruptedException, TimeoutException {
    /*
     Create file within temporary directory instead of just temporary file, to control the name of
     the file, which might later on be relevant or visible for the engine.
     */
    File jmxFile = Files.createTempDirectory("jmeter-dsl").resolve("test.jmx").toFile();
    try (C cli = buildClient()) {
      this.apiClient = cli;
      JmeterEnvironment env = new JmeterEnvironment();
      BuildTreeContext context = BuildTreeContext.buildRemoteExecutionContext();
      context.setTestStopper(buildTestStopper());
      HashTree tree = buildTree(testPlan, context);
      saveTestPlanTo(jmxFile, tree, env);
      return run(jmxFile, tree, context);
    } finally {
      if (jmxFile.delete()) {
        jmxFile.getParentFile().delete();
      }
    }
  }

  /**
   * Executes the given jmx file (generated from a DSL test plan) in the remote engine service.
   * <p>
   * This method needs to be implemented by each remote engine containing common setup y resources
   * resolution logic, test plan upload, test execution start, waiting for test execution to end and
   * test plan statistics retrieval after test execution ends.
   *
   * @param jmxFile specifies the temporary file which contains a JMeter test plan generated from a
   *                JMeter DSL test plan.
   * @param tree    specifies the JMeter tree generated from the JMeter DSL test plan. This is
   *                usually helpful in case some analysis or inspection is required on the test plan
   *                for its remote execution.
   * @param context specifies the build context which can be used to get additional information.
   *                Like file assets used by the test plan to upload to the remote engine.
   * @return the test plan statistics collected by remote engine service.
   * @throws IOException          is thrown when there is some communication problem with the remote
   *                              engine service.
   * @throws InterruptedException is thrown when the user has interrupted the execution of the test
   *                              plan.
   * @throws TimeoutException     is thrown when test plan is taking more time than expected
   *                              executing, or some intermediary phase (test plan validation,
   *                              upload, statistics retrieval, etc) takes more than expected.
   */
  protected abstract S run(File jmxFile, HashTree tree, BuildTreeContext context)
      throws IOException, InterruptedException, TimeoutException;

  /**
   * Builds the API client class that is required for interaction with the remote engine service.
   *
   * @return the API client instance for the remote engine usage.
   */
  protected abstract C buildClient();

  protected TestStopper buildTestStopper() {
    return null;
  }

  protected HashTree buildTree(DslTestPlan testPlan, BuildTreeContext context) {
    HashTree ret = new ListedHashTree();
    context.buildTreeFor(testPlan, ret);
    context.getVisualizers().forEach((v, e) ->
        LOG.warn("This engine does not currently support displaying visualizers. Ignoring {}.",
            v.getClass().getSimpleName())
    );
    return ret;
  }

  private void saveTestPlanTo(File jmxFile, HashTree tree, JmeterEnvironment env)
      throws IOException {
    try (FileOutputStream output = new FileOutputStream(jmxFile.getPath())) {
      env.saveTree(tree, output);
    }
  }

  protected List<File> findDependencies(HashTree tree, BuildTreeContext ctx) {
    Map<Class<?>, File> ret = new HashMap<>();
    findDependencies(tree, ctx, ret);
    return new ArrayList<>(ret.values());
  }

  private void findDependencies(HashTree tree, BuildTreeContext ctx, Map<Class<?>, File> deps) {
    for (Object elem : tree.list()) {
      if (deps.containsKey(elem.getClass())) {
        break;
      }
      if (elem instanceof AutoStopTestBean) {
        addDependency(elem, deps);
        if (ctx.getTestStopper() != null) {
          addDependency(ctx.getTestStopper(), deps);
        }
      } else if (elem.getClass().getPackage().getName()
          .startsWith(JmeterDsl.class.getPackage().getName())) {
        addDependency(elem, deps);
      } else {
        findDependencies(tree.get(elem), ctx, deps);
      }
    }
  }

  private void addDependency(Object elem, Map<Class<?>, File> deps) {
    deps.put(elem.getClass(), getClassJarPath(elem.getClass()));
  }

  private File getClassJarPath(Class<?> theClass) {
    try {
      return new File(theClass.getProtectionDomain().getCodeSource().getLocation().toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected boolean isAutoStoppableTest(HashTree tree) {
    for (Object elem : tree.list()) {
      if (elem instanceof AutoStopTestBean) {
        return true;
      } else {
        if (isAutoStoppableTest(tree.get(elem))) {
          return true;
        }
      }
    }
    return false;
  }

  protected ThreadGroup extractFirstThreadGroup(HashTree tree) {
    HashTree testPlanTree = tree.getTree(tree.list().iterator().next());
    return (ThreadGroup) testPlanTree.list().stream()
        // we don't want to catch subclasses (setup & teardown), only exact class.
        .filter(e -> e.getClass() == ThreadGroup.class)
        .findFirst()
        .orElse(null);
  }

  /**
   * Allows to easily check if a given timeout has expired since a given process start time.
   *
   * @param timeout specifies the duration after the given start, that defines if the associated
   *                process has timed out or not.
   * @param start   specifies the instant when the associated process started.
   * @return true if the time since the given start is greater or equal to given timeout. False
   * otherwise.
   */
  protected boolean hasTimedOut(Duration timeout, Instant start) {
    return Duration.between(start, Instant.now()).compareTo(timeout) >= 0;
  }

  /**
   * Formats given duration into a more human friendly string format.
   *
   * @param duration specifies the duration to be formatted.
   * @return the formatted duration string.
   * @since 1.11
   */
  protected String prettyDuration(Duration duration) {
    String ret = duration.toString().substring(2);
    ret = ret.replaceAll("[HMS]", "$0 ").toLowerCase();
    return ret.substring(0, ret.length() - 1);
  }

}
