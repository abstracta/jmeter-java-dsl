package us.abstracta.jmeter.javadsl.codegeneration;

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
