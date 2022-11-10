package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.Collections;
import org.apache.jmeter.protocol.http.proxy.ProxyControl;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;

// This class is just added to ignore ProxyControl when converting jmx to code
public class DslProxyControl {

  public static class CodeBuilder extends SingleTestElementCallBuilder<ProxyControl> {

    public CodeBuilder() {
      super(ProxyControl.class, Collections.emptyList());
    }

    @Override
    protected MethodCall buildMethodCall(ProxyControl testElement, MethodCallContext context) {
      return MethodCall.emptyCall();
    }

  }

}
