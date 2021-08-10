package us.abstracta.jmeter.javadsl.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import kg.apc.jmeter.JMeterPluginsUtils;
import kg.apc.jmeter.threads.UltimateThreadGroup;
import kg.apc.jmeter.threads.UltimateThreadGroupGui;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.threads.ThreadGroup;
import org.assertj.core.api.AbstractAssert;
import org.junit.Test;

public class DslThreadGroupTest {

  private static final int DURATION1_SECONDS = 10;
  private static final int DURATION2_SECONDS = 15;
  private static final int DURATION3_SECONDS = 20;
  private static final int ITERATIONS = 10;
  private static final int THREAD_COUNT = 3;

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithThreadsAndIterations() {
    assertThat(new DslThreadGroup(null, THREAD_COUNT, ITERATIONS,
        Collections.emptyList()).buildTestElement())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, ITERATIONS, 0, 0, 0));
  }

  public TestElement buildSimpleThreadGroup(int threads, int iterations, int durationSecs,
      int rampUpSecs, int delaySecs) {
    ThreadGroup ret = new ThreadGroup();
    ret.setNumThreads(threads);
    ret.setRampUp(rampUpSecs);
    LoopController loopController = new LoopController();
    ret.setSamplerController(loopController);
    if (durationSecs != 0) {
      loopController.setLoops(-1);
      ret.setDuration(durationSecs);
    } else {
      loopController.setLoops(iterations);
    }
    ret.setDelay(delaySecs);
    ret.setScheduler(durationSecs != 0 || delaySecs != 0);
    return ret;
  }

  public static TestElementAssert assertThat(TestElement actual) {
    return new TestElementAssert(actual);
  }

  public static class TestElementAssert extends
      AbstractAssert<TestElementAssert, Map<String, Object>> {

    public TestElementAssert(TestElement actual) {
      super(propsFrom(actual), TestElementAssert.class);
    }

    @Override
    public TestElementAssert isEqualTo(Object expected) {
      return super.isEqualTo(
          expected instanceof TestElement ? propsFrom((TestElement) expected) : expected);
    }

    private static Map<String, Object> propsFrom(TestElement elem) {
      Iterable<JMeterProperty> iterable = elem::propertyIterator;
      return StreamSupport.stream(iterable.spliterator(), false)
          .filter(p -> !("".equals(p.getName())))
          .collect(Collectors.toMap(JMeterProperty::getName, p -> {
            Object v = p.getObjectValue();
            return v instanceof TestElement ? propsFrom((TestElement) v) : v;
          }));
    }

  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithHoldRampAndIterations() {
    assertThat(new DslThreadGroup(null)
        .holdFor(Duration.ofSeconds(DURATION1_SECONDS))
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION2_SECONDS))
        .holdIterating(ITERATIONS)
        .buildTestElement())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, ITERATIONS, 0, DURATION2_SECONDS,
            DURATION1_SECONDS));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithRampAndIterations() {
    assertThat(new DslThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS))
        .holdIterating(ITERATIONS)
        .buildTestElement())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, ITERATIONS, 0, DURATION1_SECONDS, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithHoldAndRampWithZeroDurationAndIterations() {
    assertThat(new DslThreadGroup(null)
        .holdFor(Duration.ofSeconds(DURATION1_SECONDS))
        .rampTo(THREAD_COUNT, Duration.ZERO)
        .holdIterating(ITERATIONS)
        .buildTestElement())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, ITERATIONS, 0, 0, DURATION1_SECONDS));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithRampWithZeroDurationAndIterations() {
    assertThat(new DslThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ZERO)
        .holdIterating(ITERATIONS)
        .buildTestElement())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, ITERATIONS, 0, 0, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithHoldAndRampAndZeroIterations() {
    assertThat(new DslThreadGroup(null)
        .holdFor(Duration.ofSeconds(DURATION1_SECONDS))
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION2_SECONDS))
        .holdIterating(0)
        .buildTestElement())
        .isEqualTo(
            buildSimpleThreadGroup(THREAD_COUNT, 0, 0, DURATION2_SECONDS, DURATION1_SECONDS));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithRampAndZeroIterations() {
    assertThat(new DslThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS))
        .holdIterating(0)
        .buildTestElement())
        .isEqualTo(
            buildSimpleThreadGroup(THREAD_COUNT, 0, 0, DURATION1_SECONDS, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenBuildTestElementWithThreadsAndDuration() {
    assertThat(new DslThreadGroup(null, THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS),
        Collections.emptyList()).buildTestElement())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, 0, DURATION1_SECONDS, 0, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenHoldAndRampAndHoldDuration() {
    assertThat(new DslThreadGroup(null)
        .holdFor(Duration.ofSeconds(DURATION1_SECONDS))
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION2_SECONDS))
        .holdFor(Duration.ofSeconds(DURATION3_SECONDS))
        .buildTestElement())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, 0, DURATION3_SECONDS, DURATION2_SECONDS,
            DURATION1_SECONDS));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenRampAndHoldDuration() {
    assertThat(new DslThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS))
        .holdFor(Duration.ofSeconds(DURATION2_SECONDS))
        .buildTestElement())
        .isEqualTo(
            buildSimpleThreadGroup(THREAD_COUNT, 0, DURATION2_SECONDS, DURATION1_SECONDS, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenRamp() {
    assertThat(new DslThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS))
        .buildTestElement())
        .isEqualTo(buildSimpleThreadGroup(THREAD_COUNT, 0, 0, DURATION1_SECONDS, 0));
  }

  @Test
  public void shouldBuildSimpleThreadGroupWhenNoStages() {
    assertThat(new DslThreadGroup(null)
        .buildTestElement())
        .isEqualTo(buildSimpleThreadGroup(1, 1, 0, 0, 0));
  }

  @Test
  public void shouldThrowIllegalStateExceptionWhenIterationAfterIteration() {
    assertThrows(IllegalStateException.class, () -> new DslThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ZERO)
        .holdIterating(ITERATIONS)
        .holdIterating(ITERATIONS));
  }

  @Test
  public void shouldThrowIllegalStateExceptionWhenIterationInNonSimpleThreadGroup() {
    assertThrows(IllegalStateException.class, () -> new DslThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ZERO)
        .rampTo(0, Duration.ZERO)
        .holdIterating(ITERATIONS));
  }

  @Test
  public void shouldThrowIllegalStateExceptionWhenRampAfterIteration() {
    assertThrows(IllegalStateException.class, () -> new DslThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ZERO)
        .holdIterating(ITERATIONS)
        .rampTo(0, Duration.ZERO));
  }

  @Test
  public void shouldThrowIllegalArgumentExceptionWhenRampWithNegativeThreads() {
    assertThrows(IllegalArgumentException.class, () -> new DslThreadGroup(null)
        .rampTo(-1, Duration.ZERO));
  }

  @Test
  public void shouldBuildUltimateThreadGroupWhenRampUpAndDown() {
    assertThat(new DslThreadGroup(null)
        .rampTo(THREAD_COUNT, Duration.ofSeconds(DURATION1_SECONDS))
        .rampTo(0, Duration.ofSeconds(DURATION2_SECONDS))
        .buildTestElement())
        .isEqualTo(buildUltimateThreadGroup(
            new int[][]{{THREAD_COUNT, 0, DURATION1_SECONDS, 0, DURATION2_SECONDS}}));
  }

  private TestElement buildUltimateThreadGroup(int[][] schedsProps) {
    UltimateThreadGroup ret = new UltimateThreadGroup();
    PowerTableModel table = new PowerTableModel(UltimateThreadGroupGui.columnIdentifiers,
        UltimateThreadGroupGui.columnClasses);
    for (int[] schedProps : schedsProps) {
      table.addRow(new Object[]{"" + schedProps[0], "" + schedProps[1], "" + schedProps[2],
          "" + schedProps[3], "" + schedProps[4]});
    }
    ret.setData(JMeterPluginsUtils.tableModelRowsToCollectionProperty(table,
        UltimateThreadGroup.DATA_PROPERTY));
    return ret;
  }

  @Test
  public void shouldBuildUltimateThreadGroupWhenComplexProfile() {
    Duration d = Duration.ofSeconds(10);
    DslThreadGroup threadGroup = new DslThreadGroup(null)
        .holdFor(d)
        .rampToAndHold(3, d, d)
        .rampToAndHold(2, d, d)
        .rampTo(5, d)
        .rampTo(3, d)
        .rampToAndHold(7, d, d)
        .rampToAndHold(5, d, d)
        .rampTo(1, d);
    assertThat(threadGroup.buildTestElement())
        .isEqualTo(buildUltimateThreadGroup(new int[][]{
            {1, 10, 4, 127, 0},
            {1, 14, 4, 121, 3},
            {1, 17, 4, 10, 10},
            {1, 50, 4, 82, 3},
            {2, 54, 7, 0, 10},
            {2, 70, 7, 54, 5},
            {1, 77, 4, 35, 5},
            {1, 90, 10, 10, 5}
        }));
  }

}
