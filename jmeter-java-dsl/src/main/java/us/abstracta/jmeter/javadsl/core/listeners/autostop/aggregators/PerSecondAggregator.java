package us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators;

import java.time.Clock;
import java.time.Instant;

public class PerSecondAggregator implements AutoStopAggregator<Double> {

  private final Clock clock;
  private final AverageAggregator delegate = new AverageAggregator();
  private Instant nextSecond;
  private long countInSecond;

  public PerSecondAggregator(Clock clock) {
    this.clock = clock;
    this.nextSecond = clock.instant().plusSeconds(1);
  }

  @Override
  public void add(long value) {
    Instant now = clock.instant();
    if (nextSecond.compareTo(now) > 0) {
      countInSecond++;
      return;
    }
    delegate.add(countInSecond);
    nextSecond = nextSecond.plusSeconds(1);
    while (nextSecond.compareTo(now) <= 0) {
      delegate.add(0L);
      nextSecond = nextSecond.plusSeconds(1);
    }
    countInSecond = 0;
  }

  @Override
  public Double getValue() {
    return delegate.getValue();
  }

}
