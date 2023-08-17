package us.abstracta.jmeter.javadsl;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.assertions.DslJsonAssertion;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion;
import us.abstracta.jmeter.javadsl.core.configs.DslCounter;
import us.abstracta.jmeter.javadsl.core.configs.DslCsvDataSet;
import us.abstracta.jmeter.javadsl.core.configs.DslVariables;
import us.abstracta.jmeter.javadsl.core.controllers.DslForEachController;
import us.abstracta.jmeter.javadsl.core.controllers.DslIfController;
import us.abstracta.jmeter.javadsl.core.controllers.DslOnceOnlyController;
import us.abstracta.jmeter.javadsl.core.controllers.DslRuntimeController;
import us.abstracta.jmeter.javadsl.core.controllers.DslTransactionController;
import us.abstracta.jmeter.javadsl.core.controllers.DslWeightedSwitchController;
import us.abstracta.jmeter.javadsl.core.controllers.DslWhileController;
import us.abstracta.jmeter.javadsl.core.controllers.ForLoopController;
import us.abstracta.jmeter.javadsl.core.controllers.PercentController;
import us.abstracta.jmeter.javadsl.core.engines.AutoStoppedTestException;
import us.abstracta.jmeter.javadsl.core.listeners.AutoStopListener;
import us.abstracta.jmeter.javadsl.core.listeners.DslViewResultsTree;
import us.abstracta.jmeter.javadsl.core.listeners.HtmlReporter;
import us.abstracta.jmeter.javadsl.core.listeners.InfluxDbBackendListener;
import us.abstracta.jmeter.javadsl.core.listeners.JtlWriter;
import us.abstracta.jmeter.javadsl.core.listeners.ResponseFileSaver;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslBoundaryExtractor;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslDebugPostProcessor;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor.PostProcessorScript;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslRegexExtractor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorScript;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorVars;
import us.abstracta.jmeter.javadsl.core.samplers.DslDummySampler;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslDefaultThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslSetupThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslTeardownThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.RpsThreadGroup;
import us.abstracta.jmeter.javadsl.core.timers.DslConstantTimer;
import us.abstracta.jmeter.javadsl.core.timers.DslSynchronizingTimer;
import us.abstracta.jmeter.javadsl.core.timers.DslThroughputTimer;
import us.abstracta.jmeter.javadsl.core.timers.DslUniformRandomTimer;
import us.abstracta.jmeter.javadsl.core.util.PropertyScriptBuilder.PropertyScript;
import us.abstracta.jmeter.javadsl.http.DslAuthManager;
import us.abstracta.jmeter.javadsl.http.DslCacheManager;
import us.abstracta.jmeter.javadsl.http.DslCookieManager;
import us.abstracta.jmeter.javadsl.http.DslHttpDefaults;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;
import us.abstracta.jmeter.javadsl.http.HttpHeaders;
import us.abstracta.jmeter.javadsl.java.DslJsr223Sampler;
import us.abstracta.jmeter.javadsl.java.DslJsr223Sampler.SamplerScript;
import us.abstracta.jmeter.javadsl.util.TestResource;

/**
 * This is the main class to be imported from any code using JMeter DSL.
 * <p>
 * This class contains factory methods to create {@link DslTestElement} instances that allow
 * specifying test plans and associated test elements (samplers, thread groups, listeners, etc.). If
 * you want to support new test elements, then you either add them here (if they are considered to
 * be part of the core of JMeter), or implement another similar class containing only the specifics
 * of the protocol, repository, or grouping of test elements that you want to build (eg, one might
 * implement an Http2JMeterDsl class with only http2 test elements' factory methods).
 * <p>
 * When implement new factory methods consider adding only as parameters the main properties of the
 * test elements (the ones that makes sense to specify in most of the cases). For the rest of
 * parameters (the optional ones), prefer them to be specified as methods of the implemented
 * {@link DslTestElement} for such case, in a similar fashion as Builder Pattern.
 *
 * @since 0.1
 */
public class JmeterDsl {

  private JmeterDsl() {
  }

  /**
   * Builds a new test plan.
   *
   * @param children specifies the list of test elements that compose the test plan.
   * @return the test plan instance.
   * @see DslTestPlan
   */
  public static DslTestPlan testPlan(TestPlanChild... children) {
    return new DslTestPlan(Arrays.asList(children));
  }

  /**
   * Builds a new thread group with a given number of threads &amp; iterations.
   *
   * @param threads    specifies the number of threads to simulate concurrent virtual users.
   * @param iterations specifies the number of iterations that each virtual user will run of
   *                   children elements until it stops.
   *                   <p>
   *                   If you specify -1, then threads will iterate until test plan execution is
   *                   interrupted (you manually stop the running process, there is an error and
   *                   thread group is configured to stop on error, or some other explicit
   *                   termination condition).
   *                   <p>
   *                   <b>Setting this property to -1 is in general not advised</b>, since you
   *                   might inadvertently end up running a test plan without limits consuming
   *                   unnecessary computing power. Prefer specifying a big value as a safe limit
   *                   for iterations or duration instead.
   * @param children   contains the test elements that each thread will execute in each iteration.
   * @return the thread group instance.
   * @see DslDefaultThreadGroup
   */
  public static DslDefaultThreadGroup threadGroup(int threads, int iterations,
      ThreadGroupChild... children) {
    return threadGroup(null, threads, iterations, children);
  }

  /**
   * Same as {@link #threadGroup(int, int, ThreadGroupChild...)} but allowing to set a name on the
   * thread group.
   * <p>
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see #threadGroup(int, int, ThreadGroupChild...)
   */
  public static DslDefaultThreadGroup threadGroup(String name, int threads, int iterations,
      ThreadGroupChild... children) {
    return new DslDefaultThreadGroup(name, threads, iterations, Arrays.asList(children));
  }

  /**
   * Builds a new thread group with a given number of threads &amp; their duration.
   *
   * @param threads  to simulate concurrent virtual users.
   * @param duration to keep each thread running for this period of time. Take into consideration
   *                 that JMeter supports specifying duration in seconds, so if you specify a
   *                 smaller granularity (like milliseconds) it will be rounded up to seconds.
   * @param children contains the test elements that each thread will execute until specified
   *                 duration is reached.
   * @return the thread group instance.
   * @see ThreadGroup
   * @since 0.5
   */
  public static DslDefaultThreadGroup threadGroup(int threads, Duration duration,
      ThreadGroupChild... children) {
    return threadGroup(null, threads, duration, children);
  }

  /**
   * Same as {@link #threadGroup(int, Duration, ThreadGroupChild...)} but allowing to set a name on
   * the thread group.
   * <p>
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see #threadGroup(int, Duration, ThreadGroupChild...)
   * @since 0.5
   */
  public static DslDefaultThreadGroup threadGroup(String name, int threads, Duration duration,
      ThreadGroupChild... children) {
    return new DslDefaultThreadGroup(name, threads, duration, Arrays.asList(children));
  }

  /**
   * Builds a new thread group without any thread configuration.
   * <p>
   * This method should be used as starting point for creating complex test thread profiles (like
   * spike, or incremental tests) in combination with holdFor, rampTo and rampToAndHold
   * {@link DslDefaultThreadGroup} methods.
   *
   * <p>
   * Eg:
   * <pre>{@code
   *  threadGroup()
   *    .rampTo(10, Duration.ofSeconds(10))
   *    .rampTo(5, Duration.ofSeconds(10))
   *    .rampToAndHold(20, Duration.ofSeconds(5), Duration.ofSeconds(10))
   *    .rampTo(0, Duration.ofSeconds(5))
   *    .children(...)
   * }</pre>
   * <p>
   * For complex thread profiles that can't be mapped to JMeter built-in thread group element, the
   * DSL uses <a href="https://jmeter-plugins.org/wiki/UltimateThreadGroup/">Ultimate Thread Group
   * plugin</a>.
   *
   * @return the thread group instance.
   * @since 0.18
   */
  public static DslDefaultThreadGroup threadGroup() {
    return new DslDefaultThreadGroup(null);
  }

