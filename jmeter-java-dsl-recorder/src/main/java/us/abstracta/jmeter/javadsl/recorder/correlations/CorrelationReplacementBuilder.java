package us.abstracta.jmeter.javadsl.recorder.correlations;

import com.blazemeter.jmeter.correlation.core.replacements.CorrelationReplacement;
import com.blazemeter.jmeter.correlation.core.replacements.RegexCorrelationReplacement;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.util.regex.Pattern;
import us.abstracta.jmeter.javadsl.recorder.correlations.CorrelationReplacementBuilder.CorrelationReplacementDeserializer;

@JsonDeserialize(using = CorrelationReplacementDeserializer.class)
public class CorrelationReplacementBuilder {

  private final Pattern regex;

  public CorrelationReplacementBuilder(Pattern regex) {
    this.regex = regex;
  }

  public CorrelationReplacement<?> build() {
    return new RegexCorrelationReplacement<>(regex.toString());
  }

  public static class CorrelationReplacementDeserializer extends
      JsonDeserializer<CorrelationReplacementBuilder> {

    @Override
    public CorrelationReplacementBuilder deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException {
      JsonToken token = p.currentToken();
      if (token == JsonToken.VALUE_STRING) {
        return new CorrelationReplacementBuilder(Pattern.compile(p.getValueAsString()));
      }
      return p.readValueAs(CorrelationReplacementBuilderPojo.class);
    }

  }

  @JsonDeserialize
  public static class CorrelationReplacementBuilderPojo extends CorrelationReplacementBuilder {

    @JsonCreator
    public CorrelationReplacementBuilderPojo(@JsonProperty("regex") Pattern regex) {
      super(regex);
    }

  }

}
