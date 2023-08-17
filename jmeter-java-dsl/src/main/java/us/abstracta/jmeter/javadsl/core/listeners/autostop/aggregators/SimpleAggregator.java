package us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators;

import java.util.function.BiFunction;

public class SimpleAggregator<T extends Comparable<?>> implements AutoStopAggregator<T> {

  private final BiFunction<T, Long, T> reducer;
  private T value;

  public SimpleAggregator(BiFunction<T, Long, T> reducer) {
    this.reducer = reducer;
  }

  public void add(long value) {
    this.value = this.value != null ? reducer.apply(this.value, value) : (T) (Long) value;
  }

  public T getValue() {
    return value;
  }

}
