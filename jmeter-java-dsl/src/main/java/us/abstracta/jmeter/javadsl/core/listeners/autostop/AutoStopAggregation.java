package us.abstracta.jmeter.javadsl.core.listeners.autostop;

import java.util.function.Function;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators.AutoStopAggregator;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators.AverageAggregator;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators.PerSecondAggregator;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators.PercentAggregator;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators.PercentileAggregator;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators.SimpleAggregator;

public enum AutoStopAggregation {
  MIN(c -> "min", c -> new SimpleAggregator<Long>(Math::min)),
  MAX(c -> "max", c -> new SimpleAggregator<Long>(Math::max)),
  MEAN(c -> "mean", c -> new AverageAggregator()),
  PERCENTILE(c -> PercentileAggregator.getName(c.getPercentile()),
      c -> new PercentileAggregator(c.getPercentile())),
  TOTAL(c -> "total", c -> new SimpleAggregator<>(Long::sum)),
  PER_SECOND(c -> "per second", c -> new PerSecondAggregator(c.getClock())),
  PERCENT(c -> "percent", c -> new PercentAggregator());

  private final Function<AutoStopConditionElement, String> nameSolver;
  private final Function<AutoStopConditionElement, AutoStopAggregator<?>> aggregatorBuilder;

  AutoStopAggregation(Function<AutoStopConditionElement, String> nameSolver,
      Function<AutoStopConditionElement, AutoStopAggregator<?>> aggregatorBuilder) {
    this.nameSolver = nameSolver;
    this.aggregatorBuilder = aggregatorBuilder;
  }

  public AutoStopAggregator<?> buildAggregator(AutoStopConditionElement condition) {
    return aggregatorBuilder.apply(condition);
  }

  public String getNameFor(AutoStopConditionElement condition) {
    return nameSolver.apply(condition);
  }

}
