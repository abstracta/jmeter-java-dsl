package us.abstracta.jmeter.javadsl.wrapper;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223Sampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.wrapper.WrapperJmeterDsl.testElement;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsr223PostProcessor;
import us.abstracta.jmeter.javadsl.java.DslJsr223Sampler;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MyAssertion;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MyAssertionGui;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MyAssertionTestBean;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MyController;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MyControllerGui;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MyControllerTestBean;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MySampler;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MySamplerGui;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MySamplerTestBean;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MyThreadGroup;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MyThreadGroupGui;
import us.abstracta.jmeter.javadsl.wrapper.customelements.MyThreadGroupTestBean;
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

  @Test
  public void shouldGetDslThreadGroupWhenTestElementFromTestBeanThreadGroup() throws Exception {
    testPlan(
        testElement(WRAPPER_NAME, new MyThreadGroupTestBean())
            .prop(PROP_NAME, PROP_VAL)
            .children(DUMMY_SAMPLER)
    ).run();
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

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    protected CodeBuilderTest() {
      codeGenerator.addBuildersFrom(WrapperJmeterDsl.class);
    }

    public DslTestPlan threadGroupGui() {
      return testPlan(
          testElement(new MyThreadGroupGui())
              .prop("MY_PROP", "MY_VAL")
              .children(
                  httpSampler("http://myservice")
              )
      );
    }

    public DslTestPlan threadGroupTestBean() {
      return testPlan(
          testElement(new MyThreadGroupTestBean())
              .prop("MY_PROP", "MY_VAL")
              .children(
                  httpSampler("http://myservice")
              )
      );
    }

    public DslTestPlan sampler() {
      return testPlan(
          threadGroup(1, 1,
              testElement(new MySamplerGui())
                  .prop("MY_PROP", "MY_VAL")
          )
      );
    }

    public DslTestPlan controller() {
      return testPlan(
          threadGroup(1, 1,
              testElement(new MyControllerGui())
                  .prop("MY_PROP", "MY_VAL")
                  .children(
                      httpSampler("http://myservice")
                  )
          )
      );
    }

    public DslTestPlan assertion() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://myservice")
                  .children(
                      testElement(new MyAssertionGui())
                          .prop("MY_PROP", "MY_VAL")
                  )
          )
      );
    }

  }

}