  /**
   * Same as {@link #threadGroup()} but allowing to set a name on the thread group.
   * <p>
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see #threadGroup()
   * @since 0.18
   */
  public static DslDefaultThreadGroup threadGroup(String name) {
    return new DslDefaultThreadGroup(name);
  }

  /**
   * Builds a thread group that allows running logic before other thread groups.
   * <p>
   * This is usually used to run some setup logic before the actual test plan logic. In particular
   * logic that needs to be run within the context of JMeter test (eg: requires setting some JMeter
   * property) or needs to be run from same machines as the test plan.
   * <p>
   * Check {@link DslSetupThreadGroup} for more details and configuration options.
   *
   * @param children test elements to be run before any other thread group.
   * @return the setup thread group for further customization or just usage in test plan
   * @see DslSetupThreadGroup
   * @since 0.33
   */
  public static DslSetupThreadGroup setupThreadGroup(ThreadGroupChild... children) {
    return new DslSetupThreadGroup(null, Arrays.asList(children));
  }

  /**
   * Same as {@link #setupThreadGroup(ThreadGroupChild...)} but allowing to set a name on the thread
   * group.
   * <p>
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see DslSetupThreadGroup
   * @since 0.35
   */
  public static DslSetupThreadGroup setupThreadGroup(String name, ThreadGroupChild... children) {
    return new DslSetupThreadGroup(name, Arrays.asList(children));
  }

  /**
   * Builds a setup thread group which allows tuning settings before setting its children.
   * <p>
   * This method allows for example setting the number of iterations and threads to be used by the
   * thread group, before setting children elements.
   *
   * @see DslSetupThreadGroup
   * @since 0.35
   */
  public static DslSetupThreadGroup setupThreadGroup() {
    return new DslSetupThreadGroup(null, Collections.emptyList());
  }

  /**
   * Same as {@link #setupThreadGroup()} but allowing to set a name on the thread group.
   * <p>
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see DslSetupThreadGroup
   * @since 0.35
   */
  public static DslSetupThreadGroup setupThreadGroup(String name) {
    return new DslSetupThreadGroup(name, Collections.emptyList());
  }

  /**
   * Builds a thread group that allows running logic after other thread groups.
   * <p>
   * This is usually used to run some clean up logic after the actual test plan logic. In particular
   * logic that needs to be run within the context of JMeter test (eg: requires setting some JMeter
   * property) or needs to be run from same machines as the test plan.
   * <p>
   * Check {@link DslTeardownThreadGroup} for more details and configuration options.
   *
   * @param children test elements to be run after any other thread group.
   * @return the teardown thread group for further customization or just usage in test plan
   * @see DslTeardownThreadGroup
   * @since 0.33
   */
  public static DslTeardownThreadGroup teardownThreadGroup(ThreadGroupChild... children) {
    return new DslTeardownThreadGroup(null, Arrays.asList(children));
  }

  /**
   * Same as {@link #teardownThreadGroup(ThreadGroupChild...)} but allowing to set a name on the
   * thread group.
   * <p>
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see DslTeardownThreadGroup
   * @since 0.35
   */
  public static DslTeardownThreadGroup teardownThreadGroup(String name,
      ThreadGroupChild... children) {
    return new DslTeardownThreadGroup(name, Arrays.asList(children));
  }

  /**
   * Builds a teardown thread group which allows tuning settings before setting its children.
   * <p>
   * This method allows for example setting the number of iterations and threads to be used by the
   * thread group, before setting children elements.
   *
   * @see DslTeardownThreadGroup
   * @since 0.35
   */
  public static DslTeardownThreadGroup teardownThreadGroup() {
    return new DslTeardownThreadGroup(null, Collections.emptyList());
  }

  /**
   * Same as {@link #teardownThreadGroup()} but allowing to set a name on the thread group.
   * <p>
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see DslTeardownThreadGroup
   * @since 0.35
   */
  public static DslTeardownThreadGroup teardownThreadGroup(String name) {
    return new DslTeardownThreadGroup(name, Collections.emptyList());
  }

  /**
   * Builds a thread group that dynamically adapts thread count and pauses to match a given RPS.
   * <p>
   * Internally this element uses
   * <a href="https://jmeter-plugins.org/wiki/ConcurrencyThreadGroup/">Concurrency Thread Group</a>
   * in combination with <a
   * href="https://jmeter-plugins.org/wiki/ThroughputShapingTimer/">Throughput Shaping Timer</a>.
   * <p>
   * Eg:
   * <pre>{@code
   *  rpsThreadGroup()
   *    .maxThreads(500)
   *    .rampTo(20, Duration.ofSeconds(10))
   *    .rampTo(10, Duration.ofSeconds(10))
   *    .rampToAndHold(1000, Duration.ofSeconds(5), Duration.ofSeconds(10))
   *    .rampTo(0, Duration.ofSeconds(5))
   *    .children(...)
   * }</pre>
   *
   * @return the thread group instance.
   * @see RpsThreadGroup
   * @since 0.26
   */
  public static RpsThreadGroup rpsThreadGroup() {
    return new RpsThreadGroup(null);
  }

  /**
   * Same as {@link #rpsThreadGroup()} but allowing to set a name on the thread group.
   * <p>
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see #rpsThreadGroup()
   * @since 0.26
   */
  public static RpsThreadGroup rpsThreadGroup(String name) {
    return new RpsThreadGroup(name);
  }

  /**
   * Builds a new transaction controller with the given name.
   *
   * @param name     specifies the name to identify the transaction.
   * @param children contains the test elements that will be contained within the transaction.
   * @return the transaction instance.
   * @see DslTransactionController
   * @since 0.14
   */
  public static DslTransactionController transaction(String name, ThreadGroupChild... children) {
    return new DslTransactionController(name, Arrays.asList(children));
  }

  /**
   * Builds an If Controller that allows to conditionally run specified children.
   *
   * @param condition contains an expression that when evaluated to true tells the controller to run
   *                  specified children.
   * @param children  contains the test plan elements to execute when the condition is true.
   * @return the controller instance for further configuration and usage.
   * @see DslIfController
   * @since 0.27
   */
  public static DslIfController ifController(String condition, ThreadGroupChild... children) {
    return new DslIfController(condition, Arrays.asList(children));
  }

  /**
   * Same as {@link #ifController(String, ThreadGroupChild...)} but allowing to use Java type safety
   * and code completion when specifying the condition.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine.
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on some alternative.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see PropertyScript
   * @see #ifController(String, ThreadGroupChild...)
   * @since 0.27
   */
  public static DslIfController ifController(PropertyScript<Boolean> condition,
      ThreadGroupChild... children) {
    return new DslIfController(condition, Arrays.asList(children));
  }

  /**
   * Same as {@link #ifController(PropertyScript, ThreadGroupChild...)} but with support for running
   * at scale in a remote engine.
   * <p>
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on additional steps required to run them at scale in a remote engine.
   *
   * @see PropertyScript
   * @see #ifController(PropertyScript, ThreadGroupChild...)
   * @since 1.14
   */
  public static DslIfController ifController(
      Class<? extends PropertyScript<Boolean>> conditionClass,
      ThreadGroupChild... children) {
    return new DslIfController(conditionClass, Arrays.asList(children));
  }

  /**
   * Builds a While Controller that allows to run specific part of the test plan while a given
   * condition is met in one thread iteration.
   * <p>
   * JMeter generates {@code __jm__while__idx} variable containing the iteration number (0 indexed),
   * which can be helpful in some scenarios.
   *
   * @param condition contains an expression that will be evaluated to identify when to stop
   *                  looping.
   * @param children  contains the test plan elements to execute while the condition is true.
   * @return the controller instance for further configuration and usage.
   * @see DslWhileController
   * @since 0.27
   */
  public static DslWhileController whileController(String condition, ThreadGroupChild... children) {
    return whileController(null, condition, children);
  }

