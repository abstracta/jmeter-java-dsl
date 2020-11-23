package us.abstracta.jmeter.javadsl;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;

import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.listeners.HtmlReporter;
import us.abstracta.jmeter.javadsl.core.listeners.InfluxDbBackendListener;
import us.abstracta.jmeter.javadsl.core.listeners.JtlWriter;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor.Jsr223PostProcessorScript;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslRegexExtractor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.Jsr223PreProcessorScript;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;
import us.abstracta.jmeter.javadsl.http.HttpHeaders;

/**
 * This is the main class to be imported from any code using JMeter DSL.
 *
 * This class contains factory methods to create {@link DslTestElement} instances that allow
 * specifying test plans and associated test elements (samplers, thread groups, listeners, etc). If
 * you want to support new test elements, then you either add them here (if they are considered to
 * be part of the core of JMeter), or implement another similar class containing only the specifics
 * of the protocol, repository, or grouping of test elements that you want to build (eg, one might
 * implement an Http2JMeterDsl class with only http2 test elements factory methods).
 *
 * When implement new factory methods consider adding only as parameters the main properties of the
 * test elements (the ones that makes sense to specify in most of the cases). For the rest of
 * parameters (the optional ones), prefer them to be specified as methods of the implemented {@link
 * DslTestElement} for such case, in a similar fashion as Builder Pattern.
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
   * Builds a new thread group with a given test duration.
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
   *
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see #threadGroup(int, int, ThreadGroupChild...)
   */
  public static DslThreadGroup threadGroup(String name, int threads, int iterations,
      ThreadGroupChild... children) {
    return new DslThreadGroup(name, threads, iterations, Arrays.asList(children));
  }

  /**
   * Builds a new thread group with a given test duration.
   *
   * @param threads to simulate concurrent virtual users.
   * @param duration to run the test until it stops. Take into consideration that JMeter supports
   * specifying duration in seconds, so if you specify a smaller granularity (like milliseconds) it
   * will be rounded up to seconds.
   * @param children contains the test elements that each thread will execute until specified
   * duration is reached.
   * @return the thread group instance.
   * @see ThreadGroup
   */
  public static DslThreadGroup threadGroup(int threads, Duration duration,
      ThreadGroupChild... children) {
    return threadGroup(null, threads, duration, children);
  }

  /**
   * Same as {@link #threadGroup(int, Duration, ThreadGroupChild...)} but allowing to set a name on
   * the thread group.
   *
   * Setting a proper name allows to properly identify the requests generated in each thread group.
   *
   * @see #threadGroup(int, Duration, ThreadGroupChild...)
   */
  public static DslThreadGroup threadGroup(String name, int threads, Duration duration,
      ThreadGroupChild... children) {
    return new DslThreadGroup(name, threads, duration, Arrays.asList(children));
  }

  /**
   * Builds an HTTP Request sampler to sample HTTP requests.
   *
   * @param url specifies URL the HTTP Request sampler will hit.
   * @return the HTTP Request sampler instance which can be used to define additional settings for
   * the HTTP request (like method, body, headers, pre &amp; post processors, etc).
   * @see DslHttpSampler
   */
  public static DslHttpSampler httpSampler(String url) {
    return httpSampler(null, url);
  }

  /**
   * Builds an HTTP Request sampler to sample HTTP requests.
   *
   * @param urlSupplier specifies URL the HTTP Request sampler will hit.
   * @return the HTTP Request sampler instance which can be used to define additional settings for
   * the HTTP request (like method, body, headers, pre &amp; post processors, etc).
   * @see DslHttpSampler
   */
  public static DslHttpSampler httpSampler(Function<DslJsr223PreProcessor.Jsr223PreProcessorScriptVars, String> urlSupplier) {
    return httpSampler(null, urlSupplier);
  }

  /**
   * Same as {@link #httpSampler(String)} but allowing to set a name to the HTTP Request sampler.
   *
   * Setting a proper name allows to easily identify the requests generated by this sampler and
   * check it's particular statistics.
   *
   * @see #httpSampler(String)
   */
  public static DslHttpSampler httpSampler(String name, String url) {
    return new DslHttpSampler(name, url);
  }

  /**
   * Same as {@link #httpSampler(Function)} but allowing to set a name to the HTTP Request sampler.
   *
   * Setting a proper name allows to easily identify the requests generated by this sampler and
   * check it's particular statistics.
   *
   * @see #httpSampler(String)
   */
  public static DslHttpSampler httpSampler(String name, Function<DslJsr223PreProcessor.Jsr223PreProcessorScriptVars, String> urlSupplier) {
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
   * Builds a JSR223 Pre Processor which allows including custom logic to modify requests.
   *
   * This pre processor is very powerful, and lets you alter request parameters, jmeter context and
   * implement any kind of custom logic that you may think.
   *
   * @param script contains the script to be executed by the pre processor. By default this will be
   * a groovy script, but you can change it by setting the language property in the returned post
   * processor.
   * @return the JSR223 Pre Processor instance
   * @see DslJsr223PreProcessor
   */
  public static DslJsr223PreProcessor jsr223PreProcessor(String script) {
    return new DslJsr223PreProcessor(null, script);
  }

  /**
   * Same as {@link #jsr223PreProcessor(String)} but allowing to set a name on the pre processor.
   *
   * The name is used as logger name which allows configuring log level, appender, etc, for the pre
   * processor.
   *
   * @see #jsr223PreProcessor(String)
   **/
  public static DslJsr223PreProcessor jsr223PreProcessor(String name, String script) {
    return new DslJsr223PreProcessor(name, script);
  }

  /**
   * Same as {@link #jsr223PreProcessor(String)} but allowing to use Java type safety and code
   * completion when specifying the script.
   *
   * <b>WARNING:</b> Is currently only supported to run these pre processors only with embedded
   * jmeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with
   * BlazeMeter).
   *
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see Jsr223PreProcessorScript
   * @see #jsr223PreProcessor(String)
   */
  public static DslJsr223PreProcessor jsr223PreProcessor(Jsr223PreProcessorScript script) {
    return new DslJsr223PreProcessor(null, script);
  }

  /**
   * Same as {@link #jsr223PreProcessor(String, String)} but allowing to use Java type safety and
   * code completion when specifying the script.
   *
   * <b>WARNING:</b> Is currently only supported to run these pre processors only with embedded
   * jmeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with
   * BlazeMeter).
   *
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see Jsr223PreProcessorScript
   * @see #jsr223PreProcessor(String)
   */
  public static DslJsr223PreProcessor jsr223PreProcessor(String name,
      Jsr223PreProcessorScript script) {
    return new DslJsr223PreProcessor(name, script);
  }

  /**
   * Builds a Regex Extractor which allows to use regular expressions to extract different parts of
   * a sample result (request or response).
   *
   * This method provides a simple default implementation with required settings, but more settings
   * are provided by returned DslRegexExtractor.
   *
   * By default when regex is not matched, no variable will be created or modified. On the other
   * hand when the regex matches it will by default store the first capturing group (part of
   * expression between parenthesis) matched by the regular expression.
   *
   * @param variableName is the name of the variable to be used to store the extracted value to.
   * @param regex regular expression used to extract part of request or response.
   * @return the Regex Extractor which can be used to define additional settings to use when
   * extracting (like defining match number, template, etc).
   * @see DslRegexExtractor
   */
  public static DslRegexExtractor regexExtractor(String variableName, String regex) {
    return new DslRegexExtractor(variableName, regex);
  }

  /**
   * Builds a JSR223 Post Processor which allows including custom logic to process sample results.
   *
   * This post processor is very powerful, and lets you alter sample results, jmeter context and
   * implement any kind of custom logic that you may think.
   *
   * @param script contains the script to be executed by the post processor. By default this will be
   * a groovy script, but you can change it by setting the language property in the returned post
   * processor.
   * @return the JSR223 Post Processor instance
   * @see DslJsr223PostProcessor
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(String script) {
    return new DslJsr223PostProcessor(null, script);
  }

  /**
   * Same as {@link #jsr223PostProcessor(String)} but allowing to set a name on the post processor.
   *
   * The name is used as logger name which allows configuring log level, appender, etc, for the post
   * processor.
   *
   * @see #jsr223PostProcessor(String)
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(String name, String script) {
    return new DslJsr223PostProcessor(name, script);
  }

  /**
   * Same as {@link #jsr223PostProcessor(String)} but allowing to use Java type safety and code
   * completion when specifying the script.
   *
   * <b>WARNING:</b> Is currently only supported to run these post processors only with embedded
   * jmeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with
   * BlazeMeter).
   *
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see Jsr223PostProcessorScript
   * @see #jsr223PostProcessor(String)
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(Jsr223PostProcessorScript script) {
    return new DslJsr223PostProcessor(null, script);
  }

  /**
   * Same as {@link #jsr223PostProcessor(String, String)} but allowing to use Java type safety and
   * code completion when specifying the script.
   *
   * <b>WARNING:</b> Is currently only supported to run these post processors only with embedded
   * jmeter engine (no support for saving to JMX and running it in JMeter GUI, or running it with
   * BlazeMeter).
   *
   * Take into consideration that the provided script is invoked from as may threads as defined in
   * thread group. So make sure that provided logic is thread safe.
   *
   * @see Jsr223PostProcessorScript
   * @see #jsr223PostProcessor(String, String)
   */
  public static DslJsr223PostProcessor jsr223PostProcessor(String name,
      Jsr223PostProcessorScript script) {
    return new DslJsr223PostProcessor(name, script);
  }

  /**
   * Builds a Simple Data Writer to write all collected results to a JTL file.
   *
   * @param jtlFile is the path of the JTL file where to save the results.
   * @return the JtlWriter instance.
   * @throws FileAlreadyExistsException when a file or directory already exists with the given path.
   * The idea of this exception is to avoid users unintentionally modifying previous collected
   * results and don't force any particular structure on collection of reports.
   * @see JtlWriter
   */
  public static JtlWriter jtlWriter(String jtlFile) throws FileAlreadyExistsException {
    return new JtlWriter(jtlFile);
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
   */
  public static InfluxDbBackendListener influxDbListener(String influxDbUrl) {
    return new InfluxDbBackendListener(influxDbUrl);
  }

  /**
   * Builds an HTML Reporter which allows easily generating HTML reports for test plans.
   *
   * @param reportDirectory directory where HTML report is generated.
   * @return the HTML Reporter instance
   * @throws IOException if reportDirectory is an existing file, or an existing non empty directory.
   * The idea of this exception is to avoid users unintentionally overwriting previous reports and
   * don't force any particular structure on collection of reports.
   * @see HtmlReporter
   */
  public static HtmlReporter htmlReporter(String reportDirectory) throws IOException {
    return new HtmlReporter(reportDirectory);
  }

}
