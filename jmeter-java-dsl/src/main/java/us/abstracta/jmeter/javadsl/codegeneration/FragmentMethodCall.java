package us.abstracta.jmeter.javadsl.codegeneration;

import java.util.Collections;
import java.util.Map;
import us.abstracta.jmeter.javadsl.core.controllers.DslController;

public class FragmentMethodCall extends MethodCall {

  private final MethodCall methodDefinitionBody;

  public FragmentMethodCall(String methodName, MethodCall methodDefinitionBody) {
    super(methodName,
        methodDefinitionBody != null ? methodDefinitionBody.getReturnType() : DslController.class);
    this.methodDefinitionBody = methodDefinitionBody;
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
