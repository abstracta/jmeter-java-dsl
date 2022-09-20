package us.abstracta.jmeter.javadsl.core.util;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;

/**
 * Handles creation of dynamic expressions used in test plan.
 * <p>
 * This class allows either to set the expression plainly as it should be evaluated by JMeter, or
 * provide Java code that is converted to groovy script for JMeter execution.
 * <p>
 * Any test element that wants to be able to handle an expression that can be set both with string
 * or java code, should extend {@link DslScriptBuilder.DslScript} and {@link
 * DslScriptBuilder.DslScriptVars} providing variables that are available for groovy scripts in the
 * particular scenario.
 *
 * @since 0.27
 */
public class DslScriptBuilder {

  private static int currentScriptId = 1;
  protected final String scriptString;
  private final int scriptId;
  private final DslScript<?, ?> script;
  private final Class<?> varsClass;
  private final Map<String, String> varsNameMapping;

  public DslScriptBuilder(DslScript<?, ?> script, Class<?> varsClass,
      Map<String, String> varsNameMapping) {
    this(currentScriptId++, script, varsClass, varsNameMapping, null);
  }

  private DslScriptBuilder(int scriptId, DslScript<?, ?> script, Class<?> varsClass,
      Map<String, String> varsNameMapping, String scriptString) {
    this.scriptId = scriptId;
    this.script = script;
    this.varsClass = varsClass;
    this.varsNameMapping = varsNameMapping;
    this.scriptString = scriptString;
  }

  public DslScriptBuilder(String script) {
    this(0, null, null, null, script);
  }

  public String build() {
    return scriptString != null ? scriptString
        : "// It is currently not supported to run scripts defined in Java code in non embedded "
            + "JMeter Engine (eg: BlazeMeter, standalone JMeter GUI, etc).\n"
            + buildScriptString(registerScriptProperty(), varsClass, varsNameMapping);
  }

  private String registerScriptProperty() {
    String scriptPropName = "groovyScript" + scriptId;
    JMeterUtils.getJMeterProperties().put(scriptPropName, script);
    return scriptPropName;
  }

  private static String buildScriptString(String scriptId, Class<?> varsClass,
      Map<String, String> varsNameMapping) {
    return "props.get('" + scriptId + "').run(new " + varsClass.getName() + "("
        + buildConstructorParameters(varsClass, varsNameMapping) + "))";
  }

  private static String buildConstructorParameters(Class<?> varsClass,
      Map<String, String> varsNameMapping) {
    return Arrays.stream(varsClass.getFields())
        .map(Field::getName)
        .map(f -> varsNameMapping.getOrDefault(f, f))
        .collect(Collectors.joining(","));
  }

  public interface DslScript<P extends DslScriptVars, R> {

    R run(P scriptVars) throws Exception;

  }

  public abstract static class DslScriptVars {

    public final SampleResult prev;
    public final JMeterContext ctx;
    public final JMeterVariables vars;
    public final Properties props;
    public final Sampler sampler;
    public final Logger log;

    public DslScriptVars(SampleResult prev, JMeterContext ctx, JMeterVariables vars,
        Properties props,
        Sampler sampler, Logger log) {
      this.prev = prev;
      this.ctx = ctx;
      this.vars = vars;
      this.props = props;
      this.sampler = sampler;
      this.log = log;
    }

    /**
     * Builds a map from last sample result to ease visualization and debugging.
     *
     * @return map from last sample result.
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
     */
    public String prevResponse() {
      String statusLine = prev instanceof HTTPSampleResult ? ""
          : prev.getResponseCode() + " " + prev.getResponseMessage() + "\n";
      return statusLine + prev.getResponseHeaders() + "\n" + prev.getResponseDataAsString();
    }

    /**
     * Gets a map from current JMeter variables, making them easier to visualize, mainly while
     * debugging.
     *
     * @return map created from JMeter variables.
     */
    public Map<String, Object> varsMap() {
      return vars.entrySet().stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

  }

}