  /**
   * Same as {@link #whileController(String, ThreadGroupChild...)} but allowing to set a name which
   * defines autogenerated variable created by JMeter containing iteration index.
   *
   * @param name      specifies the name to assign to the controller. This variable affects the
   *                  JMeter autogenerated variable {@code __jm__<controllerName>__idx} which holds
   *                  the loop iteration number (starting at 0).
   * @param condition contains an expression that will be evaluated to identify when to stop
   *                  looping.
   * @param children  contains the test plan elements to execute while the condition is true.
   * @return the controller instance for further configuration and usage.
   * @see DslWhileController
   * @see #whileController(String, ThreadGroupChild...)
   * @since 0.27
   */
  public static DslWhileController whileController(String name, String condition,
      ThreadGroupChild... children) {
    return new DslWhileController(name, condition, Arrays.asList(children));
  }

  /**
   * Same as {@link #whileController(String, ThreadGroupChild...)} but allowing to use Java type
   * safety and code completion when specifying the condition.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine.
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on some alternative.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So, make sure that provided logic is thread safe.
   * <p>
   * JMeter generates {@code __jm__while__idx} variable containing the iteration number (0 indexed),
   * which can be helpful in some scenarios.
   *
   * @param condition contains java code that will be evaluated to identify when to stop looping.
   * @param children  contains the test plan elements to execute while the condition is true.
   * @return the controller instance for further configuration and usage.
   * @see PropertyScript
   * @see #whileController(String, ThreadGroupChild...)
   * @since 0.27
   */
  public static DslWhileController whileController(PropertyScript<Boolean> condition,
      ThreadGroupChild... children) {
    return whileController(null, condition, children);
  }

  /**
   * Same as {@link #whileController(PropertyScript, ThreadGroupChild...)} but allowing to set a
   * name which defines autogenerated variable created by JMeter containing iteration index.
   *
   * @param name      specifies the name to assign to the controller. This variable affects the
   *                  JMeter autogenerated variable {@code __jm__<controllerName>__idx} which holds
   *                  the loop iteration number (starting at 0).
   * @param condition contains java code that will be evaluated to identify when to stop looping.
   * @param children  contains the test plan elements to execute while the condition is true.
   * @return the controller instance for further configuration and usage.
   * @see PropertyScript
   * @see #whileController(PropertyScript, ThreadGroupChild...)
   * @since 0.27
   */
  public static DslWhileController whileController(String name, PropertyScript<Boolean> condition,
      ThreadGroupChild... children) {
    return new DslWhileController(name, condition, Arrays.asList(children));
  }

  /**
   * Same as {@link #whileController(PropertyScript, ThreadGroupChild...)} but with support for
   * running at scale in a remote engine.
   * <p>
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on additional steps required to run them at scale in a remote engine.
   *
   * @see PropertyScript
   * @see #whileController(PropertyScript, ThreadGroupChild...)
   * @since 1.14
   */
  public static DslWhileController whileController(
      Class<? extends PropertyScript<Boolean>> conditionClass,
      ThreadGroupChild... children) {
    return whileController(null, conditionClass, children);
  }

  /**
   * Same as {@link #whileController(Class, ThreadGroupChild...)} but allowing to set a name which
   * defines autogenerated variable created by JMeter containing iteration index.
   *
   * @param name specifies the name to assign to the controller. This variable affects the JMeter
   *             autogenerated variable {@code __jm__<controllerName>__idx} which holds the loop
   *             iteration number (starting at 0).
   * @see PropertyScript
   * @see #whileController(PropertyScript, ThreadGroupChild...)
   * @since 1.14
   */
  public static DslWhileController whileController(String name,
      Class<? extends PropertyScript<Boolean>> conditionClass, ThreadGroupChild... children) {
    return new DslWhileController(name, conditionClass, Arrays.asList(children));
  }

  /**
   * Builds a Loop Controller that allows to run specific number of times the given children in each
   * thread group iteration.
   * <p>
   * Eg: if a thread group iterates 3 times and the Loop Controller is configured to 5, then the
   * children elements will run {@code 3*5=15} times for each thread.
   * <p>
   * JMeter generates {@code __jm__for__idx} variable containing the iteration number (0 indexed),
   * which can be helpful in some scenarios.
   *
   * @param count    specifies the number of times to execute the children elements in each thread
   *                 group iteration.
   * @param children contains the test plan elements to execute the given number of times in each
   *                 thread group iteration.
   * @return the controller instance for further configuration and usage.
   * @see ForLoopController
   * @since 0.27
   */
  public static ForLoopController forLoopController(int count, ThreadGroupChild... children) {
    return new ForLoopController(null, String.valueOf(count), Arrays.asList(children));
  }

  /**
   * Same as {@link #forLoopController(int, ThreadGroupChild...)} but allowing to set a name which
   * defines autogenerated variable created by JMeter containing iteration index.
   *
   * @param name     specifies the name to assign to the controller. This variable affects the
   *                 JMeter autogenerated variable {@code __jm__<controllerName>__idx} which holds
   *                 the loop iteration number (starting at 0).
   * @param count    specifies the number of times to execute the children elements in each thread
   *                 group iteration.
   * @param children contains the test plan elements to execute the given number of times in each
   *                 thread group iteration.
   * @return the controller instance for further configuration and usage.
   * @see ForLoopController
   * @see #forLoopController(int, ThreadGroupChild...)
   * @since 0.27
   */
  public static ForLoopController forLoopController(String name, int count,
      ThreadGroupChild... children) {
    return new ForLoopController(name, String.valueOf(count), Arrays.asList(children));
  }

  /**
   * Same as {@link #forLoopController(int, ThreadGroupChild...)} but allowing to use JMeter
   * expressions for number of loops
   * <p>
   * This method allows, for example, to extract from a previous response the number of times to
   * execute some part of the test plan and use it in forLoop with something like <pre>{@code
   * "${LOOPS_COUNT}"}</pre>.
   *
   * @param count    specifies a JMeter expression which evaluates to a number specifying the number
   *                 of times to execute the children elements in each thread group iteration..
   * @param children contains the test plan elements to execute the given number of times in each
   *                 thread group iteration.
   * @return the controller instance for further configuration and usage.
   * @see #forLoopController(int, ThreadGroupChild...)
   * @since 0.46
   */
  public static ForLoopController forLoopController(String count, ThreadGroupChild... children) {
    return new ForLoopController(null, count, Arrays.asList(children));
  }

  /**
   * Same as {@link #forLoopController(String, ThreadGroupChild...)} but allowing to set a name
   * which defines autogenerated variable created by JMeter containing iteration index.
   *
   * @param name     specifies the name to assign to the controller. This variable affects the
   *                 JMeter autogenerated variable {@code __jm__<controllerName>__idx} which holds
   *                 the loop iteration number (starting at 0).
   * @param count    specifies a JMeter expression which evaluates to a number specifying the number
   *                 of times to execute the children elements in each thread group iteration..
   * @param children contains the test plan elements to execute the given number of times in each
   *                 thread group iteration.
   * @return the controller instance for further configuration and usage.
   * @see ForLoopController
   * @see #forLoopController(int, ThreadGroupChild...)
   * @since 0.46
   */
  public static ForLoopController forLoopController(String name, String count,
      ThreadGroupChild... children) {
    return new ForLoopController(name, count, Arrays.asList(children));
  }

