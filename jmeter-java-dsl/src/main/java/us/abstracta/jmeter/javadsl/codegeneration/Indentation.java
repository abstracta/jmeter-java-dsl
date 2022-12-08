package us.abstracta.jmeter.javadsl.codegeneration;

/**
 * Handles code indentation logic.
 * <p>
 * This class is currently not used instead of String in MethodCalls and MethodParams APIs to avoid
 * introducing non backwards compatible changes.
 *
 * @since 1.3
 */
public class Indentation {

  public static final String INDENT = "  ";

  public static String indentLevel(int level) {
    StringBuilder ret = new StringBuilder();
    for (int i = 0; i < level; i++) {
      ret.append(INDENT);
    }
    return ret.toString();
  }

  public static String indent(String str, String indentation) {
    return indentation + str.replace("\n", "\n" + indentation);
  }

}
