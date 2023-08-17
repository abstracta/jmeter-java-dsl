package us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators;

import java.text.DecimalFormat;
import org.apache.commons.math3.stat.descriptive.rank.PSquarePercentile;

public class PercentileAggregator implements AutoStopAggregator<Long> {

  private final PSquarePercentile percentile;

  public PercentileAggregator(double percentile) {
    this.percentile = new PSquarePercentile(percentile);
  }

  @Override
  public void add(long value) {
    percentile.increment(value);
  }

  @Override
  public Long getValue() {
    return Math.round(percentile.getResult());
  }

  public static String getName(double percentile) {
    return "percentile " + new DecimalFormat("#.##").format(percentile);
  }

}
