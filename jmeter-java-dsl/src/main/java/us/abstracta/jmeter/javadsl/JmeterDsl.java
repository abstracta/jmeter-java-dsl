package us.abstracta.jmeter.javadsl;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion;
import us.abstracta.jmeter.javadsl.core.configs.DslCsvDataSet;
import us.abstracta.jmeter.javadsl.core.controllers.DslIfController;
import us.abstracta.jmeter.javadsl.core.controllers.DslTransactionController;
import us.abstracta.jmeter.javadsl.core.controllers.DslWhileController;
import us.abstracta.jmeter.javadsl.core.controllers.ForLoopController;
import us.abstracta.jmeter.javadsl.core.controllers.PercentController;
import us.abstracta.jmeter.javadsl.core.listeners.DslViewResultsTree;
import us.abstracta.jmeter.javadsl.core.listeners.HtmlReporter;
import us.abstracta.jmeter.javadsl.core.listeners.InfluxDbBackendListener;
import us.abstracta.jmeter.javadsl.core.listeners.JtlWriter;
import us.abstracta.jmeter.javadsl.core.listeners.ResponseFileSaver;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslBoundaryExtractor;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor.PostProcessorScript;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslRegexExtractor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorScript;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorVars;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslSetupThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslTeardownThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.RpsThreadGroup;
import us.abstracta.jmeter.javadsl.core.timers.DslUniformRandomTimer;
import us.abstracta.jmeter.javadsl.core.util.PropertyScriptBuilder.PropertyScript;
import us.abstracta.jmeter.javadsl.http.DslCacheManager;
import us.abstracta.jmeter.javadsl.http.DslCookieManager;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;
import us.abstracta.jmeter.javadsl.http.HttpHeaders;
import us.abstracta.jmeter.javadsl.java.DslJsr223Sampler;
import us.abstracta.jmeter.javadsl.java.DslJsr223Sampler.SamplerScript;

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
 * parameters (the optional ones), prefer them to be specified as methods of the implemented {@link
 * DslTestElement} for such case, in a similar fashion as Builder Pattern.
 *
 * @since 0.1
 */
