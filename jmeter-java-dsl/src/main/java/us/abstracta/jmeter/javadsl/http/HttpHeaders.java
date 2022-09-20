package us.abstracta.jmeter.javadsl.http;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;
import us.abstracta.jmeter.javadsl.core.configs.BaseConfigElement;

/**
 * Allows specifying HTTP headers (through an underlying JMeter HttpHeaderManager) to be used by
 * HTTP samplers.
 * <p>
 * This test element can be added at different levels (in the same way as HTTPHeaderManager) of a
 * test plan affecting all samplers in the scope were is added. For example if httpHeaders is
 * specified at test plan, then all headers will apply to http samplers; if it is specified on
 * thread group, then only samplers on that thread group would be affected; if specified as a child
 * of a sampler, only the particular sampler will include such headers. Also take into consideration
 * that headers specified at lower scope will overwrite ones specified at higher scope (eg: sampler
 * child headers will overwrite test plan headers).
 *
 * @since 0.1
 */
public class HttpHeaders extends BaseConfigElement {

  protected final Map<String, String> headers = new LinkedHashMap<>();

  public HttpHeaders() {
    super("HTTP Header Manager", HeaderPanel.class);
  }

  /**
   * Allows to set an HTTP header to be used by HTTP samplers.
   * <p>
   * To specify multiple headers just invoke this method several times with the different header
   * names and values.
   *
   * @param name  of the HTTP header.
   * @param value of the HTTP header.
   * @return the config element for further configuration or usage.
   */
  public HttpHeaders header(String name, String value) {
    headers.put(name, value);
    return this;
  }

  /**
   * Allows to easily specify the Content-Type HTTP header.
   *
   * @param contentType value to use as Content-Type header.
   * @return the config element for further configuration or usage.
   * @since 0.42
   */
  public HttpHeaders contentType(ContentType contentType) {
    return header(HTTPConstants.HEADER_CONTENT_TYPE, contentType.toString());
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

  public static class CodeBuilder extends SingleTestElementCallBuilder<HeaderManager> {

    public CodeBuilder(List<Method> builderMethods) {
      super(HeaderManager.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(HeaderManager testElement, MethodCallContext context) {
      MethodCall ret = buildMethodCall();
      for (JMeterProperty prop : (CollectionProperty) testElement.getProperty(
          HeaderManager.HEADERS)) {
        Header header = (Header) prop.getObjectValue();
        if (HTTPConstants.HEADER_CONTENT_TYPE.equals(header.getName())) {
          ret.chain("contentType", new ContentTypeParam(header.getValue()));
        } else {
          ret.chain("header", new StringParam(header.getName()),
              new StringParam(header.getValue()));
        }
      }
      return ret;
    }

  }

}
