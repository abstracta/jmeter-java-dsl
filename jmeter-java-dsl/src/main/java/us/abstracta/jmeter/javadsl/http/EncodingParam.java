package us.abstracta.jmeter.javadsl.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;

public class EncodingParam extends MethodParam<Charset> {

  private static final Map<Charset, String> STANDARD_CHARSETS_NAMES =
      findConstantNames(StandardCharsets.class, Charset.class, s -> true);

  protected EncodingParam(TestElementParamBuilder paramBuilder) {
    super(Charset.class, buildCharset(paramBuilder), null);
  }

  private static Charset buildCharset(TestElementParamBuilder paramBuilder) {
    StringParam charset = paramBuilder.stringParam(HTTPSamplerBase.CONTENT_ENCODING, "");
    return charset.isDefault() ? null : Charset.forName(charset.getValue());
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
