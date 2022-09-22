package us.abstracta.jmeter.javadsl.core.threadgroups;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223Sampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.setupThreadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.teardownThreadGroup;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.SampleErrorAction;

public class DslTeardownThreadGroupTest extends JmeterDslTest {

  @Test
  public void shouldRunSetupRequestsBeforeThreadGroupRequestsWhenSetupThreadGroupInTestPlan()
      throws Exception {
    String propName = "MY_PROP";
    String propVal = "MY_VAL";
    testPlan(
        teardownThreadGroup(
            httpSampler(wiremockUri + "/${__P(" + propName + ")}")
        ),
        threadGroup(1, 1,
            jsr223Sampler(s -> s.props.put(propName, propVal))
        )
    ).run();
    verify(getRequestedFor(urlEqualTo("/" + propVal)));
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan testPlanWithTeardownThreadGroup() {
      return testPlan(
          teardownThreadGroup(
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan testPlanWithTeardownThreadGroupAndNonDefaultSettings() {
      return testPlan(
          teardownThreadGroup()
              .threadCount(5)
              .iterations(2)
              .sampleErrorAction(SampleErrorAction.STOP_TEST)
              .children(
                  httpSampler("http://localhost")
              )
      );
    }

  }

}
