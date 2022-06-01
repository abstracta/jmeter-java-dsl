package us.abstracta.jmeter.javadsl.http;

import java.nio.charset.Charset;
import java.util.Map;
import org.apache.http.entity.ContentType;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodParam;

public class ContentTypeParam extends MethodParam<ContentType> {

  private static final Map<ContentType, String> CONSTANT_CONTENT_TYPES = findConstantNames(
      ContentType.class, ContentType.class,
      f -> !"DEFAULT_TEXT".equals(f.getName()) && !"DEFAULT_BINARY".equals(f.getName()));

  protected ContentTypeParam(String value) {
    super(ContentType.class, value, ContentType::parse, null);
  }

  @Override
  public String buildSpecificCode(String indent) {
    String contentTypeName = findConstantName(value);
    if (contentTypeName != null) {
      return ContentType.class.getSimpleName() + "." + contentTypeName;
    }
    Charset charset = value.getCharset();
    StringParam mimeTypeParam = new StringParam(value.getMimeType());
    MethodParam<?>[] params = charset != null
        ? new MethodParam[]{mimeTypeParam, new StringParam(value.getCharset().name())}
        : new MethodParam[]{mimeTypeParam};
    return MethodCall.forStaticMethod(ContentType.class, "create", params)
        .buildCode();
  }

  private String findConstantName(ContentType value) {
    return CONSTANT_CONTENT_TYPES.entrySet().stream()
        .filter(entry -> entry.getKey().toString().equals(value.toString()))
        .map(Map.Entry::getValue)
        .findAny()
        .orElse(null);
  }

}
