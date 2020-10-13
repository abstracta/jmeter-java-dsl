package us.abstracta.jmeter.javadsl.http;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import us.abstracta.jmeter.javadsl.core.DslSampler;

/**
 * This allow to configure a JMeter HTTP sampler to make HTTP requests in a test plan.
 */
public class DslHttpSampler extends DslSampler {

  private final String url;
  private HttpMethod method = HttpMethod.GET;
  private final HttpHeaders headers = new HttpHeaders();
  private String body;

  public DslHttpSampler(String name, String url) {
    super(name != null ? name : "HTTP Request", HttpTestSampleGui.class, null);
    this.url = url;
  }

  public DslHttpSampler post(String body, MimeTypes.Type contentType) {
    return method(HttpMethod.POST)
        .contentType(contentType)
        .body(body);
  }

  public DslHttpSampler method(HttpMethod method) {
    this.method = method;
    return this;
  }

  public DslHttpSampler header(String name, String value) {
    headers.header(name, value);
    return this;
  }

  public DslHttpSampler contentType(MimeTypes.Type contentType) {
    headers.contentType(contentType);
    return this;
  }

  public DslHttpSampler body(String body) {
    this.body = body;
    return this;
  }

  public DslHttpSampler children(SamplerChild... children) {
    setChildren(children);
    return this;
  }

  @Override
  public TestElement buildTestElement() {
    HTTPSamplerProxy ret = new HTTPSamplerProxy();
    ret.setFollowRedirects(true);
    ret.setUseKeepAlive(true);
    ret.setPath(url);
    ret.setMethod(method.name());
    ret.setArguments(buildArguments());
    return ret;
  }

  private Arguments buildArguments() {
    Arguments args = new Arguments();
    if (body != null) {
      HTTPArgument arg = new HTTPArgument("", body, false);
      arg.setAlwaysEncoded(false);
      args.addArgument(arg);
    }
    return args;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent) {
    HashTree ret = super.buildTreeUnder(parent);
    if (!headers.isEmpty()) {
      headers.buildTreeUnder(ret);
    }
    return ret;
  }

}
