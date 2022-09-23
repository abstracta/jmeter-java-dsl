package us.abstracta.jmeter.javadsl.codegeneration.params;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam.EnumPropertyValue;

/**
 * Is a parameter with an enum value.
 *
 * @param <T> is the type of the enum value.
 * @since 0.62
 */
public class EnumParam<T extends Enum<?> & EnumPropertyValue> extends FixedParam<T> {

  public EnumParam(Class<T> paramType, String expression, T defaultValue) {
    super(paramType, expression, e -> parse(paramType, e), defaultValue);
  }

  private static <T extends Enum<?> & EnumPropertyValue> T parse(Class<T> enumType,
      String propertyValue) {
    if (propertyValue.isEmpty()) {
      return null;
    }
    Optional<T> ret = Arrays.stream(enumType.getEnumConstants())
        .filter(v -> v.propertyValue().equals(propertyValue))
        .findAny();
    return ret.orElseThrow(() -> new IllegalArgumentException(
        "Unknown " + enumType.getSimpleName() + " property value: " + propertyValue));
  }

  @Override
  public Set<String> getImports() {
    return Collections.singleton(paramType.getName());
  }

  @Override
  public String buildCode(String indent) {
    return paramType.getSimpleName() + "." + value.name();
  }

  public interface EnumPropertyValue {

    String propertyValue();

  }

}
