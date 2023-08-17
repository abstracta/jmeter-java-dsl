package us.abstracta.jmeter.javadsl.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.autoStop;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.function.Consumer;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.engines.BaseTestStopper;
import us.abstracta.jmeter.javadsl.core.engines.JmeterEnvironment;
import us.abstracta.jmeter.javadsl.core.listeners.AutoStopListener.AutoStopCondition;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.AutoStopTestBean;

public class AutoStopTestBeanTest {

  private static final Duration ONE_SEC = Duration.ofSeconds(1);
  private static final double ERROR_PERCENT_LIMIT = 50.0;
  private final MockedClock clock = new MockedClock();
  private MockedTestStopper testStopper;

  private static class MockedClock extends Clock {

    private Instant instant = Instant.now();

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return instant;
    }

    public void tick() {
      tick(ONE_SEC);
    }

    public void tick(Duration duration) {
      instant = instant.plus(duration);
    }

  }

  private static class MockedTestStopper extends BaseTestStopper {

    @Override
    protected void stopTestExecution() {
    }

  }

  @BeforeAll
  public static void setupAll() throws IOException {
    new JmeterEnvironment();
  }

  @BeforeEach
  public void setup() {
    testStopper = new MockedTestStopper();
  }

  @Test
  public void shouldAutoStopWhenErrorPercentOverOpenLimit() {
    AutoStopTestBean element = buildAutoStop(buildErrorsPercentCondition());
    errorSample(element);
    clock.tick();
    errorSample(element);
    clock.tick();
    sample(element);
    assertAutoStop();
  }

  private AutoStopTestBean buildAutoStop(AutoStopCondition condition) {
    return buildElement(autoStop().when(condition));
  }

  private AutoStopTestBean buildElement(AutoStopListener listener) {
    AutoStopTestBean element = (AutoStopTestBean) listener.buildTestElement();
    element.setTestStopper(testStopper);
    element.setName("AutoStop");
    element.setClock(clock);
    element.testStarted();
    return element;
  }

  private static AutoStopCondition buildErrorsPercentCondition() {
    return AutoStopCondition.errors().percent().every(ONE_SEC).greaterThan(ERROR_PERCENT_LIMIT)
        .holdsFor(ONE_SEC);
  }

  private void errorSample(AutoStopTestBean element) {
    sample(r -> r.setSuccessful(false), element);
  }

  public void sample(Consumer<SampleResult> consumer, AutoStopTestBean listener) {
    SampleResult sample = new SampleResult();
    sample.setSampleLabel("test");
    sample.setSuccessful(true);
    consumer.accept(sample);
    listener.sampleOccurred(new SampleEvent(sample, "test-thread"));
  }

  private void assertAutoStop() {
    assertThat(testStopper.getStopMessage()).isNotNull();
  }

  @Test
  public void shouldNotAutoStopWhenErrorPercentEqualOpenLimit() {
    AutoStopTestBean element = buildAutoStop(buildErrorsPercentCondition());
    errorSample(element);
    sample(element);
    clock.tick();
    errorSample(element);
    sample(element);
    clock.tick();
    sample(element);
    assertNotAutoStop();
  }

  private void sample(AutoStopTestBean element) {
    sample(r -> {
    }, element);
  }

  private void assertNotAutoStop() {
    assertThat(testStopper.getStopMessage()).isNull();
  }

  @Test
  public void shouldAutoStopWhenErrorPercentEqualClosedLimit() {
    AutoStopTestBean element = buildAutoStop(AutoStopCondition.errors().percent()
        .greaterThanOrEqualTo(ERROR_PERCENT_LIMIT).holdsFor(ONE_SEC));
    errorSample(element);
    sample(element);
    clock.tick();
    errorSample(element);
    sample(element);
    clock.tick();
    sample(element);
    assertAutoStop();
  }

  @Test
  public void shouldNotAutoStopWhenConditionDoesNotHoldOnSlotsBetweenSamples() {
    AutoStopTestBean element = buildAutoStop(AutoStopCondition.errors().percent()
        .every(ONE_SEC).greaterThanOrEqualTo(ERROR_PERCENT_LIMIT).holdsFor(ONE_SEC));
    errorSample(element);
    clock.tick(Duration.ofSeconds(2));
    errorSample(element);
    clock.tick(Duration.ofSeconds(2));
    sample(element);
    assertNotAutoStop();
  }

  @Test
  public void shouldNotAutoStopWhenConditionNotHolds() {
    AutoStopTestBean element = buildAutoStop(buildErrorsPercentCondition());
    errorSample(element);
    clock.tick();
    sample(element);
    clock.tick();
    sample(element);
    assertNotAutoStop();
  }

  @Test
  public void shouldAutoStopWhenConditionMatchesAtAnyPointWithNoResetZeroHolding() {
    AutoStopTestBean element = buildAutoStop(
        AutoStopCondition.errors().percent().greaterThan(ERROR_PERCENT_LIMIT));
    errorSample(element);
    assertAutoStop();
  }

  @Test
  public void shouldNotAutoStopWhenConditionNotMatchesWithNoResetZeroHolding() {
    AutoStopTestBean element = buildAutoStop(AutoStopCondition.errors().percent()
        .greaterThanOrEqualTo(ERROR_PERCENT_LIMIT));
    sample(element);
    assertNotAutoStop();
  }

  @Test
  public void shouldNotAutoStopWhenConditionMatchesAtAnyPointWithButNotHolds() {
    AutoStopTestBean element = buildAutoStop(AutoStopCondition.errors().percent()
        .greaterThanOrEqualTo(ERROR_PERCENT_LIMIT).holdsFor(ONE_SEC));
    errorSample(element);
    sample(element);
    assertNotAutoStop();
  }

  @Test
  public void shouldAutoStopWhenErrorsPerSecondIsOverLimit() {
    AutoStopTestBean element = buildAutoStop(
        AutoStopCondition.errors().perSecond().greaterThanOrEqualTo(2.0));
    errorSample(element);
    errorSample(element);
    clock.tick();
    sample(element);
    assertAutoStop();
  }

  @Test
  public void shouldNotAutoStopWhenErrorsPerSecondIsUnderLimitInEachSecond() {
    AutoStopTestBean element = buildAutoStop(
        AutoStopCondition.errors().perSecond().greaterThanOrEqualTo(2.0));
    errorSample(element);
    clock.tick();
    errorSample(element);
    clock.tick();
    sample(element);
    assertNotAutoStop();
  }

  @Test
  public void shouldAutoStopWhenTotalErrorsIsOverLimit() {
    AutoStopTestBean element = buildAutoStop(
        AutoStopCondition.errors().total().greaterThanOrEqualTo(2L));
    errorSample(element);
    clock.tick();
    errorSample(element);
    assertAutoStop();
  }

  @Test
  public void shouldAutoStopWhenConditionOverLimitAndSamplesMatchConditionRegex() {
    AutoStopTestBean element = buildAutoStop(AutoStopCondition.samplesMatching("t.*t")
        .errors().total().greaterThan(0L));
    errorSample(element);
    assertAutoStop();
  }

  @Test
  public void shouldNotAutoStopWhenConditionOverLimitButSamplesNotMatchConditionRegex() {
    AutoStopTestBean element = buildAutoStop(
        AutoStopCondition.samplesMatching("other").errors().total().greaterThan(0L));
    errorSample(element);
    assertNotAutoStop();
  }

  @Test
  public void shouldAutoStopWhenSamplesMatchRegexAndConditionOverLimit() {
    AutoStopTestBean element = buildElement(autoStop().samplesMatching("t.*t")
        .when(AutoStopCondition.errors().total().greaterThan(0L)));
    errorSample(element);
    assertAutoStop();
  }

  @Test
  public void shouldNotAutoStopWhenSamplesNotMatchRegexAndConditionOverLimit() {
    AutoStopTestBean element = buildElement(autoStop().samplesMatching("other")
        .when(AutoStopCondition.errors().total().greaterThan(0L)));
    errorSample(element);
    assertNotAutoStop();
  }

  @Test
  public void shouldAutoStopWhenConditionOverLimitAndMatchConditionRegexAndNotListenerRegex() {
    AutoStopTestBean element = buildElement(autoStop().samplesMatching("other")
        .when(AutoStopCondition.samplesMatching("t.*t").errors().total().greaterThan(0L)));
    errorSample(element);
    assertAutoStop();
  }

  @Test
  public void shouldNotAutoStopWhenConditionOverLimitAndNotMatchConditionRegexButMatchListenerRegex() {
    AutoStopTestBean element = buildElement(autoStop().samplesMatching("t.*t")
        .when(AutoStopCondition.samplesMatching("other").errors().total().greaterThan(0L)));
    errorSample(element);
    assertNotAutoStop();
  }

  @Test
  public void shouldAutoStopWhenSampleTimeMedianIsOverLimit() {
    AutoStopTestBean element = buildAutoStop(
        AutoStopCondition.sampleTime().percentile(50).every(ONE_SEC).greaterThan(ONE_SEC));
    sampleWithTime(Duration.ofSeconds(1), element);
    sampleWithTime(Duration.ofSeconds(2), element);
    sampleWithTime(Duration.ofSeconds(3), element);
    clock.tick();
    sample(element);
    assertAutoStop();
  }

  private void sampleWithTime(Duration duration, AutoStopTestBean element) {
    sample(r -> r.setStampAndTime(clock.instant.toEpochMilli(), duration.toMillis()), element);
  }

  @Test
  public void shouldNotAutoStopWhenSampleTimeMedianIsUnderLimit() {
    AutoStopTestBean element = buildAutoStop(
        AutoStopCondition.sampleTime().percentile(50).every(ONE_SEC).greaterThan(ONE_SEC));
    sampleWithTime(Duration.ofMillis(500), element);
    sampleWithTime(ONE_SEC, element);
    sampleWithTime(Duration.ofSeconds(2), element);
    clock.tick();
    sample(element);
    assertNotAutoStop();
  }

}
