package us.abstracta.jmeter.javadsl.http;

import java.util.HashMap;
import java.util.Map;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.testelement.TestElement;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import us.abstracta.jmeter.javadsl.core.BaseTestElement;
import us.abstracta.jmeter.javadsl.core.DslSampler.SamplerChild;
import us.abstracta.jmeter.javadsl.core.DslTestPlan.TestPlanChild;
import us.abstracta.jmeter.javadsl.core.DslThreadGroup.ThreadGroupChild;

/**
 * This class allows specifying HTTP headers (through an underlying JMeter HttpHeaderManager) to be
 * used by HTTP samplers.
 *
 * This test element can be added at different levels (in the same way as HTTPHeaderManager) of a
 * test plan affecting all samplers in the scope were is added. For example if httpHeaders is
 * specified at test plan, then all headers will apply to http samplers; if it is specified on
 * thread group, then only samplers on that thread group would be affected; if specified as a child
 * of a sampler, only the particular sampler will include such headers. Also take into consideration
 * that headers specified at lower scope will overwrite ones specified at higher scope (eg: sampler
 * child headers will overwrite test plan headers).
 */
public class HttpHeaders extends BaseTestElement implements SamplerChild, ThreadGroupChild,
    TestPlanChild {

  private final Map<String, String> headers = new HashMap<>();

  public HttpHeaders() {
    super("HTTP Header Manager", HeaderPanel.class);
  }

  public HttpHeaders header(String name, String value) {
    headers.put(name, value);
    return this;
  }

  public HttpHeaders contentType(MimeTypes.Type contentType) {
    headers.put(HttpHeader.CONTENT_TYPE.toString(), contentType.toString());
    return this;
  }

  public boolean isEmpty() {
    return headers.isEmpty();
  }

  @Override
  protected TestElement buildTestElement() {
    HeaderManager ret = new HeaderManager();
    headers.forEach((name, value) -> ret.add(new Header(name, value)));
    return ret;
  }

}
