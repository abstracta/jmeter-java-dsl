package us.abstracta.jmeter.javadsl.core.listeners;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SimpleDataWriter;
import us.abstracta.jmeter.javadsl.core.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.MultiLevelTestElement;

/**
 * Allows to generate a result log file (JTL) with data for each sample for a test plan, thread
 * group or sampler, depending on what level of test plan is added.
 *
 * If jtlWriter is added at testPlan level it will log information about all samples in the test
 * plan, if added at thread group level it will only log samples for samplers contained within it,
 * if added as a sampler child, then only that sampler samples will be logged.
 *
 * By default, this writer will use JMeter default JTL format, a csv with following fields:
 * timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,
 * bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect. You can change the format to
 * XML and specify additional (or remove existing ones) fields to store with provided methods.
 *
 * See <a href="http://jmeter.apache.org/usermanual/listeners.html">JMeter listeners doc</a> for
 * more details on JTL format and settings.
 *
 * @since 0.1
 */
public class JtlWriter extends BaseTestElement implements MultiLevelTestElement {

  private final String jtlFile;
  private boolean saveAsXml;
  private boolean saveElapsedTime = true;
  private boolean saveResponseMessage = true;
  private boolean saveSuccess = true;
  private boolean saveSentByteCount = true;
  private boolean saveResponseFilename;
  private boolean saveEncoding;
  private boolean saveIdleTime = true;
  private boolean saveResponseHeaders;
  private boolean saveAssertionResults = true;
  private boolean saveFieldNames = true;
  private boolean saveLabel = true;
  private boolean saveThreadName = true;
  private boolean saveAssertionFailureMessage = true;
  private boolean saveActiveThreadCounts = true;
  private boolean saveLatency = true;
  private boolean saveSampleAndErrorCounts;
  private boolean saveRequestHeaders;
  private boolean saveResponseData;
  private boolean saveTimeStamp = true;
  private boolean saveResponseCode = true;
  private boolean saveDataType = true;
  private boolean saveReceivedByteCount = true;
  private boolean saveUrl = true;
  private boolean saveConnectTime = true;
  private boolean saveHostname;
  private boolean saveSamplerData;
  private boolean saveSubResults = true;
  private boolean overwriteJtl = false;
  private List<String> sampleVariables = Collections.emptyList();

  public JtlWriter(String jtlFile) {
    super("Simple Data Writer", SimpleDataWriter.class);
    this.jtlFile = jtlFile;
  }

  @Override
  public TestElement buildTestElement() {
    File file = new File(jtlFile);
    if (file.exists()) {
      if (overwriteJtl) {
        file.delete();
      } else {
        throw new IllegalArgumentException(new FileAlreadyExistsException(jtlFile));
      }
    }
    ResultCollector logger = new ResultCollector();
    logger.setFilename(jtlFile);
    SampleSaveConfiguration config = logger.getSaveConfig();
    config.setAsXml(saveAsXml);
    config.setTime(saveElapsedTime);
    config.setMessage(saveResponseMessage);
    config.setSuccess(saveSuccess);
    config.setSentBytes(saveSentByteCount);
    config.setFileName(saveResponseFilename);
    config.setEncoding(saveEncoding);
    config.setIdleTime(saveIdleTime);
    config.setResponseHeaders(saveResponseHeaders);
    config.setAssertions(saveAssertionResults);
    config.setFieldNames(saveFieldNames);
    config.setLabel(saveLabel);
    config.setThreadName(saveThreadName);
    config.setAssertionResultsFailureMessage(saveAssertionFailureMessage);
    config.setThreadCounts(saveActiveThreadCounts);
    config.setLatency(saveLatency);
    config.setSampleCount(saveSampleAndErrorCounts);
    config.setRequestHeaders(saveRequestHeaders);
    config.setResponseData(saveResponseData);
    config.setTimestamp(saveTimeStamp);
    config.setCode(saveResponseCode);
    config.setDataType(saveDataType);
    config.setBytes(saveReceivedByteCount);
    config.setUrl(saveUrl);
    config.setConnectTime(saveConnectTime);
    config.setHostname(saveHostname);
    config.setSamplerData(saveSamplerData);
    config.setSubresults(saveSubResults);
    if (!sampleVariables.isEmpty()) {
      JMeterUtils.setProperty("sample_variables", String.join(",", sampleVariables));
    }
    return logger;
  }