  /**
   * Builds a For each controller that iterates over a set of variables and runs a given set of
   * children for each variable in the set.
   * <p>
   * This is usually used in combination with extractors that return more than one variable (like
   * regex extractor with -1 index), to iterate over generated variables.
   * <p>
   * JMeter generates {@code __jm__<iterationVarName>__idx} variable containing the iteration number
   * (0 indexed), which can be helpful in some scenarios.
   *
   * @param varsPrefix       specifies the variable prefix of the set of variables to iterate over.
   *                         This will be suffixed by underscore and the iteration index number (eg:
   *                         my_var_0) to get the actual variable and store its value in a variable
   *                         with name specified by iterationVarName.
   * @param iterationVarName specifies the name of the variable to store the value assigned to the
   *                         variable of current iteration. eg: the value of my_var_0 when first
   *                         iteration and my_var is set as varsPrefix.
   * @param children         contains the test plan elements to execute for the given set of
   *                         variables.
   * @return the controller instance for further configuration and usage.
   * @see DslForEachController
   * @since 0.44
   */
  public static DslForEachController forEachController(String varsPrefix, String iterationVarName,
      ThreadGroupChild... children) {
    return new DslForEachController(iterationVarName, varsPrefix, iterationVarName,
        Arrays.asList(children));
  }

  /**
   * Same as {@link #forEachController(String, String, ThreadGroupChild...)} but allowing to set a
   * name which defines autogenerated variable created by JMeter containing iteration index.
   *
   * @param name             specifies the name to assign to the controller. This variable affects
   *                         the JMeter autogenerated variable {@code __jm__<controllerName>__idx}
   *                         which holds the loop iteration number (starting at 0).
   * @param varsPrefix       specifies the variable prefix of the set of variables to iterate over.
   *                         This will be suffixed by underscore and the iteration index number (eg:
   *                         my_var_0) to get the actual variable and store its value in a variable
   *                         with name specified by iterationVarName.
   * @param iterationVarName specifies the name of the variable to store the value assigned to the
   *                         variable of current iteration. eg: the value of my_var_0 when first
   *                         iteration and my_var is set as varsPrefix.
   * @param children         contains the test plan elements to execute the given number of times in
   *                         each thread group iteration.
   * @return the controller instance for further configuration and usage.
   * @see DslForEachController
   * @see #forEachController(String, String, ThreadGroupChild...)
   * @since 0.44
   */
  public static DslForEachController forEachController(String name, String varsPrefix,
      String iterationVarName, ThreadGroupChild... children) {
    return new DslForEachController(name, varsPrefix, iterationVarName, Arrays.asList(children));
  }

  /**
   * Builds a Once Only Controller that allows running a part of a test plan only once and only on
   * the first iteration of each thread group.
   * <p>
   * Eg: if a thread group iterates 3 times and contains few samplers inside the Once Only
   * Controller, then children elements will run 1 time for each thread.
   *
   * @param children contains the test plan elements to execute only one time on first iteration of
   *                 each thread group.
   * @return the controller instance for further configuration and usage.
   * @see DslOnceOnlyController
   * @since 0.34
   */
  public static DslOnceOnlyController onceOnlyController(ThreadGroupChild... children) {
    return new DslOnceOnlyController(Arrays.asList(children));
  }

  /**
   * Builds a Runtime Controller that stops executing child elements when a period of time expires.
   * <p>
   * This can be very useful to implement some time based logic like refreshing a token after
   * certain time while doing http requests (runtimeController + whileController).
   * <p>
   * The controller executes each child sequentially until there are no more children to execute or
   * the time has expired.
   * <p>
   * Child elements execution is not interrupted, the controller just checks on each child execution
   * ending if the time has expired, and if so, don't execute the rest of its child elements.
   *
   * @param duration defines de duration after which no further child elements will be executed (on
   *                 each iteration). Take into consideration that JMeter supports specifying
   *                 duration in seconds, so if you specify a smaller granularity (like
   *                 milliseconds) it will be rounded up to seconds.
   * @param children contains the child elements to execute.
   * @return the controller instance for further configuration and usage.
   * @see DslRuntimeController
   * @since 1.1
   */
  public static DslRuntimeController runtimeController(Duration duration,
      ThreadGroupChild... children) {
    return new DslRuntimeController(String.valueOf(duration.getSeconds()), Arrays.asList(children));
  }

  /**
   * Same as {@link #runtimeController(Duration, ThreadGroupChild...)} but allowing to use JMeter
   * expressions for the duration.
   *
   * @param seconds  defines a JMeter expression that evaluates to a number of seconds the runtime
   *                 controller will execute child elements for.
   * @param children contains the child elements to execute.
   * @return the controller instance for further configuration and usage.
   * @see #runtimeController(Duration, ThreadGroupChild...)
   * @see DslRuntimeController
   * @since 1.1
   */
  public static DslRuntimeController runtimeController(String seconds,
      ThreadGroupChild... children) {
    return new DslRuntimeController(seconds, Arrays.asList(children));
  }

  /**
   * Builds a Percent Controller to execute children only a given percent of times.
   * <p>
   * Internally, this uses a JMeter Throughput Controller with executions percentage configuration.
   *
   * @param percent  defines a number between 0 and 100 that defines the percentage of times to
   *                 execute given children elements.
   * @param children holds test plan elements to execute when for the given percent of times.
   * @return the controller instance for further configuration and usage.
   * @see PercentController
   * @since 0.25
   */
  public static PercentController percentController(float percent, ThreadGroupChild... children) {
    return new PercentController(String.valueOf(percent), Arrays.asList(children));
  }

  /**
   * Same as {@link #percentController(float, ThreadGroupChild...)} but allowing using JMeter
   * expressions (eg: ${PERCENT_VAR}) in percent parameter.
   * <p>
   * This might be useful to set percents according to different profiles, for example, using
   * different percents in production vs staging tests
   *
   * @param percent  defines a JMeter expression that evaluates to a number between 0 and 100 that
   *                 defines the percentage of times to execute given children elements.
   * @param children holds test plan elements to execute when for the given percent of times.
   * @return the controller instance for further configuration and usage.
   * @since 0.63
   */
  public static PercentController percentController(String percent, ThreadGroupChild... children) {
    return new PercentController(percent, Arrays.asList(children));
  }

  /**
   * Builds a Controller which executes in each iteration choosing one child according to given
   * weights.
   * <p>
   * Internally this uses <a
   * href="https://github.com/Blazemeter/jmeter-bzm-plugins/blob/master/wsc/WeightedSwitchController.md">
   * BlazeMeter Weighted Switch Controller plugin</a>.
   *
   * @return the controller instance for further configuration and usage.
   * @see DslWeightedSwitchController
   * @since 0.53
   */
  public static DslWeightedSwitchController weightedSwitchController() {
    return new DslWeightedSwitchController();
  }

  /**
   * Builds an HTTP Request sampler to sample HTTP requests.
   *
   * @param url specifies URL the HTTP Request sampler will hit.
   * @return the HTTP Request sampler instance which can be used to define additional settings for
   * the HTTP request (like method, body, headers, pre &amp; post processors, etc.).
   * @see DslHttpSampler
   */
  public static DslHttpSampler httpSampler(String url) {
    return httpSampler(null, url);
  }

  /**
   * Builds an HTTP Request sampler to sample HTTP requests with a dynamically calculated URL.
   * <p>
   * This method is just an abstraction that uses jexl2 function as URL.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine.
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on some alternative.
   *
   * @param urlSupplier specifies URL the HTTP Request sampler will hit.
   * @return the HTTP Request sampler instance which can be used to define additional settings for
   * the HTTP request (like method, body, headers, pre &amp; post processors, etc.).
   * @see DslHttpSampler
   * @since 0.10
   */
  public static DslHttpSampler httpSampler(Function<PreProcessorVars, String> urlSupplier) {
    return httpSampler(null, urlSupplier);
  }

