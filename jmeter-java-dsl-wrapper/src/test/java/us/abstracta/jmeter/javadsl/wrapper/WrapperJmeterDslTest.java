package us.abstracta.jmeter.javadsl.wrapper;

import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223Sampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.wrapper.WrapperJmeterDsl.testElement;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.assertions.gui.AbstractAssertionGui;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.gui.AbstractControllerGui;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.gui.AbstractThreadGroupGui;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
import us.abstracta.jmeter.javadsl.java.DslJsr223Sampler;
import us.abstracta.jmeter.javadsl.wrapper.wrappers.MultiLevelTestElementWrapper;

public class WrapperJmeterDslTest {

  private static final String PROP_NAME = "MY_PROP";
  private static final String PROP_VAL = "MY_VAL";
  private static final DslJsr223Sampler DUMMY_SAMPLER = jsr223Sampler("'OK'");
  private static final DslJsr223PostProcessor DUMMY_CHILD = jsr223PostProcessor("");
  private static final String WRAPPER_NAME = "name";

  @Test
  public void shouldGetDslThreadGroupWhenTestElementFromThreadGroupGuiComponent() throws Exception {
    testPlan(
        testElement(new MyThreadGroupGui())
            .prop(PROP_NAME, PROP_VAL)
            .children(DUMMY_SAMPLER)
    ).run();
  }

  private static class MyThreadGroupGui extends AbstractThreadGroupGui {

    @Override
    public String getLabelResource() {
      return "MyThreadGroup";
    }

    @Override
    public TestElement createTestElement() {
      return new MyThreadGroup();
    }

    @Override
    public void modifyTestElement(TestElement element) {

    }

  }

  private static class MyThreadGroup extends AbstractThreadGroup {

    @Override
    public boolean stopThread(String threadName, boolean now) {
      return true;
    }

    @Override
    public int numberOfActiveThreads() {
      return 0;
    }

    @Override
    public void start(int groupCount, ListenerNotifier notifier,
        ListedHashTree threadGroupTree, StandardJMeterEngine engine) {
    }

    @Override
    public JMeterThread addNewThread(int delay, StandardJMeterEngine engine) {
      return null;
    }

    @Override
    public boolean verifyThreadsStopped() {
      return true;
    }

    @Override
    public void waitThreadsStopped() {
    }

    @Override
    public void tellThreadsToStop() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void threadFinished(JMeterThread thread) {
    }

  }

  @Test
  public void shouldGetDslThreadGroupWhenTestElementFromTestBeanThreadGroup() throws Exception {
    testPlan(
        testElement(WRAPPER_NAME, new MyThreadGroupTestBean())
            .prop(PROP_NAME, PROP_VAL)
            .children(DUMMY_SAMPLER)
    ).run();
  }

  private static class MyThreadGroupTestBean extends MyThreadGroup implements TestBean {

  }

  @Test
  public void shouldGetDslThreadGroupWhenTestElementFromNonTestBeanThreadGroup() throws Exception {
    testPlan(
        testElement(new MyThreadGroup())
            .prop(PROP_NAME, PROP_VAL)
            .children(DUMMY_SAMPLER)
    ).run();
  }

  @Test
  public void shouldGetDslSamplerWhenTestElementFromSamplerGuiComponent() throws Exception {
    testPlan(
        threadGroup(1, 1,
            testElement(new MySamplerGui())
                .prop(PROP_NAME, PROP_VAL)
                .children(DUMMY_CHILD)
        )
    ).run();
  }

  private static class MySamplerGui extends AbstractSamplerGui {

    @Override
    public String getLabelResource() {
      return "MySampler";
    }

    @Override
    public TestElement createTestElement() {
      return new MySampler();
    }

    @Override
    public void modifyTestElement(TestElement element) {

    }

  }

  // requires public visibility due to test element clone method
  public static class MySampler extends AbstractSampler {

    @Override
    public SampleResult sample(Entry e) {
      return null;
    }

  }

