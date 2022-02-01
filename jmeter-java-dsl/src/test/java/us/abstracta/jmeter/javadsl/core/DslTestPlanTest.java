package us.abstracta.jmeter.javadsl.core;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import org.junit.jupiter.api.Nested;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;

public class DslTestPlanTest {

  @SuppressWarnings("unused")
  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public DslTestPlan simpleTestPlan() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          )
      );
    }

    public DslTestPlan completeTestPlan() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost")
          ),
          threadGroup(1, 1,
              httpSampler("http://localhost")
          )
      ).sequentialThreadGroups()
          .tearDownOnlyAfterMainThreadsDone();
    }

  }

}