public class JmeterDsl {

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
   * @param threads specifies the number of threads to simulate concurrent virtual users.
   * @param iterations specifies the number of iterations that each virtual user will run of
   * children elements until it stops.
   * @param children contains the test elements that each thread will execute in each iteration.
   * @return the thread group instance.
   * @see DslThreadGroup
   */
  public static DslThreadGroup threadGroup(int threads, int iterations,
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
  public static DslThreadGroup threadGroup(String name, int threads, int iterations,
      ThreadGroupChild... children) {
    return new DslThreadGroup(name, threads, iterations, Arrays.asList(children));
  }

  /**
   * Builds a new thread group with a given number of threads &amp; their duration.
   *
   * @param threads to simulate concurrent virtual users.
   * @param duration to keep each thread running for this period of time. Take into consideration
   * that JMeter supports specifying duration in seconds, so if you specify a smaller granularity
   * (like milliseconds) it will be rounded up to seconds.
   * @param children contains the test elements that each thread will execute until specified
   * duration is reached.
   * @return the thread group instance.
   * @see ThreadGroup
   * @since 0.5
   */
  public static DslThreadGroup threadGroup(int threads, Duration duration,
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
  public static DslThreadGroup threadGroup(String name, int threads, Duration duration,
      ThreadGroupChild... children) {
    return new DslThreadGroup(name, threads, duration, Arrays.asList(children));
  }

  /**
   * Builds a new thread group without any thread configuration.
   *
   * This method should be used as starting point for creating complex test thread profiles (like
   * spike, or incremental tests) in combination with {@link DslThreadGroup#holdFor(Duration)},
   * {@link DslThreadGroup#rampTo(int, Duration)} and {@link DslThreadGroup#rampToAndHold(int,
   * Duration, Duration)}.
   *
   * Eg:
   * <pre>{@code
   *  threadGroup()
   *    .rampTo(10, Duration.ofSeconds(10))
   *    .rampTo(5, Duration.ofSeconds(10))
   *    .rampToAndHold(20, Duration.ofSeconds(5), Duration.ofSeconds(10))
   *    .rampTo(0, Duration.ofSeconds(5))
   *    .children(...)
   * }</pre>
   *
   * @return the thread group instance.
   * @since 0.18
   */
  public static DslThreadGroup threadGroup() {
    return new DslThreadGroup(null);
  }

  /**
   * Same as {@link #threadGroup()} but allowing to set a name on the thread group.
   * <p>
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see #threadGroup()
   * @since 0.18
   */
  public static DslThreadGroup threadGroup(String name) {
    return new DslThreadGroup(name);
  }

  /**
   * Builds a thread group that allows running logic before other thread groups.
   *
   * This is usually used to run some setup logic before the actual test plan logic. In particular
   * logic that needs to be run within the context of JMeter test (eg: requires setting some JMeter
   * property) or needs to be run from same machines as the test plan.
   *
   * Check {@link DslSetupThreadGroup} for more details and configuration options.
   *
   * @param children test elements to be run before any other thread group.
   * @return the setup thread group for further customization or just usage in test plan
   * @see DslSetupThreadGroup
   * @since 0.33
   */
  public static DslSetupThreadGroup setupThreadGroup(ThreadGroupChild... children) {
    return new DslSetupThreadGroup(Arrays.asList(children));
  }

  /**
   * Builds a thread group that allows running logic after other thread groups.
   *
   * This is usually used to run some clean up logic after the actual test plan logic. In particular
   * logic that needs to be run within the context of JMeter test (eg: requires setting some JMeter
   * property) or needs to be run from same machines as the test plan.
   *
   * Check {@link DslTeardownThreadGroup} for more details and configuration options.
   *
   * @param children test elements to be run after any other thread group.
   * @return the teardown thread group for further customization or just usage in test plan
   * @see DslTeardownThreadGroup
   * @since 0.33
   */
  public static DslTeardownThreadGroup teardownThreadGroup(ThreadGroupChild... children) {
    return new DslTeardownThreadGroup(Arrays.asList(children));
  }

  /**
   * Builds a thread group that dynamically adapts thread count and pauses to match a given RPS.
   *
   * Internally this element uses
   * <a href="https://jmeter-plugins.org/wiki/ConcurrencyThreadGroup/">Concurrency Thread Group</a>
   * in combination with <a href="https://jmeter-plugins.org/wiki/ThroughputShapingTimer/">Throughput
   * Shaping Timer</a>.
   *
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
   * @param name specifies the name to identify the transaction.
   * @param children contains the test elements that will be contained within the transaction.
   * @return the transaction instance.
   * @see DslTransactionController
   * @since 0.14
   */
  public static DslTransactionController transaction(String name, ThreadGroupChild... children) {
    return new DslTransactionController(name, Arrays.asList(children));
  }

  /**
   * Same as {@link #transaction(String, ThreadGroupChild...)} but postponing children setting to
   * allow further configuration before specifying children elements.
   *
   * @param name specifies the name to identify the transaction.
   * @return the transaction instance.
   * @see DslTransactionController
   * @since 0.29
   */
  public static DslTransactionController transaction(String name) {
    return new DslTransactionController(name, Collections.emptyList());
  }

  /**
   * Builds an If Controller that allows to conditionally run specified children.
   *
   * @param condition contains an expression that when evaluated to true tells the controller to run
   * specified children.
   * @param children contains the test plan elements to execute when the condition is true.
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
   * <b>WARNING:</b> This only works when using embedded jmeter engine (no support for saving to
   * JMX and running it in JMeter GUI, or running it with BlazeMeter). If you need such support
   * consider using {@link #ifController(String, ThreadGroupChild...)} instead.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see PropertyScript
   * @see #ifController(String, ThreadGroupChild...)
   * @since 0.27
   */
  public static DslIfController ifController(PropertyScript condition,
      ThreadGroupChild... children) {
    return new DslIfController(condition, Arrays.asList(children));
  }

  /**
   * Builds a While Controller that allows to run specific part of the test plan while a given
   * condition is met in one thread iteration.
   *
   * @param condition contains an expression that will be evaluated to identify when to stop
   * looping.
   * @param children contains the test plan elements to execute while the condition is true.
   * @return the controller instance for further configuration and usage.
   * @see DslWhileController
   * @since 0.27
   */
  public static DslWhileController whileController(String condition, ThreadGroupChild... children) {
    return new DslWhileController(null, condition, Arrays.asList(children));
  }

  /**
   * Same as {@link #whileController(String, ThreadGroupChild...)} but allowing to set a name which
   * defines autogenerated variable created by JMeter containing iteration index.
   *
   * @param name specifies the name to assign to the controller. This variable affects the JMeter
   * autogenerated variable {@code __jm__<controllerName>__idx} which holds the loop iteration
   * number (starting at 0).
   * @param condition contains an expression that will be evaluated to identify when to stop
   * looping.
   * @param children contains the test plan elements to execute while the condition is true.
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
   * <b>WARNING:</b> This only works when using embedded jmeter engine (no support for saving to
   * JMX and running it in JMeter GUI, or running it with BlazeMeter). If you need such support
   * consider using {@link #whileController(String, ThreadGroupChild...)} instead.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @param condition contains java code that will be evaluated to identify when to stop looping.
   * @param children contains the test plan elements to execute while the condition is true.
   * @return the controller instance for further configuration and usage.
   * @see PropertyScript
   * @see #whileController(String, ThreadGroupChild...)
   * @since 0.27
   */
  public static DslWhileController whileController(PropertyScript condition,
      ThreadGroupChild... children) {
    return new DslWhileController(null, condition, Arrays.asList(children));
  }

  /**
   * Same as {@link #whileController(PropertyScript, ThreadGroupChild...)} but allowing to set a
   * name which defines autogenerated variable created by JMeter containing iteration index.
   *
   * @param name specifies the name to assign to the controller. This variable affects the JMeter
   * autogenerated variable {@code __jm__<controllerName>__idx} which holds the loop iteration
   * number (starting at 0).
   * @param condition contains java code that will be evaluated to identify when to stop looping.
   * @param children contains the test plan elements to execute while the condition is true.
   * @return the controller instance for further configuration and usage.
   * @see PropertyScript
   * @see #whileController(PropertyScript, ThreadGroupChild...)
   * @since 0.27
   */
  public static DslWhileController whileController(String name, PropertyScript condition,
      ThreadGroupChild... children) {
    return new DslWhileController(name, condition, Arrays.asList(children));
  }

  /**
   * Builds a Loop Controller that allows to run specific number of times the given children in each
   * thread group iteration.
   *
   * Eg: if a thread group iterates 3 times and the Loop Controller is configured to 5, then the
   * children elements will run {@code 3*5=15} times for each thread.
   *
   * @param count specifies the number of times to execute the children elements in each thread
   * group iteration.
   * @param children contains the test plan elements to execute the given number of times in each
   * thread group iteration.
   * @return the controller instance for further configuration and usage.
   * @see ForLoopController
   * @since 0.27
   */
  public static ForLoopController forLoopController(int count, ThreadGroupChild... children) {
    return new ForLoopController(null, count, Arrays.asList(children));
  }

  /**
   * Same as {@link #forLoopController(int, ThreadGroupChild...)} but allowing to set a name which
   * defines autogenerated variable created by JMeter containing iteration index.
   *
   * @param count specifies the number of times to execute the children elements in each thread
   * group iteration.
   * @param children contains the test plan elements to execute the given number of times in each
   * thread group iteration.
   * @return the controller instance for further configuration and usage.
   * @see ForLoopController
   * @see #forLoopController(int, ThreadGroupChild...)
   * @since 0.27
   */
  public static ForLoopController forLoopController(String name, int count,
      ThreadGroupChild... children) {
    return new ForLoopController(name, count, Arrays.asList(children));
  }

  /**
   * Builds a Percent Controller to execute children only a given percent of times.
   *
   * Internally, this uses a JMeter Throughput Controller with executions percentage configuration.
   *
   * @param percent defines a number between 0 and 100 that defines the percentage of times to
   * execute given children elements.
   * @param children holds test plan elements to execute when for the given percent of times.
   * @return the controller instance for further configuration and usage.
   * @see PercentController
   * @since 0.25
   */
  public static PercentController percentController(float percent, ThreadGroupChild... children) {
    return new PercentController(percent, Arrays.asList(children));
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
   * This method is just an abstraction that uses a JMeter variable as URL and calculates the
   * variable with a jsr223PreProcessor, added as first child of the HTTP Request Sampler.
   * <p>
   * <b>WARNING:</b> As this method internally uses {@link #jsr223PreProcessor(PreProcessorScript)},
   * same limitations and considerations apply. Check its documentation. To avoid such limitations
   * you may just use {@link #httpSampler(String)} instead, using a JMeter variable and {@link
   * #jsr223PreProcessor(String)} to dynamically set the variable.
   *
   * @param urlSupplier specifies URL the HTTP Request sampler will hit.
   * @return the HTTP Request sampler instance which can be used to define additional settings for
   * the HTTP request (like method, body, headers, pre &amp; post processors, etc.).
   * @see DslHttpSampler
   * @see #jsr223PreProcessor(PreProcessorScript)
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
   * <b>WARNING:</b> As this method internally uses {@link #jsr223PreProcessor(PreProcessorScript)},
   * same limitations and considerations apply. Check its documentation To avoid such limitations
   * you may just use {@link #httpSampler(String, String)} instead, using a JMeter variable and
   * {@link #jsr223PreProcessor(String)} to dynamically set the variable.
   *
   * @see #httpSampler(Function)
   * @since 0.10
   */
  public static DslHttpSampler httpSampler(String name,
      Function<PreProcessorVars, String> urlSupplier) {
    return new DslHttpSampler(name, urlSupplier);
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
   * Builds a JSR223 Sampler which allows sampling any Java API or custom logic.
   * <p>
   *
   * @param script contains the script to be use while sampling. By default, this will be a groovy
   * script, but you can change it by setting the language property in the returned post processor.
   * @return the JSR223 Sampler instance
   * @see DslJsr223Sampler
   * @since 0.22
   */
  public static DslJsr223Sampler jsr223Sampler(String script) {
    return new DslJsr223Sampler(null, script);
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
   * <b>WARNING:</b> This only works when using embedded jmeter engine (no support for saving to
   * JMX and running it in JMeter GUI, or running it with BlazeMeter). If you need such support
   * consider using {@link #jsr223Sampler(String)} instead.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see SamplerScript
   * @see #jsr223Sampler(String)
   * @since 0.22
   */
  public static DslJsr223Sampler jsr223Sampler(SamplerScript script) {
    return new DslJsr223Sampler(null, script);
  }

  /**
   * Same as {@link #jsr223Sampler(String, String)} but allowing to use Java type safety and code
   * completion when specifying the script.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine (no support for saving to
   * JMX and running it in JMeter GUI, or running it with BlazeMeter). If you need such support
   * consider using {@link #jsr223Sampler(String)} instead.
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
   * Builds a JSR223 Pre Processor which allows including custom logic to modify requests.
   * <p>
   * This preprocessor is very powerful, and lets you alter request parameters, jmeter context and
   * implement any kind of custom logic that you may think.
   *
   * @param script contains the script to be executed by the preprocessor. By default, this will be
   * a groovy script, but you can change it by setting the language property in the returned post
   * processor.
   * @return the JSR223 Pre Processor instance
   * @see DslJsr223PreProcessor
   * @since 0.7
   */
  public static DslJsr223PreProcessor jsr223PreProcessor(String script) {
    return new DslJsr223PreProcessor(null, script);
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
   * <b>WARNING:</b> This only works when using embedded jmeter engine (no support for saving to
   * JMX and running it in JMeter GUI, or running it with BlazeMeter). If you need such support
   * consider using {@link #jsr223PreProcessor(String)} instead.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see PreProcessorScript
   * @see #jsr223PreProcessor(String)
   * @since 0.10
   */
  public static DslJsr223PreProcessor jsr223PreProcessor(PreProcessorScript script) {
    return new DslJsr223PreProcessor(null, script);
  }

  /**
   * Same as {@link #jsr223PreProcessor(String, String)} but allowing to use Java type safety and
   * code completion when specifying the script.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine (no support for saving to
   * JMX and running it in JMeter GUI, or running it with BlazeMeter). If you need such support
   * consider using {@link #jsr223PreProcessor(String)} instead.
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
   * Builds a Regex Extractor which allows using regular expressions to extract different parts of a
   * sample result (request or response).
   * <p>
   * This method provides a simple default implementation with required settings, but more settings
   * are provided by returned DslRegexExtractor.
   * <p>
   * By default when regex is not matched, no variable will be created or modified. On the other
   * hand when the regex matches it will by default store the first capturing group (part of
   * expression between parenthesis) of the first match for the regular expression.
   *
   * @param variableName is the name of the variable to be used to store the extracted value to.
   * Additional variables {@code <variableName>_g<groupId>} will be created for each regular
   * expression capturing group (segment of regex between parenthesis), being the group 0 the entire
   * match of the regex. {@code <variableName>_g} variable contains the number of matched capturing
   * groups (not counting the group 0).
   * @param regex regular expression used to extract part of request or response.
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
   * By default when no match is found, no variable will be created or modified. On the other hand,
   * when a match is found, it will by default store the first match.
   *
   * @param variableName is the name of the variable to be used to store the extracted value to.
   * @param leftBoundary specifies text preceding the text to be extracted.
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
   * Builds a JSON JMESPath Extractor which allows using a JMESPath to extract part of a JSON
   * response.
   * <p>
   * This method provides a simple default implementation with required settings, but more settings
   * are provided by returned DslJsonExtractor.
   * <p>
   * By default when no match is found, no variable will be created or modified. On the other hand,
   * when a match is found, it will by default store the first match.
   *
   * @param variableName is the name of the variable to be used to store the extracted value to.
   * @param jmesPath specifies the JMESPath to extract the value.
   * @return the JSON JMESPath Extractor which can be used to define additional settings to use when
   * extracting (like defining match number, scope, etc.).
   * @see DslJsonExtractor
   * @since 0.28
   */
  public static DslJsonExtractor jsonExtractor(String variableName, String jmesPath) {
    return new DslJsonExtractor(variableName, jmesPath);
  }

  /**
   * Builds a JSR223 Post Processor which allows including custom logic to process sample results.
   * <p>
   * This post processor is very powerful, and lets you alter sample results, jmeter context and
   * implement any kind of custom logic that you may think.
   *
   * @param script contains the script to be executed by the post processor. By default, this will
   * be a groovy script, but you can change it by setting the language property in the returned post
   * processor.
   * @return the JSR223 Post Processor instance
   * @see DslJsr223PostProcessor
   * @since 0.6
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(String script) {
    return new DslJsr223PostProcessor(null, script);
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
   * <b>WARNING:</b> This only works when using embedded jmeter engine (no support for saving to
   * JMX and running it in JMeter GUI, or running it with BlazeMeter). If you need such support
   * consider using {@link #jsr223PostProcessor(String)} instead.
   * <p>
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see PostProcessorScript
   * @see #jsr223PostProcessor(String)
   * @since 0.10
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(PostProcessorScript script) {
    return new DslJsr223PostProcessor(null, script);
  }

  /**
   * Same as {@link #jsr223PostProcessor(String, String)} but allowing to use Java type safety and
   * code completion when specifying the script.
   * <p>
   * <b>WARNING:</b> This only works when using embedded jmeter engine (no support for saving to
   * JMX and running it in JMeter GUI, or running it with BlazeMeter). If you need such support
   * consider using {@link #jsr223PostProcessor(String)} instead.
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
   * Builds a Response Assertion to be able to check that obtained sample result is the expected
   * one.
   *
   * JMeter by default uses repose codes (eg: 4xx and 5xx HTTP response codes are error codes) to
   * determine if a request was success or not, but in some cases this might not be enough or
   * correct. In some cases applications might not behave in this way, for example, they might
   * return a 200 HTTP status code but with an error message in the body, or the response might be a
   * success one, but the information contained within the response is not the expected one to
   * continue executing the test. In such scenarios you can use response assertions to properly
   * verify your assumptions before continuing with next request in the test plan.
   *
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
   * Builds a Simple Data Writer to write all collected results to a JTL file.
   *
   * @param jtlFile is the path of the JTL file where to save the results.
   * @return the JtlWriter instance.
   * @see JtlWriter
   */
  public static JtlWriter jtlWriter(String jtlFile) {
    return new JtlWriter(jtlFile);
  }

  /**
   * Builds a Response File Saver to generate a file for each response of a sample.
   *
   * @param fileNamePrefix the prefix to be used when generating the files. This should contain the
   * directory location where the files should be generated and can contain a file name prefix for
   * all file names (eg: target/response-files/response-).
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
   * sent.
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
   *
   * @param reportDirectory directory where HTML report is generated.
   * @return the HTML Reporter instance
   * @throws IOException if reportDirectory is an existing file, or an existing nonempty directory.
   * The idea of this exception is to avoid users unintentionally overwriting previous reports and
   * don't force any particular structure on collection of reports.
   * @see HtmlReporter
   * @since 0.6
   */
  public static HtmlReporter htmlReporter(String reportDirectory) throws IOException {
    return new HtmlReporter(reportDirectory);
  }

  /**
   * Builds a View Results Tree element to show live results in a pop-up window while the test
   * runs.
   *
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
   * Builds a Uniform Random Timer which pauses the thread with a random time with uniform
   * distribution.
   *
   * <p>
   * The timer uses the minimumMillis and maximumMillis to define the range of values to be used in
   * the uniformly distributed selected value. These values differ from the parameters used in
   * JMeter Uniform Random Timer element to make it simpler for general users to use. The generated
   * JMeter test element uses as "constant delay offset" the minimumMillis value, and as "maximum
   * random delay" (maximumMillis - minimumMillis) value.
   * </p>
   *
   * <p>
   * EXAMPLE: wait at least 3 seconds and maximum of 10 seconds {@code
   * uniformRandomTimer(3000,10000)}
   * <p>
   *
   * @param minimumMillis is used to set the constant delay of the Uniform Random Timer.
   * @param maximumMillis is used to set the maximum time the timer will be paused and will be used
   * to obtain the random delay from the result of (maximumMillis - minimumMillis).
   * @return The Uniform Random Timer instance
   * @see DslUniformRandomTimer
   * @since 0.16
   */
  public static DslUniformRandomTimer uniformRandomTimer(long minimumMillis, long maximumMillis) {
    return new DslUniformRandomTimer(minimumMillis, maximumMillis);
  }

  /**
   * Builds a CSV Data Set which allows loading from a CSV file variables to be used in test plan.
   *
   * This allows to store for example in a CSV file one line for each user credentials, and then in
   * the test plan be able to use all the credentials to test with different users.
   *
   * By default, the CSV data set will read comma separated values, use first row as name of the
   * generated variables, restart from beginning when csv entries are exhausted and will read a new
   * line of CSV for each thread and iteration.
   *
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

}
