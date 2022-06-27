package us.abstracta.jmeter.javadsl.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.FixedParam;
import us.abstracta.jmeter.javadsl.codegeneration.params.StringParam;

public class EncodingParam extends FixedParam<Charset> {

  private static final Map<Charset, String> STANDARD_CHARSETS_NAMES =
      findConstantNames(StandardCharsets.class, Charset.class, s -> true);

  private EncodingParam(String expression, Charset defaultValue) {
    super(Charset.class, expression, Charset::forName, defaultValue);
  }

  public static MethodParam from(TestElementParamBuilder paramBuilder) {
    return paramBuilder.buildParam(HTTPSamplerBase.CONTENT_ENCODING, EncodingParam::new, null);
  }

  @Override
  public String buildCode(String indent) {
    String standardCharsetName = STANDARD_CHARSETS_NAMES.get(value);
    return standardCharsetName != null
        ? StandardCharsets.class.getSimpleName() + "." + standardCharsetName
        : MethodCall.forStaticMethod(Charset.class, "forName", new StringParam(getValue().name()))
            .buildCode();
  }

}
