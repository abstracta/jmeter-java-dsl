package us.abstracta.jmeter.javadsl.codegeneration.params;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;

/**
 * Is a parameter with an encoding (Charset) value.
 *
 * @since 0.62
 */
public class EncodingParam extends FixedParam<Charset> {

  private static final Map<Charset, String> STANDARD_CHARSETS_NAMES =
      findConstantNames(StandardCharsets.class, Charset.class, s -> true);

  public EncodingParam(String expression, Charset defaultValue) {
    super(Charset.class, expression, Charset::forName, defaultValue);
  }

  public String buildCode(String indent) {
    String standardCharsetName = STANDARD_CHARSETS_NAMES.get(value);
    return standardCharsetName != null
        ? StandardCharsets.class.getSimpleName() + "." + standardCharsetName
        : MethodCall.forStaticMethod(Charset.class, "forName", new StringParam(getValue().name()))
            .buildCode();
  }

}