  /**
   * Same as {@link #httpSampler(String)} but allowing to set a name to the HTTP Request sampler.
   * <p>
   * Setting a proper name allows to easily identify the requests generated by this sampler and
   * check its particular statistics.
   *
   * @see #httpSampler(String)
   */
  public static DslHttpSampler httpSampler(String name, String url) {
    return new DslHttpSampler(name, url);
  }

  /**
   * Same as {@link #httpSampler(Function)} but allowing to set a name to the HTTP Request sampler.
   * <p>
   * Setting a proper name allows to easily identify the requests generated by this sampler and
   * check its particular statistics.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine.
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on some alternative.
   *
   * @see #httpSampler(Function)
   * @since 0.10
   */
  public static DslHttpSampler httpSampler(String name,
      Function<PreProcessorVars, String> urlSupplier) {
    return new DslHttpSampler(name, urlSupplier);
  }

  /**
   * Same as {@link #httpSampler(Function)} but with support for running at scale in a remote
   * engine.
   * <p>
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on additional steps required to run them at scale in a remote engine.
   *
   * @see #httpSampler(Function)
   * @since 1.14
   */
  public static DslHttpSampler httpSampler(Class<? extends PropertyScript<String>> urlSolverClass) {
    return httpSampler(null, urlSolverClass);
  }

  /**
   * Same as {@link #httpSampler(Class)} but allowing to set a name to the HTTP Request sampler.
   * <p>
   * Setting a proper name allows to easily identify the requests generated by this sampler and
   * check its particular statistics.
   *
   * @see #httpSampler(Class)
   * @since 1.14
   */
  public static DslHttpSampler httpSampler(String name,
      Class<? extends PropertyScript<String>> urlSolverClass) {
    return new DslHttpSampler(name, urlSolverClass);
  }

  /**
   * Builds an HTTP header manager which allows setting HTTP headers to be used by HTTPRequest
   * samplers.
   *
   * @return the HTTP header manager instance which allows specifying the particular HTTP headers to
   * use.
   * @see HttpHeaders
   */
  public static HttpHeaders httpHeaders() {
    return new HttpHeaders();
  }

  /**
   * Builds an HTTP request defaults element that allows setting default values used by HTTP
   * samplers.
   * <p>
   * In general, prefer using Java variables or custom builder methods to abstract common logic for
   * samplers which allows for easier debugging, readability and traceability. In some cases though
   * it might be shorter/simpler to just use and httpDefaults element.
   *
   * @return the HTTP defaults test element for customization and usage.
   * @since 0.39
   */
  public static DslHttpDefaults httpDefaults() {
    return new DslHttpDefaults();
  }

  /**
   * Builds a Cookie manager at the test plan level which allows configuring cookies settings used
   * by HTTPRequest samplers.
   *
   * @return the Cookie manager instance which allows configuring cookies settings.
   * @see DslCookieManager
   * @since 0.17
   */
  public static DslCookieManager httpCookies() {
    return new DslCookieManager();
  }

  /**
   * Builds a Cache manager at the test plan level which allows configuring caching behavior used by
   * HTTPRequest samplers.
   *
   * @return the Cache manager instance which allows configuring caching settings.
   * @see DslCacheManager
   * @since 0.17
   */
  public static DslCacheManager httpCache() {
    return new DslCacheManager();
  }

  /**
   * Builds an Auth manager which simplifies configuration for automatic authentication of HTTP
   * requests.
   *
   * @return the Auth manager instance which allows to specify different authentications for
   * different URLs.
   * @see DslAuthManager
   * @since 1.5
   */
  public static DslAuthManager httpAuth() {
    return new DslAuthManager();
  }

  /**
   * Builds a DslVariables that allows to easily initialize or update JMeter variables.
   * <p>
   * This internally uses User Defined Variables when placed as test plan child and JSR223 Samplers
   * otherwise.
   *
   * @return the DslVariables instance to define variables values and using it in a test plan.
   * @see DslVariables
   * @since 0.50
   */
  public static DslVariables vars() {
    return new DslVariables();
  }

  /**
   * Builds a JSR223 Sampler which allows sampling any Java API or custom logic.
   * <p>
   *
   * @param script contains the script to be use while sampling. By default, this will be a groovy
   *               script, but you can change it by setting the language property in the returned
   *               post processor.
   * @return the JSR223 Sampler instance
   * @see DslJsr223Sampler
   * @since 0.22
   */
  public static DslJsr223Sampler jsr223Sampler(String script) {
    return jsr223Sampler(null, script);
  }

  /**
   * Same as {@link #jsr223Sampler(String)} but allowing to set a name on the sampler.
   * <p>
   * The name is used in collected samples to easily identify their results and as logger name which
   * allows configuring log level, appender, etc., for the sampler.
   *
   * @see #jsr223Sampler(String)
   * @since 0.22
   **/
  public static DslJsr223Sampler jsr223Sampler(String name, String script) {
    return new DslJsr223Sampler(name, script);
  }

  /**
   * Same as {@link #jsr223Sampler(String)} but allowing to use Java type safety and code completion
   * when specifying the script.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine.
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on some alternative.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see SamplerScript
   * @see #jsr223Sampler(String)
   * @since 0.22
   */
  public static DslJsr223Sampler jsr223Sampler(SamplerScript script) {
    return jsr223Sampler(null, script);
  }

  /**
   * Same as {@link #jsr223Sampler(String, String)} but allowing to use Java type safety and code
   * completion when specifying the script.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine.
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on some alternative.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see SamplerScript
   * @see #jsr223Sampler(String)
   * @since 0.22
   */
  public static DslJsr223Sampler jsr223Sampler(String name, SamplerScript script) {
    return new DslJsr223Sampler(name, script);
  }

  /**
   * Same as {@link #jsr223Sampler(SamplerScript)} but with support for running at scale in a remote
   * engine.
   * <p>
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on additional steps required to run them at scale in a remote engine.
   *
   * @see SamplerScript
   * @see #jsr223Sampler(SamplerScript)
   * @since 1.14
   */
  public static DslJsr223Sampler jsr223Sampler(Class<? extends SamplerScript> scriptClass) {
    return jsr223Sampler(null, scriptClass);
  }

  /**
   * Same as {@link #jsr223Sampler(Class)} but allowing to set a name on the sampler.
   * <p>
   * The name is used in collected samples to easily identify their results and as logger name which
   * allows configuring log level, appender, etc., for the sampler.
   *
   * @see #jsr223Sampler(Class)
   * @since 1.14
   **/
  public static DslJsr223Sampler jsr223Sampler(String name,
      Class<? extends SamplerScript> scriptClass) {
    return new DslJsr223Sampler(name, scriptClass);
  }

  /**
   * Builds a JMeter plugin Dummy Sampler which allows emulating a sampler easing testing other
   * parts of a test plan (like extractors, controllers conditions, etc).
   * <p>
   * Usually you would replace an existing sampler with this one, to test some extractor or test
   * plan complex behavior (like controllers conditions), and once you have verified that the rest
   * of the plan works as expected, you place back the original sampler that makes actual
   * interactions to a server.
   * <p>
   * By default, this sampler, in contrast to the JMeter plugin Dummy Sampler, does not simulate
   * response time. This helps speeding up the debug and tracing process while using it.
   *
   * @param responseBody specifies the response body to be included in generated sample results.
   * @return the dummy sampler for further configuration and usage in test plan.
   * @see DslDummySampler
   * @since 0.46
   */
  public static DslDummySampler dummySampler(String responseBody) {
    return dummySampler(null, responseBody);
  }

  /**
   * Same as {@link #dummySampler(String)} but allowing to set a name on the sampler.
   * <p>
   * Setting the name of the sampler allows better simulation the final use case when dummy sampler
   * is replaced by actual/final sampler, when sample results are reported in stats, logs, etc.
   *
   * @see DslDummySampler
   * @see #dummySampler(String)
   * @since 0.46
   */
  public static DslDummySampler dummySampler(String name, String responseBody) {
    return new DslDummySampler(name, responseBody);
  }

