package us.abstracta.jmeter.javadsl.core.listeners.autostop;

import java.util.function.BiFunction;

public enum AutoStopComparison {
  LT("<", (v1, v2) -> v1.compareTo(v2) < 0),
  LTE("<=", (v1, v2) -> v1.compareTo(v2) <= 0),
  GT(">", (v1, v2) -> v1.compareTo(v2) > 0),
  GTE(">=", (v1, v2) -> v1.compareTo(v2) >= 0);

  private final String name;
  private final BiFunction<Comparable, Comparable, Boolean> comparator;

  AutoStopComparison(String name, BiFunction<Comparable, Comparable, Boolean> comparator) {
    this.name = name;
    this.comparator = comparator;
  }

  boolean compare(Comparable<?> v1, Comparable<?> v2) {
    return comparator.apply(v1, v2);
  }

  public String getName() {
    return name;
  }

}
