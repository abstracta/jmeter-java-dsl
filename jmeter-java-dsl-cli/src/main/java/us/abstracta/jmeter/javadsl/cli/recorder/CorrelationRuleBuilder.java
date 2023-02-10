package us.abstracta.jmeter.javadsl.cli.recorder;

import com.blazemeter.jmeter.correlation.core.CorrelationRule;
import com.blazemeter.jmeter.correlation.core.extractors.RegexCorrelationExtractor;
import com.blazemeter.jmeter.correlation.core.extractors.ResultField;
import com.blazemeter.jmeter.correlation.core.replacements.RegexCorrelationReplacement;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.regex.Pattern;

public class CorrelationRuleBuilder {

  @JsonProperty("variable")
  private String variableName;
  @JsonProperty("extractor")
  private Pattern extractorRegex;
  @JsonProperty("replacement")
  private Pattern replacementRegex;

  // required for yaml deserialization
  public CorrelationRuleBuilder() {
  }

  public CorrelationRuleBuilder(String variableName, Pattern extractorRegex,
      Pattern replacementRegex) {
    this.variableName = variableName;
    this.extractorRegex = extractorRegex;
    this.replacementRegex = replacementRegex;
  }

  public CorrelationRule build() {
    return new CorrelationRule(variableName,
        extractorRegex != null
            ? new RegexCorrelationExtractor<>(extractorRegex.toString(), "1", "1",
            ResultField.BODY.name(), "true")
            : null,
        replacementRegex != null
            ? new RegexCorrelationReplacement<>(replacementRegex.toString())
            : null);
  }

}
