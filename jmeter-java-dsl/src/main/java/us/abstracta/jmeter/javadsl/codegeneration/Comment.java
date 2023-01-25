package us.abstracta.jmeter.javadsl.codegeneration;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Allows specifying a comment to be added to generated code.
 * <p>
 * This is particularly helpful when adding tips, warnings or any extra information to generated
 * code that needs attention/review.
 *
 * @since 1.5
 */
public class Comment implements CodeSegment {

  private final String body;

  public Comment(String body) {
    this.body = body;
  }

  @Override
  public String buildCode(String indent) {
    return "// " + body;
  }

  @Override
  public Collection<String> getStaticImports() {
    return Collections.emptyList();
  }

  @Override
  public Collection<String> getImports() {
    return Collections.emptyList();
  }

  @Override
  public Map<String, ? extends MethodCall> getMethodDefinitions() {
    return Collections.emptyMap();
  }

}
