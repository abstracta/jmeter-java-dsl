package us.abstracta.jmeter.javadsl.core.postprocessors;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.jmeter.extractor.JSR223PostProcessor;
import org.apache.jmeter.extractor.JSR223PostProcessorBeanInfo;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JSR223BeanInfoSupport;
import org.apache.jmeter.util.JSR223TestElement;
import org.slf4j.Logger;
import us.abstracta.jmeter.javadsl.core.DslJsr223TestElement;
import us.abstracta.jmeter.javadsl.core.MultiLevelTestElement;

/**
 * Allows running custom logic after getting a sample result.
 * <p>
 * This is a very powerful and flexible component that allows you to modify sample results (like
 * changing the flag if is success or not), jmeter variables, context settings, etc.
 * <p>
 * By default, provided script will be interpreted as groovy script, which is the default setting
 * for JMeter. If you need, you can use any of JMeter provided scripting languages (beanshell,
 * javascript, jexl, etc.) by setting the {@link #language(String)} property.
 *
 * @since 0.6
 */
public class DslJsr223PostProcessor extends DslJsr223TestElement implements MultiLevelTestElement {

  private static final String DEFAULT_NAME = "JSR223 PostProcessor";

  public DslJsr223PostProcessor(String name, String script) {
    super(name, DEFAULT_NAME, script);
  }

  public DslJsr223PostProcessor(String name, PostProcessorScript script) {
    super(name, DEFAULT_NAME, script, PostProcessorVars.class, Collections.emptyMap());
  }

  @Override
  protected JSR223TestElement buildJsr223TestElement() {
    return new JSR223PostProcessor();
  }

  @Override
  protected JSR223BeanInfoSupport getJsr223BeanInfo() {
    return new JSR223PostProcessorBeanInfo();
  }

  /**
   * Allows to use any java code as script.
   *
   * @see PostProcessorVars for a list of provided variables in script execution
   * @since 0.10
   */
  public interface PostProcessorScript extends Jsr223Script<PostProcessorVars> {

  }

  public static class PostProcessorVars extends Jsr223ScriptVars {

    public final SampleResult prev;

    public PostProcessorVars(SampleResult prev, JMeterContext ctx, JMeterVariables vars,
        Properties props, Sampler sampler, Logger log, String label) {
      super(ctx, vars, props, sampler, log, label);
      this.prev = prev;
    }

    /**
     * Builds a map from last sample result to ease visualization and debugging.
     *
     * @return map from last sample result.
     * @since 0.19
     */
    public Map<String, Object> prevMap() {
      Map<String, Object> ret = prevMetadata();
      ret.putAll(prevMetrics());
      ret.put("request", prevRequest());
      ret.put("response", prevResponse());
      return ret;
    }

    /**
     * Builds a map from last sample result including most significant metadata to ease
     * visualization and debugging.
     *
     * @return map of last sample result most significant metadata.
     * @since 0.19
     */
    public Map<String, Object> prevMetadata() {
      Map<String, Object> ret = new LinkedHashMap<>();
      ret.put("label", prev.getSampleLabel());
      ret.put("timestamp", Instant.ofEpochMilli(prev.getTimeStamp()));
      SampleResult parent = prev.getParent();
      if (parent != null) {
        ret.put("parent", parent.getSampleLabel());
      }
      ret.put("successful", prev.isSuccessful());
      ret.put("threadName", prev.getThreadName());
      ret.put("threadsCount", prev.getAllThreads());
      ret.put("threadGroupSize", prev.getGroupThreads());
      return ret;
    }

    /**
     * Builds a map from last sample result collected metrics to ease visualization and debugging.
     *
     * @return map of last sample collected metrics.
     * @since 0.19
     */
    public Map<String, Object> prevMetrics() {
      Map<String, Object> ret = new LinkedHashMap<>();
      ret.put("sampleMillis", prev.getTime());
      ret.put("connectionMillis", prev.getConnectTime());
      ret.put("latencyMillis", prev.getLatency());
      ret.put("sentBytes", prev.getSentBytes());
      ret.put("receivedBytes", prev.getBytesAsLong());
      return ret;
    }

    /**
     * Builds a string from last sample result request to ease visualization and debugging.
     *
     * @return string representing last sample request.
     * @since 0.19
     */
    public String prevRequest() {
      return prev instanceof HTTPSampleResult ? httpRequestString((HTTPSampleResult) prev)
          : prev.getRequestHeaders() + "\n" + prev.getSamplerData();
    }

    private String httpRequestString(HTTPSampleResult result) {
      String cookiesHeader = result.getCookies();
      if (cookiesHeader != null && !cookiesHeader.isEmpty()) {
        cookiesHeader = HTTPConstants.HEADER_COOKIE + ": " + cookiesHeader + "\n";
      }
      return result.getHTTPMethod() + " " + result.getUrlAsString() + "\n"
          + result.getRequestHeaders() + cookiesHeader + "\n"
          + result.getQueryString();
    }

    /**
     * Builds a string from last sample result response to ease visualization and debugging.
     *
     * @return string representing last sample response.
     * @since 0.19
     */
    public String prevResponse() {
      String statusLine = prev instanceof HTTPSampleResult ? ""
          : prev.getResponseCode() + " " + prev.getResponseMessage() + "\n";
      return statusLine + prev.getResponseHeaders() + "\n" + prev.getResponseDataAsString();
    }

  }

}
