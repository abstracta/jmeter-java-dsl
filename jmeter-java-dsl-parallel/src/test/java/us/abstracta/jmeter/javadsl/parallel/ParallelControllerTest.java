package us.abstracta.jmeter.javadsl.parallel;

import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223PostProcessor;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.parallel.ParallelController.parallelController;

import java.util.concurrent.CyclicBarrier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class ParallelControllerTest extends JmeterDslTest {

  @Test
  @Timeout(30)
  public void shouldExecuteRequestsInParallelWhenRequestsInsideParallelController()
      throws Exception {
    CyclicBarrier barrier = new CyclicBarrier(2);
    testPlan(
        threadGroup(1, 1,
            parallelController(
                httpSampler(wiremockUri)
                    .children(jsr223PostProcessor(s -> barrier.await())),
                httpSampler(wiremockUri)
                    .children(jsr223PostProcessor(s -> barrier.await()))
            )
        )
    ).run();
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public CodeBuilderTest() {
      codeGenerator.addBuildersFrom(ParallelController.class);
    }

    public DslTestPlan testPlanWithParallelController() {
      return testPlan(
          threadGroup(1, 1,
              parallelController(
                  httpSampler("http://localhost"),
                  httpSampler("http://localhost")
              )
          )
      );
    }

    public DslTestPlan testPlanWithParallelWithNonDefaultSettings() {
      return testPlan(
          threadGroup(1, 1,
              parallelController()
                  .generateParentSample()
                  .maxThreads(5)
                  .children(
                      httpSampler("http://localhost"),
                      httpSampler("http://localhost")
                  )
          )
      );
    }

  }

}
