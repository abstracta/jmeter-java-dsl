package us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators;

public interface AutoStopAggregator<T extends Comparable<?>> {

  void add(long value);

  T getValue();

}