  /**
   * Builds a JSR223 Pre Processor which allows including custom logic to modify requests.
   * <p>
   * This preprocessor is very powerful, and lets you alter request parameters, jmeter context and
   * implement any kind of custom logic that you may think.
   *
   * @param script contains the script to be executed by the preprocessor. By default, this will be
   *               a groovy script, but you can change it by setting the language property in the
   *               returned post processor.
   * @return the JSR223 Pre Processor instance
   * @see DslJsr223PreProcessor
   * @since 0.7
   */
  public static DslJsr223PreProcessor jsr223PreProcessor(String script) {
    return jsr223PreProcessor(null, script);
  }

  /**
   * Same as {@link #jsr223PreProcessor(String)} but allowing to set a name on the preprocessor.
   * <p>
   * The name is used as logger name which allows configuring log level, appender, etc., for the
   * preprocessor.
   *
   * @see #jsr223PreProcessor(String)
   * @since 0.9
   **/
  public static DslJsr223PreProcessor jsr223PreProcessor(String name, String script) {
    return new DslJsr223PreProcessor(name, script);
  }

  /**
   * Same as {@link #jsr223PreProcessor(String)} but allowing to use Java type safety and code
   * completion when specifying the script.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine.
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on some alternative.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see PreProcessorScript
   * @see #jsr223PreProcessor(String)
   * @since 0.10
   */
  public static DslJsr223PreProcessor jsr223PreProcessor(PreProcessorScript script) {
    return jsr223PreProcessor(null, script);
  }

  /**
   * Same as {@link #jsr223PreProcessor(String, String)} but allowing to use Java type safety and
   * code completion when specifying the script.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine.
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on some alternative.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see PreProcessorScript
   * @see #jsr223PreProcessor(String)
   * @since 0.10
   */
  public static DslJsr223PreProcessor jsr223PreProcessor(String name, PreProcessorScript script) {
    return new DslJsr223PreProcessor(name, script);
  }

  /**
   * Same as {@link #jsr223PreProcessor(PreProcessorScript)} but with support for running at scale
   * in a remote engine.
   * <p>
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on additional steps required to run them at scale in a remote engine.
   *
   * @see PreProcessorScript
   * @see #jsr223PreProcessor(PreProcessorScript)
   * @since 1.14
   */
  public static DslJsr223PreProcessor jsr223PreProcessor(
      Class<? extends PreProcessorScript> scriptClass) {
    return jsr223PreProcessor(null, scriptClass);
  }

  /**
   * Same as {@link #jsr223PreProcessor(Class)} but allowing to set a name on the sampler.
   * <p>
   * The name is used in collected samples to easily identify their results and as logger name which
   * allows configuring log level, appender, etc., for the sampler.
   *
   * @see #jsr223PreProcessor(Class)
   * @since 1.14
   **/
  public static DslJsr223PreProcessor jsr223PreProcessor(String name,
      Class<? extends PreProcessorScript> scriptClass) {
    return new DslJsr223PreProcessor(name, scriptClass);
  }

  /**
   * Builds a Regex Extractor which allows using regular expressions to extract different parts of a
   * sample result (request or response).
   * <p>
   * This method provides a simple default implementation with required settings, but more settings
   * are provided by returned DslRegexExtractor.
   * <p>
   * By default, when regex is not matched, no variable will be created or modified. On the other
   * hand when the regex matches it will by default store the first capturing group (part of
   * expression between parenthesis) of the first match for the regular expression.
   *
   * @param variableName is the name of the variable to be used to store the extracted value to.
   *                     Additional variables {@code <variableName>_g<groupId>} will be created for
   *                     each regular expression capturing group (segment of regex between
   *                     parenthesis), being the group 0 the entire match of the regex. {@code
   *                     <variableName>_g} variable contains the number of matched capturing groups
   *                     (not counting the group 0).
   * @param regex        regular expression used to extract part of request or response.
   * @return the Regex Extractor which can be used to define additional settings to use when
   * extracting (like defining match number, template, etc.).
   * @see DslRegexExtractor
   * @since 0.8
   */
  public static DslRegexExtractor regexExtractor(String variableName, String regex) {
    return new DslRegexExtractor(variableName, regex);
  }

  /**
   * Builds a Boundary Extractor which allows using left and right boundary texts to extract
   * different parts of a sample result (request or response).
   * <p>
   * This method provides a simple default implementation with required settings, but more settings
   * are provided by returned DslBoundaryExtractor.
   * <p>
   * By default, when no match is found, no variable will be created or modified. On the other hand,
   * when a match is found, it will by default store the first match.
   *
   * @param variableName  is the name of the variable to be used to store the extracted value to.
   * @param leftBoundary  specifies text preceding the text to be extracted.
   * @param rightBoundary specifies text following the text to be extracted.
   * @return the Boundary Extractor which can be used to define additional settings to use when
   * extracting (like defining match number, targetField, etc.).
   * @see DslBoundaryExtractor
   * @since 0.28
   */
  public static DslBoundaryExtractor boundaryExtractor(String variableName, String leftBoundary,
      String rightBoundary) {
    return new DslBoundaryExtractor(variableName, leftBoundary, rightBoundary);
  }

  /**
   * Builds an Extractor which allows using JMESPath or JSONPath to extract part of a JSON
   * response.
   * <p>
   * This method provides a simple default implementation with required settings, but more settings
   * are provided by returned DslJsonExtractor.
   * <p>
   * By default, uses JMESPath for queries and when no match is found, no variable will be created
   * or modified. On the other hand, when a match is found, it will by default store the first
   * match.
   *
   * @param variableName is the name of the variable to be used to store the extracted value to.
   * @param query        specifies JMESPath (or JSONPath, if queryLanguage specifies so) to extract
   *                     the value.
   * @return the JSON Extractor which can be used to define additional settings to use when
   * extracting (like defining match number, scope, etc.).
   * @see DslJsonExtractor
   * @since 0.28
   */
  public static DslJsonExtractor jsonExtractor(String variableName, String query) {
    return new DslJsonExtractor(variableName, query);
  }

  /**
   * Builds a Debug post processor which is helpful to collect debugging information from test plans
   * executions.
   * <p>
   * This element is particularly helpful to collect JMeter variables and debug extractors during or
   * after a test plan execution.
   * <p>
   * Created Debug post processor is configured by default to only include JMeter variables, which
   * covers most common usages and keeps memory and disk usage low.
   *
   * @return the post processor for further configuration and usage in test plan.
   * @see DslDebugPostProcessor
   * @since 0.47
   */
  public static DslDebugPostProcessor debugPostProcessor() {
    return new DslDebugPostProcessor();
  }

