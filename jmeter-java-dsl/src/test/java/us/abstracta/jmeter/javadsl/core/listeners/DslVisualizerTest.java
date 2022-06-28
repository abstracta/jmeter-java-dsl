package us.abstracta.jmeter.javadsl.core.listeners;

import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import org.apache.commons.io.FileUtils;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.FailureScreenshotTaker;
import org.assertj.swing.timing.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public abstract class DslVisualizerTest extends JmeterDslTest {

  private static final FailureScreenshotTaker SCREENSHOT_TAKER = new FailureScreenshotTaker(
      buildGuiScreenshotsFolder());
  protected Robot robot;
  protected ExecutorService executor;

  private static File buildGuiScreenshotsFolder() {
    File ret = Paths.get("target", "surefire-reports", "failed-gui-tests").toFile();
    if (ret.exists()) {
      try {
        FileUtils.forceDelete(ret);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    ret.mkdirs();
    return ret;
  }

  @BeforeEach
  public void setup() {
    executor = Executors.newSingleThreadExecutor();
    robot = BasicRobot.robotWithNewAwtHierarchy();
  }

  @AfterEach
  public void teardown() {
    if (robot != null) {
      robot.cleanUp();
    }
    if (executor != null) {
      executor.shutdownNow();
    }
  }

  public void testVisualizerTestPlan(DslTestPlan testPlan,
      ResultsReceivedCondition resultsCondition, VisualizerAssertion assertion, TestInfo testInfo) {
    executor.submit(testPlan::run);
    FrameFixture frame = WindowFinder.findFrame(JFrame.class)
        .withTimeout(10, TimeUnit.SECONDS)
        .using(robot);
    try {
      int timeoutSeconds = 30;
      awaitResultsReceived(resultsCondition, timeoutSeconds, frame);
      assertion.assertVisualizer(frame);
    } catch (Exception | AssertionError e) {
      saveScreenshot(testInfo);
      throw e;
    } finally {
      frame.cleanUp();
    }
  }

  private void awaitResultsReceived(ResultsReceivedCondition resultsCondition, int timeoutSeconds,
      FrameFixture frame) {
    pause(new Condition("sample results received") {
      @Override
      public boolean test() {
        return GuiActionRunner.execute(() -> resultsCondition.isTestPlanCompleted(frame));
      }
    }, timeout(timeoutSeconds, TimeUnit.SECONDS));
  }

  protected interface ResultsReceivedCondition {

    boolean isTestPlanCompleted(FrameFixture frame);

  }

  protected interface VisualizerAssertion {

    void assertVisualizer(FrameFixture frame);

  }

  protected void saveScreenshot(TestInfo testInfo) {
    String testName = testInfo.getTestClass()
        .map(c -> c.getSimpleName() + ".")
        .orElse("")
        + testInfo.getDisplayName();
    SCREENSHOT_TAKER.saveScreenshot(testName);
  }

}
