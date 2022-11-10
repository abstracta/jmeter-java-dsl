package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;

public class FragmentMethodCall extends MethodCall {

  private final MethodCall methodDefinitionBody;

  protected FragmentMethodCall(MethodCall methodDefinitionBody, TestElement element, MethodCallContext context) {
    super(solveMethodName(element, context), DslTestFragmentController.class);
    this.methodDefinitionBody = methodDefinitionBody;
  }

  private static String solveMethodName(TestElement element, MethodCallContext context) {
    Map<TestElement, String> definedMethods = getDefinedMethods(context.getRoot());
    String ret = definedMethods.get(element);
    if (ret != null) {
      return ret;
    }
    // removing any character that may not be allowed in method name
    ret = element.getName().replaceAll("\\W", "");
    // avoid method names starting with digits which are not supported by java
    ret = (Character.isDigit(ret.charAt(0)) ? "fragment" : "") + ret;
    // lower first char to follow java method naming convention
    ret = Character.toLowerCase(ret.charAt(0)) + ret.substring(1);
    HashSet<String> methodNames = new HashSet<>(definedMethods.values());
    if (methodNames.contains(ret)) {
      int index = 1;
      do {
        index++;
      } while (methodNames.contains(ret + index));
      ret = ret + index;
    }
    definedMethods.put(element, ret);
    return ret;
  }

  private static Map<TestElement, String> getDefinedMethods(MethodCallContext context) {
    Object entryKey = DslTestFragmentController.class;
    Map<TestElement, String> definedMethods = (Map<TestElement, String>) context.getEntry(entryKey);
    if (definedMethods == null) {
      definedMethods = new HashMap<>();
      context.setEntry(entryKey, definedMethods);
    }
    return definedMethods;
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

  @Override
  public String buildCode(String indent) {
    return methodName + "()";
  }

}