  /**
   * Builds a JSR223 Post Processor which allows including custom logic to process sample results.
   * <p>
   * This post processor is very powerful, and lets you alter sample results, jmeter context and
   * implement any kind of custom logic that you may think.
   *
   * @param script contains the script to be executed by the post processor. By default, this will
   *               be a groovy script, but you can change it by setting the language property in the
   *               returned post processor.
   * @return the JSR223 Post Processor instance
   * @see DslJsr223PostProcessor
   * @since 0.6
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(String script) {
    return jsr223PostProcessor(null, script);
  }

  /**
   * Same as {@link #jsr223PostProcessor(String)} but allowing to set a name on the post processor.
   * <p>
   * The name is used as logger name which allows configuring log level, appender, etc., for the
   * post processor.
   *
   * @see #jsr223PostProcessor(String)
   * @since 0.9
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(String name, String script) {
    return new DslJsr223PostProcessor(name, script);
  }

  /**
   * Same as {@link #jsr223PostProcessor(String)} but allowing to use Java type safety and code
   * completion when specifying the script.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine.
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on some alternative.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see PostProcessorScript
   * @see #jsr223PostProcessor(String)
   * @since 0.10
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(PostProcessorScript script) {
    return jsr223PostProcessor(null, script);
  }

  /**
   * Same as {@link #jsr223PostProcessor(String, String)} but allowing to use Java type safety and
   * code completion when specifying the script.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine.
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on some alternative.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see PostProcessorScript
   * @see #jsr223PostProcessor(String, String)
   * @since 0.10
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(String name,
      PostProcessorScript script) {
    return new DslJsr223PostProcessor(name, script);
  }

  /**
   * Same as {@link #jsr223PostProcessor(PostProcessorScript)} but with support for running at scale
   * in a remote engine.
   * <p>
   * Check <a href="https://abstracta.github.io/jmeter-java-dsl/guide/#lambdas">the user guide</a>
   * for details on additional steps required to run them at scale in a remote engine.
   *
   * @see PostProcessorScript
   * @see #jsr223PostProcessor(PostProcessorScript)
   * @since 1.14
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(
      Class<? extends PostProcessorScript> scriptClass) {
    return jsr223PostProcessor(null, scriptClass);
  }

  /**
   * Same as {@link #jsr223PostProcessor(Class)} but allowing to set a name on the sampler.
   * <p>
   * The name is used in collected samples to easily identify their results and as logger name which
   * allows configuring log level, appender, etc., for the sampler.
   *
   * @see #jsr223PostProcessor(Class)
   * @since 1.14
   **/
  public static DslJsr223PostProcessor jsr223PostProcessor(String name,
      Class<? extends PostProcessorScript> scriptClass) {
    return new DslJsr223PostProcessor(name, scriptClass);
  }

  /**
   * Builds a Response Assertion to be able to check that obtained sample result is the expected
   * one.
   * <p>
   * JMeter by default uses repose codes (eg: 4xx and 5xx HTTP response codes are error codes) to
   * determine if a request was success or not, but in some cases this might not be enough or
   * correct. In some cases applications might not behave in this way, for example, they might
   * return a 200 HTTP status code but with an error message in the body, or the response might be a
   * success one, but the information contained within the response is not the expected one to
   * continue executing the test. In such scenarios you can use response assertions to properly
   * verify your assumptions before continuing with next request in the test plan.
   * <p>
   * By default, response assertion will use the response body of the main sample result (not sub
   * samples as redirects, or embedded resources) to check the specified criteria (substring match,
   * entire string equality, contained regex or entire regex match) against.
   *
   * @return the created Response Assertion which should be modified to apply the proper criteria.
   * Check {@link DslResponseAssertion} for all available options.
   * @see DslResponseAssertion
   * @since 0.11
   */
  public static DslResponseAssertion responseAssertion() {
    return new DslResponseAssertion(null);
  }

  /**
   * Same as {@link #responseAssertion()} but allowing to set a name on the assertion, which can be
   * later used to identify assertion results and differentiate it from other assertions.
   *
   * @param name is the name to be assigned to the assertion
   * @return the created Response Assertion which should be modified to apply the proper criteria.
   * Check {@link DslResponseAssertion} for all available options.
   * @see #responseAssertion(String)
   * @since 0.11
   */
  public static DslResponseAssertion responseAssertion(String name) {
    return new DslResponseAssertion(name);
  }

  /**
   * Builds an Assertion element to check that obtained sampler result JSON is the expected one.
   * <p>
   * This method simplifies checking JSON responses over {@link #responseAssertion()} alternative.
   * <p>
   * By default, uses JMESPath for queries and just checks for existence of specified element. Check
   * {@link DslJsonAssertion} for additional options.
   *
   * @param jsonQuery specifies JMESPath (or JSONPath, if queryLanguage specifies so) to check the
   *                  existence of given element in sample result response. the value.
   * @return the Assertion which can be used to configure further settings (like query Language,
   * check if value in path matches regex, etc.) and used in a test plan.
   * @see DslJsonAssertion
   * @since 1.15
   */
  public static DslJsonAssertion jsonAssertion(String jsonQuery) {
    return jsonAssertion(null, jsonQuery);
  }

  /**
   * Same as {@link #jsonAssertion(String)} but allowing to set a name on the assertion, which can
   * be later used to identify assertion results and differentiate it from other assertions.
   *
   * @see #jsonAssertion(String)
   * @see DslJsonAssertion
   * @since 1.15
   */
  public static DslJsonAssertion jsonAssertion(String name, String jsonQuery) {
    return new DslJsonAssertion(name, jsonQuery);
  }

  /**
   * Builds an AutoStopListener that allows to stop a test plan execution if some condition over
   * collected metrics is met.
   * <p>
   * This element is useful to avoid waisting resources  (for instance execution costs associated to
   * remote engine execution time) and time when a test plan execution is not getting an expected
   * behavior.
   * <p>
   * When a condition is met, the test plan is stopped and a {@link AutoStoppedTestException} is
   * thrown, containing as message an indicator for the auto stop listener name and the condition
   * that triggered the stop.
   * <p>
   * As with other listeners, the location of the listener within the plan determines which samples
   * will be taken into consideration by the listener (eg: if listener is placed as a child of a
   * sampler, then only that sampler will be evaluated).
   * <p>
   * Note: This element does not use AutoStop listener plugin, but is inspired on it. One important
   * difference is that this element by default evaluates conditions on every sample and does not
   * reset aggregations for every second. If you need to configure similar behavior as AutoStop
   * listener plugin, then you can use
   * {@link
   * us.abstracta.jmeter.javadsl.core.listeners.AutoStopListener.AggregatedConditionBuilder#every}
   * and
   * {@link us.abstracta.jmeter.javadsl.core.listeners.AutoStopListener.AutoStopCondition#holdsFor}
   *
   * @return the listener for further configuration or usage in test plan.
   * @throws AutoStoppedTestException when test plan is stopped by a met condition.
   * @see AutoStopListener
   * @since 1.19
   */
  public static AutoStopListener autoStop() {
    return autoStop(null);
  }

  /**
   * Same as {@link #autoStop()} but allowing to set a name on the listener, which can help identify
   * which autoStop listener stopped a test plan if multiple ones are configured.
   *
   * @see #autoStop()
   * @see AutoStopListener
   * @since 1.19
   */
  public static AutoStopListener autoStop(String name) {
    return new AutoStopListener(name);
  }

  /**
   * Builds a Simple Data Writer to write all collected results to a JTL file.
   * <p>
   * This is just a handy short way of generating JTL files using as filename the template:
   * <pre>{@code <yyyy-MM-dd HH-mm-ss> <UUID>.jtl}</pre>
   * <p>
   * If you need to have a predictable name, consider using {@link #jtlWriter(String, String)}
   * instead.
   *
   * @param directory specifies the directory path where jtl files will be generated in. If the
   *                  directory does not exist, then it will be created.
   * @return the JtlWriter instance.
   * @see #jtlWriter(String, String)
   * @see JtlWriter
   */
  public static JtlWriter jtlWriter(String directory) {
    return new JtlWriter(directory, null);
  }

  /**
   * Builds a Simple Data Writer to write all collected results to a JTL file.
   * <p>
   * This is particularly helpful when you need to control de file name to do later post-processing
   * on the file (eg: use CI build ID in the file name).
   *
   * @param directory specifies the directory path where jtl file will be generated. If the
   *                  directory does not exist, then it will be created.
   * @param fileName  the name to be used for the file. <b>File names should be unique, otherwise
   *                  the new results will be appended to existing file.</b>
   * @return the JtlWriter instance.
   * @since 1.0
   */
  public static JtlWriter jtlWriter(String directory, String fileName) {
    return new JtlWriter(directory, fileName);
  }

