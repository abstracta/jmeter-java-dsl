package us.abstracta.jmeter.javadsl.core;

import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.ifController;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Condition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.listeners.DslVisualizerTest;

public class DslTestPlanTest extends DslVisualizerTest {

  @Test
  public void shouldShowJmeterGuiWhenShowGuiInSimpleTestPlan(TestInfo testInfo) {
    DslTestPlan testPlan = testPlan(
        threadGroup(1, 1,
            /*
             ifController included since issues have been detected with it when test plan showGui
             is not properly implemented. Check DslTestPlan.showGui implementation for more details.
             */
            ifController("true",
                httpSampler("http://myservice.com")
            )
        )
    );
    executor.submit(testPlan::showInGui);
    FrameFixture frame = WindowFinder.findFrame(JFrame.class)
        .withTimeout(10, TimeUnit.SECONDS)
        .using(robot);
    try {
      frame.requireVisible();
      pause(new Condition("test plan loaded") {
        @Override
        public boolean test() {
          return GuiActionRunner.execute(() -> frame.tree().target().getRowCount() > 1);
        }
      }, timeout(10, TimeUnit.SECONDS));
    } catch (Exception | AssertionError e) {
      saveScreenshot(testInfo);
      throw e;
    } finally {
      frame.cleanUp();
    }
  }

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
      return testPlan()
          .sequentialThreadGroups()
          .tearDownOnlyAfterMainThreadsDone()
          .children(
              threadGroup(1, 1,
                  httpSampler("http://localhost")
              ),
              threadGroup(1, 1,
                  httpSampler("http://localhost")
              )
          );
    }

  }

}
