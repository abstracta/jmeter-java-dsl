package us.abstracta.jmeter.javadsl.core.timers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.SyncTimer;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;

/**
 * Uses JMeter Synchronizing Timer to allow sending a batch of requests simultaneously to a system
 * under test.
 *
 * @since 1.17
 */
public class DslSynchronizingTimer extends BaseTimer {

  public DslSynchronizingTimer() {
    super("Synchronizing Timer", TestBeanGUI.class);
  }

  @Override
  protected TestElement buildTestElement() {
    return new SyncTimer();
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<SyncTimer> {

    public CodeBuilder(List<Method> builderMethods) {
      super(SyncTimer.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(SyncTimer testElement, MethodCallContext context) {
      return buildMethodCall();
    }

  }

}
