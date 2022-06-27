package us.abstracta.jmeter.javadsl.http;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.FixedParam;
import us.abstracta.jmeter.javadsl.http.DslHttpSampler.HttpClientImpl;

public class ClientImplParam extends FixedParam<HttpClientImpl> {

  private ClientImplParam(String expression, HttpClientImpl defaultValue) {
    super(HttpClientImpl.class, expression, HttpClientImpl::fromPropertyValue, defaultValue);
  }

  public static MethodParam from(TestElementParamBuilder paramBuilder) {
    return paramBuilder.buildParam(HTTPSamplerBase.IMPLEMENTATION, ClientImplParam::new,
        HttpClientImpl.HTTP_CLIENT);
  }

  @Override
  public String buildCode(String indent) {
    return HttpClientImpl.class.getSimpleName() + "." + value.name();
  }

}
