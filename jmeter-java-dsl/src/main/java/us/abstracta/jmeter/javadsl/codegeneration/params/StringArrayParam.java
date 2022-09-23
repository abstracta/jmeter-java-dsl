package us.abstracta.jmeter.javadsl.codegeneration.params;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringArrayParam extends FixedParam<String[]> {

  public StringArrayParam(String expression) {
    super(String[].class, expression, e -> e.split(","), null);
  }

  @Override
  public String buildCode(String indent) {
    return Arrays.stream(value)
        .map(s -> buildStringLiteral(s, indent))
        .collect(Collectors.joining(", "));
  }

}
