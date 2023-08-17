package us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators;

public class PercentAggregator implements AutoStopAggregator<Double> {

  private final AverageAggregator delegate = new AverageAggregator();

  @Override
  public void add(long value) {
    delegate.add(value);
  }

  @Override
  public Double getValue() {
    return delegate.getValue() * 100;
  }

}
