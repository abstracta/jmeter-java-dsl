package us.abstracta.jmeter.javadsl.cli;

import java.util.regex.Pattern;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;
import us.abstracta.jmeter.javadsl.cli.recorder.CorrelationRuleBuilder;

public class CorrelationRuleBuilderConverter implements ITypeConverter<CorrelationRuleBuilder> {

  @Override
  public CorrelationRuleBuilder convert(String value) {
    String[] parts = value.split(",");
    if (parts.length < 2 || parts.length > 3) {
      throw new TypeConversionException(
          "Invalid format: must be 'variable,extractor,replacement' but was '" + value + "'");
    }
    return new CorrelationRuleBuilder(parts[0], compileRegex(parts[1]),
        parts.length > 2 ? compileRegex(parts[2]) : null);
  }

  private Pattern compileRegex(String param) {
    // remove quotes escaping required to avoid picocli interpretation
    return Pattern.compile(param.replace("\\\"", "\""));
  }

}
