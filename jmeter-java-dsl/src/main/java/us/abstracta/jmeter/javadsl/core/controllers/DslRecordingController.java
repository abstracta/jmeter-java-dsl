package us.abstracta.jmeter.javadsl.core.controllers;

import java.util.Collections;
import java.util.List;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.protocol.http.control.gui.RecordController;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.ChildrenParam;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * This element is only provided to ignore recording controllers when generating DSL code from JMX.
 *
 * @since 0.50
 */
public class DslRecordingController extends BaseController<DslRecordingController> {

  public DslRecordingController(List<ThreadGroupChild> children) {
    super("Recording Controller", RecordController.class, children);
  }

  @Override
  protected TestElement buildTestElement() {
    throw new UnsupportedOperationException();
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<RecordingController> {

    public CodeBuilder() {
      super(RecordingController.class, Collections.emptyList());
    }

    @Override
    protected MethodCall buildMethodCall(RecordingController testElement,
        MethodCallContext context) {
      return new OnlyChildrenMethodCall();
    }

  }

  public static class OnlyChildrenMethodCall extends MethodCall {

    protected OnlyChildrenMethodCall() {
      super(null, DslController.class, new ChildrenParam<>(ThreadGroupChild[].class));
    }

    @Override
    public String buildCode(String indent) {
      return buildParamsCode(indent);
    }

  }

}
