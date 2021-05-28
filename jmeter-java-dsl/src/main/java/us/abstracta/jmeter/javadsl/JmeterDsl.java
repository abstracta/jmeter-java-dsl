package us.abstracta.jmeter.javadsl;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Function;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.assertions.DslResponseAssertion;
import us.abstracta.jmeter.javadsl.core.listeners.HtmlReporter;
import us.abstracta.jmeter.javadsl.core.listeners.InfluxDbBackendListener;
import us.abstracta.jmeter.javadsl.core.listeners.JtlWriter;
import us.abstracta.jmeter.javadsl.core.listeners.ResponseFileSaver;
import us.abstracta.jmeter.javadsl.core.logiccontrollers.DslTransactionController;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor.PostProcessorScript;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslRegexExtractor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorScript;
import us.abstracta.jmeter.javadsl.core.preprocessors.DslJsr223PreProcessor.PreProcessorVars;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler;
import us.abstracta.jmeter.javadsl.http.HttpHeaders;
import us.abstracta.jmeter.javadsl.core.timers.DslUniformRandomTimer;

/**
 * This is the main class to be imported from any code using JMeter DSL.
 * <p>
 * This class contains factory methods to create {@link DslTestElement} instances that allow
 * specifying test plans and associated test elements (samplers, thread groups, listeners, etc). If
 * you want to support new test elements, then you either add them here (if they are considered to
 * be part of the core of JMeter), or implement another similar class containing only the specifics
 * of the protocol, repository, or grouping of test elements that you want to build (eg, one might
 * implement an Http2JMeterDsl class with only http2 test elements factory methods).
 * <p>
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
   */
  public static DslThreadGroup threadGroup(String name, int threads, Duration duration,
      ThreadGroupChild... children) {
    return new DslThreadGroup(name, threads, duration, Arrays.asList(children));
  }

  /**
   * Builds a new transaction controller with the given name.
   *
   * @param name specifies the name to identify the transaction.
   * @param children contains the test elements that will be contained within the transaction.
   * @return the test plan instance.
   * @see DslTransactionController
   */
  public static DslTransactionController transaction(String name, ThreadGroupChild... children) {
    return new DslTransactionController(name, Arrays.asList(children));
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
   * Builds an HTTP Request sampler to sample HTTP requests with a dynamically calculated URL.
   * <p>
   * This method is just an abstraction that uses a JMeter variable as URL and calculates the
   * variable with a jsr223PreProcessor, added as first child of the HTTP Request Sampler.
   * <p>
   * <b>WARNING:</b> As this method internally uses {@link #jsr223PreProcessor(PreProcessorScript)},
   * same limitations and considerations apply. Check it's documentation. To avoid such limitations
   * you may just use {@link #httpSampler(String)} instead, using a JMeter variable and {@link
   * #jsr223PreProcessor(String)} to dynamically set the variable.
   *
   * @param urlSupplier specifies URL the HTTP Request sampler will hit.
   * @return the HTTP Request sampler instance which can be used to define additional settings for
   * the HTTP request (like method, body, headers, pre &amp; post processors, etc).
   * @see DslHttpSampler
   * @see #jsr223PreProcessor(PreProcessorScript)
   */
  public static DslHttpSampler httpSampler(Function<PreProcessorVars, String> urlSupplier) {
    return httpSampler(null, urlSupplier);
  }

  /**
   * Same as {@link #httpSampler(String)} but allowing to set a name to the HTTP Request sampler.
   * <p>
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
   * <p>
   * Setting a proper name allows to easily identify the requests generated by this sampler and
   * check it's particular statistics.
   * <p>
   * <b>WARNING:</b> As this method internally uses {@link #jsr223PreProcessor(PreProcessorScript)},
   * same limitations and considerations apply. Check it's documentation To avoid such limitations
   * you may just use {@link #httpSampler(String, String)} instead, using a JMeter variable and
   * {@link #jsr223PreProcessor(String)} to dynamically set the variable.
   *
   * @see #httpSampler(Function)
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
   * Builds a JSR223 Pre Processor which allows including custom logic to modify requests.
   * <p>
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
   * <p>
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
   */
  public static DslJsr223PreProcessor jsr223PreProcessor(String name, PreProcessorScript script) {
    return new DslJsr223PreProcessor(name, script);
  }

  /**
   * Builds a Regex Extractor which allows to use regular expressions to extract different parts of
   * a sample result (request or response).
   * <p>
   * This method provides a simple default implementation with required settings, but more settings
   * are provided by returned DslRegexExtractor.
   * <p>
   * By default when regex is not matched, no variable will be created or modified. On the other
   * hand when the regex matches it will by default store the first capturing group (part of
   * expression between parenthesis) matched by the regular expression.
   *
   * @param variableName is the name of the variable to be used to store the extracted value to.
   * Additional variables {@code <variableName>_g<groupId>} will be created for each regular
   * expression capturing group (segment of regex between parenthesis), being group 0 the entire
   * match of the regex. {@code <variableName>_g} variable contains the number of matched capturing
   * groups (not counting the group 0).
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
   * <p>
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
   * <p>
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
   * By default response assertion will use the response body of the main sample result (not sub
   * samples as redirects, or embedded resources) to check the specified criteria (substring match,
   * entire string equality, contained regex or entire regex match) against.
   *
   * @return the create Response Assertion which should be modified to apply the proper criteria.
   * Check {@link DslResponseAssertion} for all available options.
   * @see DslResponseAssertion
   */
  public static DslResponseAssertion responseAssertion() {
    return new DslResponseAssertion(null);
  }

  /**
   * Same as {@link #responseAssertion()} but allowing to set a name on the assertion, which can be
   * later used to identify assertion results and differentiate it from other assertions.
   *
   * @param name is the name to be assigned to the assertion
   * @return the create Response Assertion which should be modified to apply the proper criteria.
   * Check {@link DslResponseAssertion} for all available options.
   * @see #responseAssertion(String)
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

  /**
   * Builds a Uniform Random Timer which pauses the thread with a random time with uniform distribution.
   *
   * <p>
   * The timer uses the minimunMillis and maximumMillis to define the range of values to be used in
   * the uniformly distributed selected value. These values differ from the parameters used in JMeter
   * Uniform Random Timer element to make it simpler for general users to use. The generated JMeter
   * test element uses as "constant delay offset" the minimumMillis value, and as "maximum random
   * delay" (maximumMillis - minimumMillis) value.
   * </p>
   *
   * <p>
   * EXAMPLE: wait at least 3 seconds and maximum of 10 seconds
   * {@code uniformRandomTimer(3000,10000)}
   * <p>
   * @param minimumMillis is used to set the constant delay of the Uniform Random Timer.
   * @param maximumMillis is used to set the maximum time the timer will be paused and will be used
   * to obtain the random delay from the result of (maximumMillis - minimumMillis).
   * @return The Uniform Random Timer instance
   * @see DslUniformRandomTimer
   */

  public static DslUniformRandomTimer uniformRandomTimer(long minimumMillis, long maximumMillis) {
    return new DslUniformRandomTimer(minimumMillis, maximumMillis);
  }

}