  @Test
  public void shouldGetDslSamplerWhenTestElementFromTestBeanSampler() throws Exception {
    testPlan(
        threadGroup(1, 1,
            testElement(WRAPPER_NAME, new MySamplerTestBean())
                .prop(PROP_NAME, PROP_VAL)
                .children(DUMMY_CHILD)
        )
    ).run();
  }

  // requires public visibility due to test element clone method
  public static class MySamplerTestBean extends MySampler implements TestBean {

  }

  @Test
  public void shouldGetDslSamplerWhenTestElementFromNonTestBeanSampler() throws Exception {
    testPlan(
        threadGroup(1, 1,
            testElement(new MySampler())
                .prop(PROP_NAME, PROP_VAL)
                .children(DUMMY_CHILD)
        )
    ).run();
  }

  @Test
  public void shouldGetDslControllerWhenTestElementFromControllerGuiComponent() throws Exception {
    testPlan(
        threadGroup(1, 1,
            testElement(new MyControllerGui())
                .prop(PROP_NAME, PROP_VAL)
                .children(DUMMY_SAMPLER)
        )
    ).run();
  }

  private static class MyControllerGui extends AbstractControllerGui {

    @Override
    public String getLabelResource() {
      return "MyController";
    }

    @Override
    public TestElement createTestElement() {
      return new MyController();
    }

    @Override
    public void modifyTestElement(TestElement element) {

    }

  }

  // requires public visibility due to test element clone method
  public static class MyController extends GenericController {

  }

  @Test
  public void shouldGetDslControllerWhenTestElementFromTestBeanController() throws Exception {
    testPlan(
        threadGroup(1, 1,
            testElement(WRAPPER_NAME, new MyControllerTestBean())
                .prop(PROP_NAME, PROP_VAL)
                .children(DUMMY_SAMPLER)
        )
    ).run();
  }

  // requires public visibility due to test element clone method
  public static class MyControllerTestBean extends MyController implements TestBean {

  }

  @Test
  public void shouldGetDslControllerWhenTestElementFromNonTestBeanController() throws Exception {
    testPlan(
        threadGroup(1, 1,
            testElement(new MyController())
                .prop(PROP_NAME, PROP_VAL)
                .children(DUMMY_CHILD)
        )
    ).run();
  }

  @Test
  public void shouldGetDslMultiLevelElementWhenTestElementFromMultiLevelElementGuiComponent()
      throws Exception {
    MultiLevelTestElementWrapper assertion = testElement(new MyAssertionGui())
        .prop(PROP_NAME, PROP_VAL);
    testPlan(
        assertion,
        threadGroup(1, 1,
            assertion,
            DUMMY_SAMPLER.children(assertion)
        )
    ).run();
  }

  private static class MyAssertionGui extends AbstractAssertionGui {

    @Override
    public String getLabelResource() {
      return "MyController";
    }

    @Override
    public TestElement createTestElement() {
      return new MyAssertion();
    }

    @Override
    public void modifyTestElement(TestElement element) {

    }

  }

  // requires public visibility due to test element clone method
  public static class MyAssertion extends AbstractTestElement implements Assertion {

    @Override
    public AssertionResult getResult(SampleResult response) {
      return new AssertionResult("MyAssertion");
    }

  }

  @Test
  public void shouldGetDslMultiLevelElementWhenTestElementFromTestBeanMultiLevelElement()
      throws Exception {
    MultiLevelTestElementWrapper assertion = testElement(WRAPPER_NAME, new MyAssertionTestBean())
        .prop(PROP_NAME, PROP_VAL);
    testPlan(
        assertion,
        threadGroup(1, 1,
            assertion,
            DUMMY_SAMPLER.children(assertion)
        )
    ).run();
  }

  // requires public visibility due to test element clone method
  public static class MyAssertionTestBean extends MyAssertion implements TestBean {

  }

  @Test
  public void shouldGetDslMultiLevelElementWhenTestElementFromNonTestBeanMultiLevelElement()
      throws Exception {
    MultiLevelTestElementWrapper assertion = testElement(new MyAssertion())
        .prop(PROP_NAME, PROP_VAL);
    testPlan(
        assertion,
        threadGroup(1, 1,
            assertion,
            DUMMY_SAMPLER.children(assertion)
        )
    ).run();
  }

}
