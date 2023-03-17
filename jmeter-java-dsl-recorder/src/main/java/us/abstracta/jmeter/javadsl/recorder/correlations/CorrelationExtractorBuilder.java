package us.abstracta.jmeter.javadsl.recorder.correlations;

import com.blazemeter.jmeter.correlation.core.extractors.CorrelationExtractor;
import com.blazemeter.jmeter.correlation.core.extractors.RegexCorrelationExtractor;
import com.blazemeter.jmeter.correlation.core.extractors.ResultField;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.regex.Pattern;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslRegexExtractor;
import us.abstracta.jmeter.javadsl.recorder.correlations.CorrelationExtractorBuilder.CorrelationExtractorDeserializer;

@JsonDeserialize(using = CorrelationExtractorDeserializer.class)
public class CorrelationExtractorBuilder {

  private final Pattern regex;
  private TargetField target = TargetField.RESPONSE_BODY;

  public CorrelationExtractorBuilder(Pattern regex) {
    this.regex = regex;
  }

  /**
   * Allows specifying over which part of sample results to try applying the extractor.
   *
   * @param target specifies the field to apply the extractor to.
   * @return the CorrelationExtractorBuilder for further usage.
   */
  public CorrelationExtractorBuilder target(TargetField target) {
    this.target = target;
    return this;
  }

  public CorrelationExtractor<?> build() {
    return new RegexCorrelationExtractor<>(regex.toString(), "1", "1",
        target.resultField.name(), "true");
  }

  public enum TargetField {
    /**
     * @see DslRegexExtractor.TargetField#RESPONSE_BODY
     */
    RESPONSE_BODY(ResultField.BODY),
    /**
     * @see DslRegexExtractor.TargetField#RESPONSE_BODY_UNESCAPED
     */
    RESPONSE_BODY_UNESCAPED(ResultField.BODY_UNESCAPED),
    /**
     * @see DslRegexExtractor.TargetField#RESPONSE_BODY_AS_DOCUMENT
     */
    RESPONSE_BODY_AS_DOCUMENT(ResultField.BODY_AS_A_DOCUMENT),
    /**
     * @see DslRegexExtractor.TargetField#RESPONSE_HEADERS
     */
    RESPONSE_HEADERS(ResultField.RESPONSE_HEADERS),
    /**
     * @see DslRegexExtractor.TargetField#REQUEST_HEADERS
     */
    REQUEST_HEADERS(ResultField.REQUEST_HEADERS),
    /**
     * @see DslRegexExtractor.TargetField#REQUEST_URL
     */
    REQUEST_URL(ResultField.URL),
    /**
     * @see DslRegexExtractor.TargetField#RESPONSE_CODE
     */
    RESPONSE_CODE(ResultField.RESPONSE_CODE),
    /**
     * @see DslRegexExtractor.TargetField#RESPONSE_MESSAGE
     */
    RESPONSE_MESSAGE(ResultField.RESPONSE_MESSAGE);

    private final ResultField resultField;

    TargetField(ResultField resultField) {
      this.resultField = resultField;
    }

  }

  public static class CorrelationExtractorDeserializer extends
      JsonDeserializer<CorrelationExtractorBuilder> {

    @Override
    public CorrelationExtractorBuilder deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      JsonToken token = p.getCurrentToken();
      if (token == JsonToken.VALUE_STRING) {
        return new CorrelationExtractorBuilder(Pattern.compile(p.getValueAsString()));
      } else {
        return p.readValueAs(CorrelationExtractorBuilderPojo.class);
      }
    }

  }

  @JsonDeserialize
  public static class CorrelationExtractorBuilderPojo extends CorrelationExtractorBuilder {

    @JsonCreator
    public CorrelationExtractorBuilderPojo(@JsonProperty("regex") Pattern regex) {
      super(regex);
    }

  }

}