  /**
   * Allows setting if all or none fields are enabled when saving the JTL.
   *
   * If you enable them all, then XML format will be used.
   *
   * Take into consideration that having a JTL writer with no fields enabled makes no sense. But,
   * you may want to disable all fields to then enable specific ones, and not having to manually
   * disable each of default included fields manually. The same applies when you want most of the
   * fields except for some: in such case you can enable all and then manually disable the ones that
   * you want to exclude.
   *
   * Also take into consideration that the more fields you add to JTL writer, the more time JMeter
   * will spend on saving the information, and the more disk the file will consume. So, include
   * fields thoughtfully.
   *
   * @param enabled specifies whether enable or disable all fields.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withAllFields(boolean enabled) {
    saveAsXml = enabled;
    saveElapsedTime = enabled;
    saveResponseMessage = enabled;
    saveSuccess = enabled;
    saveSentByteCount = enabled;
    saveResponseFilename = enabled;
    saveEncoding = enabled;
    saveIdleTime = enabled;
    saveResponseHeaders = enabled;
    saveAssertionResults = enabled;
    saveFieldNames = enabled;
    saveLabel = enabled;
    saveThreadName = enabled;
    saveAssertionFailureMessage = enabled;
    saveActiveThreadCounts = enabled;
    saveLatency = enabled;
    saveSampleAndErrorCounts = enabled;
    saveRequestHeaders = enabled;
    saveResponseData = enabled;
    saveTimeStamp = enabled;
    saveResponseCode = enabled;
    saveDataType = enabled;
    saveReceivedByteCount = enabled;
    saveUrl = enabled;
    saveConnectTime = enabled;
    saveHostname = enabled;
    saveSamplerData = enabled;
    saveSubResults = enabled;
    return this;
  }

  /**
   * Allows specifying to use XML or CSV format for saving JTL.
   *
   * Take into consideration that some fields (like requestHeaders, responseHeaders, etc.) will only
   * be saved when XML format is used.
   *
   * @param enabled specifies whether enable XML format saving, or disable it (and use CSV).
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter saveAsXml(boolean enabled) {
    this.saveAsXml = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include elapsed time (milliseconds spent in each sample) in
   * generated JTL.
   *
   * This is usually the most important metric to collect during a performance test, so in general
   * this should be included.
   *
   * @param enabled specifies whether enable or disable inclusion of elapsed time.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withElapsedTime(boolean enabled) {
    this.saveElapsedTime = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include response message (eg: "OK" for HTTP 200 status code)
   * in generated JTL.
   *
   * This property is usually handy to trace potential issues, specially the ones that are not
   * standard issues (like HTTPConnectionExceptions) which are not deducible from response code.
   *
   * @param enabled specifies whether enable or disable inclusion of response message.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withResponseMessage(boolean enabled) {
    this.saveResponseMessage = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include success (a boolean indicating if request was success
   * or not) field in generated JTL.
   *
   * This property is usually handy to easily identify if a request failed or not (either due to
   * default JMeter logic, or due to some assertion check or post processor alteration).
   *
   * @param enabled specifies whether enable or disable inclusion of success field.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withSuccess(boolean enabled) {
    this.saveSuccess = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include sent bytes count (number of bytes sent to server by
   * request) field in generated JTL.
   *
   * This property is helpful when requests are dynamically generated or when you want to easily
   * evaluate how much data/load has been transferred to the server.
   *
   * @param enabled specifies whether enable or disable inclusion of sent bytes count.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withSentByteCount(boolean enabled) {
    this.saveSentByteCount = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include response file name (name of file stored by {@link
   * ResponseFileSaver}) field in generated JTL.
   *
   * This property is helpful when ResponseFileSaver is used to easily trace the request response
   * contents and don't have to include them in JTL file itself.
   *
   * @param enabled specifies whether enable or disable inclusion of response file name.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withResponseFilename(boolean enabled) {
    this.saveResponseFilename = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include the response encoding (eg: UTF-8, ISO-8859-1, etc.)
   * field in generated JTL.
   *
   * @param enabled specifies whether enable or disable inclusion of response encoding.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withEncoding(boolean enabled) {
    this.saveEncoding = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include the Idle time (milliseconds spent in JMeter
   * processing, but not sampling, generally 0) field in generated JTL.
   *
   * @param enabled specifies whether enable or disable inclusion of idle time.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withIdleTime(boolean enabled) {
    this.saveIdleTime = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include response headers (eg: HTTP headers like Content-Type
   * and the like) field in generated JTL.
   *
   * <b>Note:</b> this field will only be saved if {@link #saveAsXml(boolean)} is also set to
   * true.
   *
   * @param enabled specifies whether enable or disable inclusion of response headers.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withResponseHeaders(boolean enabled) {
    this.saveResponseHeaders = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include assertion results (with name, success field, and
   * potential error message) info in generated JTL.
   *
   * <b>Note:</b> this will only be saved if {@link #saveAsXml(boolean)} is also set to
   * true.
   *
   * This info is handy when tracing why requests are marked as failure and exact reason.
   *
   * @param enabled specifies whether enable or disable inclusion of assertion results.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withAssertionResults(boolean enabled) {
    this.saveAssertionResults = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include assertion results (with name, success field, and
   * potential error message) info in generated JTL.
   *
   * <b>Note:</b> this will only be saved if {@link #saveAsXml(boolean)} is set to false (or not
   * set, which defaults XML save to false).
   *
   * @param enabled specifies whether enable or disable inclusion of assertion results.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withFieldNames(boolean enabled) {
    this.saveFieldNames = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include sample label (i.e.: name of the request) field in
   * generated JTL.
   *
   * In general, you should enable this field to properly identify results to associated samplers.
   *
   * @param enabled specifies whether enable or disable inclusion of sample labels.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withLabel(boolean enabled) {
    this.saveLabel = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include thread name field in generated JTL.
   *
   * This is helpful to identify the requests generated by each thread and allow tracing
   * "correlated" requests (requests that are associated to previous requests in same thread).
   *
   * @param enabled specifies whether enable or disable inclusion of thread name.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withThreadName(boolean enabled) {
    this.saveThreadName = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include assertion failure message field in generated JTL.
   *
   * This is helpful to trace potential reason of a request being marked as failure.
   *
   * @param enabled specifies whether enable or disable inclusion of assertion failure message.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withAssertionFailureMessage(boolean enabled) {
    this.saveAssertionFailureMessage = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include active thread counts (basically, number of concurrent
   * requests, both in the sample thread group, and in all thread groups) fields in generated JTL.
   *
   * This is helpful to know under how much load (concurrent requests) is the tested service at the
   * moment the request was done.
   *
   * @param enabled specifies whether enable or disable inclusion of active thread counts.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withActiveThreadCounts(boolean enabled) {
    this.saveActiveThreadCounts = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include latency time (milliseconds between the sample started
   * and first byte of response is received) field in generated JTL.
   *
   * This is usually helpful to identify how fast does the tested service takes to answer, taking
   * out the time spent in transferring response data.
   *
   * @param enabled specifies whether enable or disable inclusion of latency time.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withLatency(boolean enabled) {
    this.saveLatency = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include sample counts (total and error counts) fields in
   * generated JTL.
   *
   * In general sample count will be 1, and error count will be 0 or 1 depending on sample success
   * or failure. But there are some scenarios where these counts might be greater, for example when
   * controllers results are being included.
   *
   * @param enabled specifies whether enable or disable inclusion of sample counts.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withSampleAndErrorCounts(boolean enabled) {
    this.saveSampleAndErrorCounts = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include request headers (eg: HTTP headers like User-Agent and
   * the like) field in generated JTL.
   *
   * <b>Note:</b> this field will only be saved if {@link #saveAsXml(boolean)} is also set to
   * true.
   *
   * @param enabled specifies whether enable or disable inclusion of request headers.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withRequestHeaders(boolean enabled) {
    this.saveRequestHeaders = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include response body field in generated JTL.
   *
   * <b>Note:</b> this field will only be saved if {@link #saveAsXml(boolean)} is also set to
   * true.
   *
   * This is usually helpful for tracing the response obtained by each sample. Consider using {@link
   * ResponseFileSaver} to get a file for each response body.
   *
   * @param enabled specifies whether enable or disable inclusion of response body.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withResponseData(boolean enabled) {
    this.saveResponseData = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include timestamp (epoch when the sample started) field in
   * generated JTL.
   *
   * @param enabled specifies whether enable or disable inclusion of timestamps.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withTimeStamp(boolean enabled) {
    this.saveTimeStamp = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include response codes (e.g.: 200) field in generated JTL.
   *
   * This field allows to quickly identify different reasons for failure in server (eg: bad request,
   * service temporally unavailable, etc.).
   *
   * @param enabled specifies whether enable or disable inclusion of response codes.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withResponseCode(boolean enabled) {
    this.saveResponseCode = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include response data type (i.e.: binary or text) field in
   * generated JTL.
   *
   * @param enabled specifies whether enable or disable inclusion of response data types.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withDataType(boolean enabled) {
    this.saveDataType = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include received bytes count (number of bytes sent by server
   * in the response) field in generated JTL.
   *
   * This property is helpful to measure how much load is the network getting and how much
   * information is the tested service generating.
   *
   * @param enabled specifies whether enable or disable inclusion of received bytes counts.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withReceivedByteCount(boolean enabled) {
    this.saveReceivedByteCount = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include url field in generated JTL.
   *
   * This property is helpful when URLs are dynamically generated and may vary for the sample
   * sampler
   *
   * @param enabled specifies whether enable or disable inclusion of urls.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withUrl(boolean enabled) {
    this.saveUrl = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include connect time (milliseconds between the sample started
   * and connection is established to service to start sending request) field in generated JTL.
   *
   * This is usually helpful to identify issues in network latency when connecting or server load
   * when serving connection requests.
   *
   * @param enabled specifies whether enable or disable inclusion of connect time.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withConnectTime(boolean enabled) {
    this.saveConnectTime = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include host name (name of host that did the sample) field in
   * generated JTL.
   *
   * This particularly helpful when running JMeter in a distributed fashion to identify which node
   * the sample result is associated to.
   *
   * @param enabled specifies whether enable or disable inclusion of host names.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withHostname(boolean enabled) {
    this.saveHostname = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include sampler data (like cookies, HTTP method, request body
   * and redirection URL) entries in generated JTL.
   *
   * <b>Note:</b> this field will only be saved if {@link #saveAsXml(boolean)} is also set to
   * true.
   *
   * @param enabled specifies whether enable or disable inclusion of sample data.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withSamplerData(boolean enabled) {
    this.saveSamplerData = enabled;
    return this;
  }

  /**
   * Allows setting whether or not to include sub results (like redirects) entries in generated
   * JTL.
   *
   * @param enabled specifies whether enable or disable inclusion of sub results.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter withSubResults(boolean enabled) {
    this.saveSubResults = enabled;
    return this;
  }

  /**
   * Allows specifying that if a JTL with provided name exists, then it should be overwritten (and
   * avoid default generated exception).
   *
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.13
   */
  public JtlWriter overwriteJtl() {
    this.overwriteJtl = true;
    return this;
  }

  /**
   * Allows specifying JMeter variables to include in generated jtl file.
   *
   * <b>Warning:</b> variables to sample are test plan wide. This means that if you set them in one
   * jtl writer, they will appear in all jtl writers used in the test plan. Moreover, if you set
   * them in different jtl writers, only variables set on latest one will be considered.
   *
   * @param variables names of JMeter variables to include in jtl file.
   * @return the JtlWriter instance to be able to specify additional options in a fluent API way.
   * @since 0.22
   */
  public JtlWriter withVariables(String... variables) {
    this.sampleVariables = Arrays.asList(variables);
    return this;
  }

}
