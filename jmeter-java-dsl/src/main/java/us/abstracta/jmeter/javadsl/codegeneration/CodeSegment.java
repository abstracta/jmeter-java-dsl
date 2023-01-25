package us.abstracta.jmeter.javadsl.codegeneration;

import java.util.Collection;
import java.util.Map;

/**
 * Specifies part of code to be generated including common logic required byt different type of code
 * segments.
 *
 * @since 1.5
 */
public interface CodeSegment {

  String buildCode(String indent);

  Collection<String> getStaticImports();

  Collection<String> getImports();

  Map<String, ? extends MethodCall> getMethodDefinitions();

}
