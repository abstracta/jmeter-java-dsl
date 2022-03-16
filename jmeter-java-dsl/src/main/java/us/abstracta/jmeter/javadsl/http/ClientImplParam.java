package us.abstracta.jmeter.javadsl.http;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler.HttpClientImpl;

public class ClientImplParam extends MethodParam<HttpClientImpl> {

  protected ClientImplParam(TestElementParamBuilder paramBuilder) {
    super(HttpClientImpl.class,
        HttpClientImpl.fromPropertyValue(
            paramBuilder.prop(HTTPSamplerBase.IMPLEMENTATION).getStringValue()),
        HttpClientImpl.HTTP_CLIENT);
  }

  @Override
  public String buildCode(String indent) {
    return HttpClientImpl.class.getSimpleName() + "." + value.name();
  }

}
