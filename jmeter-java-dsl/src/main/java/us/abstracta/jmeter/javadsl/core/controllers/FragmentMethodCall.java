package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;

/**
 * Defines a call to a test plan fragment method definition will be used.
 * <p>
 * Fragments method definitions might be the explicitly defined from a test fragment controller or
 * implicitly from module controller target controller.
 *
 * @since 1.3
 */
public class FragmentMethodCall extends MethodCall {

  private final MethodCall methodDefinitionBody;

  public FragmentMethodCall(TestElement element, MethodCall methodDefinitionBody,
      MethodCallContext context) {
    this(solveMethodName(element, context), methodDefinitionBody);
  }

  private FragmentMethodCall(String methodName, MethodCall methodDefinitionBody) {
    super(methodName,
        methodDefinitionBody != null ? methodDefinitionBody.getReturnType() : DslController.class);
    this.methodDefinitionBody = methodDefinitionBody;
  }

  private static String solveMethodName(TestElement element, MethodCallContext context) {
    Map<TestElement, String> definedMethods = getDefinedMethods(context);
    String ret = definedMethods.computeIfAbsent(element,
        e -> buildUniqueName(e.getName(), new HashSet<>(definedMethods.values())));
    if (element != context.getTestElement()) {
      context.replaceMethodCall(element, c -> {
        if (c instanceof FragmentMethodCall) {
          return c;
        }
        FragmentMethodCall fragment = new FragmentMethodCall(ret, c);
        fragment.setCommented(c.isCommented());
        c.setCommented(false);
        return fragment;
      });
    }
    return ret;
  }

  private static Map<TestElement, String> getDefinedMethods(MethodCallContext context) {
    return context.getRoot().computeEntryIfAbsent(FragmentMethodCall.class, HashMap::new);
  }

  private static String buildUniqueName(String elementName, Set<String> existingNames) {
    // removing any character that may not be allowed in method name
    String ret = elementName.replaceAll("\\W", "");
    // avoid method names starting with digits which are not supported by java
    ret = (Character.isDigit(ret.charAt(0)) ? "fragment" : "") + ret;
    // lower first char to follow java method naming convention
    ret = Character.toLowerCase(ret.charAt(0)) + ret.substring(1);
    if (!existingNames.contains(ret)) {
      return ret;
    }
    int index = 2;
    while (existingNames.contains(ret + index)) {
      index++;
    }
    return ret + index;
  }

  @Override
  public MethodCall child(MethodCall child) {
    return methodDefinitionBody.child(child);
  }

  @Override
  public Map<String, MethodCall> getMethodDefinitions() {
    return methodDefinitionBody != null ? Collections.singletonMap(methodName, methodDefinitionBody)
        : Collections.emptyMap();
  }

}
