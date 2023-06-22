package us.abstracta.jmeter.javadsl.core.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class JmeterFunction {

  public static String from(String name, Object... args) {
    String argsString = args.length > 0
        ? "(" + Arrays.stream(args)
        /*
        Using US locale to avoid particular locales generating commas which break parsing of
        function
        */
        .map(a -> a instanceof Double || a instanceof Float
            ? String.format(Locale.US, "%f", a)
            : a.toString().replace("\\", "\\\\").replace(",", "\\,"))
        .collect(Collectors.joining(",")) + ")"
        : "";
    return "${" + name + argsString + "}";
  }

  public static String var(String varName) {
    return from(varName);
  }

  public static String groovy(String script) {
    return from("__groovy", script);
  }

}
