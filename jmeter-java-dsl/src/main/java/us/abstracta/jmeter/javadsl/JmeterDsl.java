package us.abstracta.jmeter.javadsl;

import java.time.Duration;
import java.util.Arrays;
import us.abstracta.jmeter.javadsl.core.DslTestElement;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;
import us.abstracta.jmeter.javadsl.core.listeners.InfluxDbBackendListener;
import us.abstracta.jmeter.javadsl.core.listeners.JtlWriter;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
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
   * @return the HTTP Request sampler instance.
   * @see DslHttpSampler
   */
  public static DslHttpSampler httpSampler(String url) {
    return httpSampler(null, url);
  }

  /**
   * Same as {@link #httpSampler(String)} but allowing to set a name to the HTTP Request sampler.
   *
   * @see #httpSampler(String)
   */
  public static DslHttpSampler httpSampler(String name, String url) {
    return new DslHttpSampler(name, url);
  }

  /**
   * Builds an HTTP header manager which allows setting HTTP headers to be used by HTTPRequest
   * samplers.
   *
   * @return the HTTP header manager instance
   * @see HttpHeaders
   */
  public static HttpHeaders httpHeaders() {
    return new HttpHeaders();
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
    return new DslJsr223PostProcessor(script);
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
   * Builds a Backend Listener configured to use InfluxDB to send all results for easy tracing,
   * historic, comparison and live test results.
   *
   * @param influxDbUrl is the URL to connect to the InfluxDB instance where test results should be
   * sent.
   * @return the Backend Listener instance.
   * @see InfluxDbBackendListener
   */
  public static InfluxDbBackendListener influxDbListener(String influxDbUrl) {
    return new InfluxDbBackendListener(influxDbUrl);
  }

}
