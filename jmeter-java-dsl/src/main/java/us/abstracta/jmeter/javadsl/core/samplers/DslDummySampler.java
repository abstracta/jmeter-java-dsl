package us.abstracta.jmeter.javadsl.core.samplers;

import java.time.Duration;
import kg.apc.jmeter.dummy.DummyElement;
import kg.apc.jmeter.samplers.DummySampler;
import kg.apc.jmeter.samplers.DummySamplerGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.core.util.JmeterFunction;

/**
 * Allows using JMeter Dummy Sampler plugin to emulate other samples and ease testing post
 * processors and other parts of a test plan.
 * <p>
 * By default, this element is set with no request, url, response code=200, response message = OK,
 * and response time with random value between 50 and 500 milliseconds. Additionally, emulation of
 * response times (through sleeps) is disabled to speed up testing.
 *
 * @since 0.46
 */
public class DslDummySampler extends BaseSampler<DslDummySampler> {

  protected String responseBody;
  protected boolean successful = true;
  protected String responseCode = "200";
  protected String responseMessage = "OK";
  protected String responseTime = JmeterFunction.from("__Random", 50, 500);
  protected boolean simulateResponseTime;
  protected String url = "";
  protected String requestBody = "";

  public DslDummySampler(String name, String responseBody) {
    super(name == null ? "jp@gc - Dummy Sampler" : name, DummySamplerGui.class);
    this.responseBody = responseBody;
  }

  /**
   * Allows generating successful or unsuccessful sample results for this sampler.
   *
   * @param successful when true, generated sample result will be successful, otherwise it will be
   *                   marked as failure. When not specified, successful sample results are
   *                   generated.
   * @return the sampler for further configuration or usage.
   */
  public DslDummySampler successful(boolean successful) {
    this.successful = successful;
    return this;
  }

  /**
   * Specifies the response code included in generated sample results.
   *
   * @param code defines the response code included in sample results. When not set, 200 is used.
   * @return the sampler for further configuration or usage.
   */
  public DslDummySampler responseCode(String code) {
    this.responseCode = code;
    return this;
  }

  /**
   * Specifies the response message included in generated sample results.
   *
   * @param message defines the response message included in sample results. When not set, OK is
   *                used.
   * @return the sampler for further configuration or usage.
   */
  public DslDummySampler responseMessage(String message) {
    this.responseMessage = message;
    return this;
  }

  /**
   * Specifies the response time used in generated sample results.
   *
   * @param responseTime defines the response time associated to the sample results. When not set, a
   *                     randomly calculated value between 50 and 500 milliseconds is used.
   * @return the sampler for further configuration or usage.
   */
  public DslDummySampler responseTime(Duration responseTime) {
    this.responseTime = String.valueOf(responseTime.toMillis());
    return this;
  }

  /**
   * Specifies if used response time should be simulated (the sample will sleep for the given
   * duration) or not.
   * <p>
   * Having simulation disabled allows for really fast emulation and trial of test plan, which is
   * very handy when debugging. If you need a more accurate emulation in more advanced cases, like
   * you don't want to generate too many requests per second, and you want a behavior closer to the
   * real thing, then consider enabling response time simulation.
   *
   * @param simulate when true enables simulation of response times, when false no wait is done
   *                 speeding up test plan execution. By default, simulation is disabled.
   * @return the sampler for further configuration or usage.
   */
  public DslDummySampler simulateResponseTime(boolean simulate) {
    this.simulateResponseTime = simulate;
    return this;
  }

  /**
   * Specifies the URL used in generated sample results.
   * <p>
   * This might be helpful in scenarios where extractors, pre-processors or other test plan elements
   * depend on the URL.
   *
   * @param url defines the URL associated to generated sample results. When not set, an empty URL
   *            is used.
   * @return the sampler for further configuration or usage.
   */
  public DslDummySampler url(String url) {
    this.url = url;
    return this;
  }

  /**
   * Specifies the request body used in generated sample results.
   * <p>
   * This might be helpful in scenarios where extractors, pre-processors or other test plan elements
   * depend on the request body.
   *
   * @param requestBody defines the request body associated to generated sample results. When not
   *                    set, an empty body is used.
   * @return the sampler for further configuration or usage.
   */
  public DslDummySampler requestBody(String requestBody) {
    this.requestBody = requestBody;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    DummySampler ret = new DummySampler();
    DummyElement dummy = ret.getDummy();
    dummy.setResponseData(responseBody);
    dummy.setSuccessful(successful);
    dummy.setResponseCode(responseCode);
    dummy.setResponseMessage(responseMessage);
    dummy.setResponseTime(responseTime);
    dummy.setSimulateWaiting(simulateResponseTime);
    dummy.setURL(url);
    dummy.setRequestData(requestBody);
    return ret;
  }

}
