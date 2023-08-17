package us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators;

public class AverageAggregator implements AutoStopAggregator<Double> {

  private double val;
  private long count;

  @Override
  public void add(long value) {
    count++;
    val = count == 1 ? value
        /*
        using this formula instead of (value * count-1 + value)/count, or keeping total and count
        and divide at end, to avoid overflow and reduce precision loss while working with big
        numbers
        */
        : (double) value / count + this.val * ((double) (count - 1) / count);
  }

  @Override
  public Double getValue() {
    return val;
  }

}
