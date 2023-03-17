package us.abstracta.jmeter.javadsl.recorder.correlations;

import com.blazemeter.jmeter.correlation.core.CorrelationRule;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.regex.Pattern;

public class CorrelationRuleBuilder {

  private final String variableName;
  private final CorrelationExtractorBuilder extractor;
  private final CorrelationReplacementBuilder replacement;

  public CorrelationRuleBuilder(String variableName, Pattern extractorRegex,
      Pattern replacementRegex) {
    this(variableName,
        extractorRegex != null ? new CorrelationExtractorBuilder(extractorRegex) : null,
        replacementRegex != null ? new CorrelationReplacementBuilder(replacementRegex) : null);
  }

  @JsonCreator
  public CorrelationRuleBuilder(@JsonProperty("variable") String variableName,
      @JsonProperty("extractor") CorrelationExtractorBuilder extractor,
      @JsonProperty("replacement") CorrelationReplacementBuilder replacement) {
    this.variableName = variableName;
    this.extractor = extractor;
    this.replacement = replacement;
  }

  public CorrelationRule build() {
    return new CorrelationRule(variableName, extractor != null ? extractor.build() : null,
        replacement != null ? replacement.build() : null);
  }

}