  /**
   * Builds a Response File Saver to generate a file for each response of a sample.
   *
   * @param fileNamePrefix the prefix to be used when generating the files. This should contain the
   *                       directory location where the files should be generated and can contain a
   *                       file name prefix for all file names (eg: target/response-files/response-
   *                       ).
   * @return the ResponseFileSaver instance.
   * @see ResponseFileSaver
   * @since 0.13
   */
  public static ResponseFileSaver responseFileSaver(String fileNamePrefix) {
    return new ResponseFileSaver(fileNamePrefix);
  }

  /**
   * Builds a Backend Listener configured to use InfluxDB to send all results for easy tracing,
   * historic, comparison and live test results.
   *
   * @param influxDbUrl is the URL to connect to the InfluxDB instance where test results should be
   *                    sent.
   * @return the Backend Listener instance which can be used to set additional settings like title,
   * token &amp; queueSize.
   * @see InfluxDbBackendListener
   * @since 0.4
   */
  public static InfluxDbBackendListener influxDbListener(String influxDbUrl) {
    return new InfluxDbBackendListener(influxDbUrl);
  }

  /**
   * Builds an HTML Reporter which allows easily generating HTML reports for test plans.
   * <p>
   * This is just a handy short way of generating html reports following naming template
   * <pre>{@code <yyyy-MM-dd HH-mm-ss> <UUID>}</pre>
   * <p>
   * If you need to have a predictable report name, consider using
   * {@link #htmlReporter(String, String)} instead.
   *
   * @param reportsDirectory specifies the directory where HTML reports are generated. If the
   *                         reportsDirectory does not exist, then it will be created.
   * @return the HTML Reporter instance
   * @see #htmlReporter(String, String)
   * @see HtmlReporter
   * @since 0.6
   */
  public static HtmlReporter htmlReporter(String reportsDirectory) {
    return new HtmlReporter(reportsDirectory, null);
  }

  /**
   * Builds an HTML Reporter which allows easily generating HTML reports for test plans.
   * <p>
   * This is particularly helpful when you need to control de report directory name to do later
   * post-processing on it (eg: use CI build ID in the report directory name).
   *
   * @param reportsDirectory specifies the directory where HTML reports are generated. If the
   *                         reportsDirectory does not exist, then it will be created.
   * @param name             the name to be used for the report directory. <b>Names should be
   *                         unique, otherwise the report generation will fail after test plan
   *                         execution.</b>
   * @return the HTML Reporter instance
   * @see HtmlReporter
   * @since 1.0
   */
  public static HtmlReporter htmlReporter(String reportsDirectory, String name) {
    return new HtmlReporter(reportsDirectory, name);
  }

  /**
   * Builds a View Results Tree element to show live results in a pop-up window while the test
   * runs.
   * <p>
   * This element is helpful when debugging a test plan to verify each sample result, and general
   * structure of results.
   *
   * @return the View Results Tree element.
   * @see DslViewResultsTree
   * @since 0.19
   */
  public static DslViewResultsTree resultsTreeVisualizer() {
    return new DslViewResultsTree();
  }

  /**
   * Builds a Constant Timer which pauses the thread with for a given duration.
   *
   * @param duration specifies the duration for the timer to wait.
   * @return the timer for usage in test plan.
   * @since 1.0
   */

  public static DslConstantTimer constantTimer(Duration duration) {
    return new DslConstantTimer(duration);
  }

  /**
   * Builds a Uniform Random Timer which pauses the thread with a random time with uniform
   * distribution.
   *
   * <p>
   * The timer uses the minimum and maximum durations to define the range of values to be used in
   * the uniformly distributed selected value. These values differ from the parameters used in
   * JMeter Uniform Random Timer element to make it simpler for general users to use. The generated
   * JMeter test element uses as "constant delay offset" the minimum value, and as "maximum random
   * delay" (maximum - minimum) value.
   * </p>
   *
   * <p>
   * EXAMPLE: wait at least 3 seconds and maximum of 10 seconds
   * {@code uniformRandomTimer(Duration.ofSeconds(3), Duration.ofSeconds(10))}
   * <p>
   *
   * @param minimum is used to set the constant delay of the Uniform Random Timer.
   * @param maximum is used to set the maximum time the timer will be paused and will be used to
   *                obtain the random delay from the result of (maximum - minimum).
   * @return The Uniform Random Timer instance
   * @see DslUniformRandomTimer
   * @since 1.0
   */
  public static DslUniformRandomTimer uniformRandomTimer(Duration minimum, Duration maximum) {
    return new DslUniformRandomTimer(minimum, maximum);
  }

  /**
   * Builds a Constant Throughput Timer which paces samplers to limit the throughput.
   * <p>
   * The timer adds variable pauses to test plan execution to limit the throughput by checking time
   * between timer executions.
   *
   * @param throughputPerMinute specifies the target samples per minute.
   * @return the timer for usage in test plan.
   * @see DslThroughputTimer
   * @since 1.5
   */
  public static DslThroughputTimer throughputTimer(double throughputPerMinute) {
    return new DslThroughputTimer(throughputPerMinute);
  }

  /**
   * Builds a Synchronizing Timer that allows synchronizing samples to be sent all at once.
   * <p>
   * This timer is useful when you need to send requests in simultaneous batches, as a way to asure
   * the system under test gets the requests all at the same time.
   *
   * @return the timer for usage in a test plan.
   * @see DslSynchronizingTimer
   * @since 1.17
   */
  public static DslSynchronizingTimer synchronizingTimer() {
    return new DslSynchronizingTimer();
  }

  /**
   * Builds a CSV Data Set which allows loading from a CSV file variables to be used in test plan.
   * <p>
   * This allows to store for example in a CSV file one line for each user credentials, and then in
   * the test plan be able to use all the credentials to test with different users.
   * <p>
   * By default, the CSV data set will read comma separated values, use first row as name of the
   * generated variables, restart from beginning when csv entries are exhausted and will read a new
   * line of CSV for each thread and iteration.
   * <p>
   * E.g: If you have a csv with 2 entries and a test plan with two threads, iterating 2 times each,
   * you might get (since threads run in parallel, the assignment is not deterministic) following
   * assignment of rows:
   *
   * <pre>
   * thread 1, row 1
   * thread 2, row 2
   * thread 2, row 1
   * thread 1, row 2
   * </pre>
   *
   * @param csvFile path to the CSV file to read the data from.
   * @return the CSV Data Set instance for further configuration and usage.
   * @see DslCsvDataSet
   * @since 0.24
   */
  public static DslCsvDataSet csvDataSet(String csvFile) {
    return new DslCsvDataSet(csvFile);
  }

  /**
   * Same as {@link #csvDataSet(String)} but easing usage of test resources.
   *
   * @param resource test resource to use as input file for the CSV data set.
   * @return the CSV Data Set instance for further configuration and usage.
   * @see #csvDataSet(String)
   * @since 0.54
   */
  public static DslCsvDataSet csvDataSet(TestResource resource) {
    return new DslCsvDataSet(resource.filePath());
  }

  /**
   * Builds a counter for easy usage of auto incremental numbers in test plans.
   * <p>
   * This element is handy when generating uto incremental ids, positions in a list, etc.
   *
   * @param name specifies the variable name used for holding the counter value.
   * @return the counter for further configuration and usage.
   * @since 1.10
   */
  public static DslCounter counter(String name) {
    return new DslCounter(name);
  }

  /**
   * Allows easy usage of test resources like the ones in {@code src/test/resource} in maven
   * projects.
   *
   * @param resourcePath path to locate the resource. For example, in a maven project to create a
   *                     resource to file {@code "src/test/resources/csvs/my-csv.csv"} you need to
   *                     use {@code testResource("csvs/my-csv.csv")}.
   * @return the test resource.
   * @throws IllegalArgumentException when no file can be located for provided resource path
   * @see TestResource
   * @since 0.54
   */
  public static TestResource testResource(String resourcePath) throws IllegalArgumentException {
    return new TestResource(resourcePath);
  }

}
