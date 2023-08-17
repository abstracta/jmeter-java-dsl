package us.abstracta.jmeter.javadsl.core.listeners.autostop;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.DoubleProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jorphan.documentation.VisibleForTesting;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.aggregators.AutoStopAggregator;

public class AutoStopConditionElement extends AbstractTestElement {

  private static final String REGEX_PROP = "regex";
  private static final String METRIC_PROP = "metric";
  private static final String AGGREGATION_PROP = "aggregation";
  private static final String PERCENTILE_PROP = "percentile";
  private static final String AGGREGATION_RESET_PERIOD_SECONDS_PROP =
      "aggregationResetPeriodSeconds";
  private static final String COMPARISON_PROP = "comparison";
  private static final String VALUE_PROP = "value";
  private static final String HOLDS_FOR_SECONDS_PROP = "holdsForSeconds";

  private Clock clock = Clock.systemUTC();
  private Instant slotStart;
  private AutoStopAggregator<?> aggregator;
  private Instant matchStart;

  public String getRegex() {
    return getPropertyAsString(REGEX_PROP, null);
  }

  public void setRegex(String regex) {
    setProperty(REGEX_PROP, regex);
  }

  public String getMetric() {
    return getPropertyAsString(METRIC_PROP);
  }

  public void setMetric(String metric) {
    setProperty(METRIC_PROP, metric);
  }

  public String getAggregation() {
    return getPropertyAsString(AGGREGATION_PROP);
  }

  public void setAggregation(String aggregation) {
    setProperty(AGGREGATION_PROP, aggregation);
  }

  public double getPercentile() {
    return getPropertyAsDouble(PERCENTILE_PROP);
  }

  public void setPercentile(double percentile) {
    setProperty(new DoubleProperty(PERCENTILE_PROP, percentile));
  }

  public long getAggregationResetPeriodSeconds() {
    return getPropertyAsLong(AGGREGATION_RESET_PERIOD_SECONDS_PROP);
  }

  public void setAggregationResetPeriodSeconds(long aggregationResetPeriod) {
    setProperty(AGGREGATION_RESET_PERIOD_SECONDS_PROP, aggregationResetPeriod);
  }

  public String getComparison() {
    return getPropertyAsString(COMPARISON_PROP);
  }

  public void setComparison(String comparison) {
    setProperty(COMPARISON_PROP, comparison);
  }

  public Object getValue() {
    JMeterProperty ret = getProperty(VALUE_PROP);
    // not using ternary operator to avoid long being cast to double
    if (ret instanceof DoubleProperty) {
      return ret.getDoubleValue();
    } else {
      return ret.getLongValue();
    }
  }

  public void setValue(Object value) {
    if (value instanceof Double) {
      setProperty(new DoubleProperty(VALUE_PROP, (Double) value));
    } else {
      setProperty(VALUE_PROP, (Long) value);
    }
  }

  public long getHoldsForSeconds() {
    return getPropertyAsLong(HOLDS_FOR_SECONDS_PROP);
  }

  public void setHoldsForSeconds(long holdsFor) {
    setProperty(HOLDS_FOR_SECONDS_PROP, holdsFor);
  }

  Clock getClock() {
    return clock;
  }

  @VisibleForTesting
  void setClock(Clock clock) {
    this.clock = clock;
  }

  public void start() {
    slotStart = clock.instant();
    aggregator = getAggregationEnumValue().buildAggregator(this);
  }

  private AutoStopAggregation getAggregationEnumValue() {
    return AutoStopAggregation.valueOf(getAggregation());
  }

  public boolean eval(SampleResult result) {
    if (getRegex() != null && !result.getSampleLabel().matches(getRegex())) {
      return false;
    }
    return getAggregationResetPeriodSeconds() == 0 ? isMatchNow(result) : isMatchSlot(result);
  }

  private boolean isMatchNow(SampleResult result) {
    aggregator.add(getMetricEnumValue().extractFrom(result));
    return isMatchAt(clock.instant());
  }

  private AutoStopMetric getMetricEnumValue() {
    return AutoStopMetric.valueOf(getMetric());
  }

  private boolean isMatchAt(Instant matchTime) {
    boolean matched = getComparisonEnumValue()
        .compare(aggregator.getValue(), (Comparable<?>) getValue());
    if (!matched) {
      matchStart = null;
      return false;
    }
    if (matchStart == null) {
      matchStart = matchTime;
    }
    return Duration.between(matchStart, matchTime).compareTo(getHoldsForDuration()) >= 0;
  }

  private AutoStopComparison getComparisonEnumValue() {
    return AutoStopComparison.valueOf(getComparison());
  }

  private Duration getHoldsForDuration() {
    return Duration.ofSeconds(getHoldsForSeconds());
  }

  private boolean isMatchSlot(SampleResult result) {
    Instant currentSlotStart = findSlotStart();
    while (!slotStart.equals(currentSlotStart)) {
      Instant slotEnd = slotStart.plusSeconds(getAggregationResetPeriodSeconds());
      if (isMatchAt(slotEnd)) {
        return true;
      }
      aggregator = getAggregationEnumValue().buildAggregator(this);
      slotStart = slotEnd;
    }
    aggregator.add(getMetricEnumValue().extractFrom(result));
    return false;
  }

  private Instant findSlotStart() {
    long startMillis = slotStart.toEpochMilli();
    long slotMillis = getAggregationResetPeriodSeconds() * 1000;
    long nowMillis = clock.instant().toEpochMilli();
    return slotStart.plusMillis(((nowMillis - startMillis) / slotMillis) * slotMillis);
  }

  @Override
  public String toString() {
    return String.format("%s%s %s (last value: %s) was %s %s%s", buildSamplesMatchingMessage(),
        getMetricEnumValue().getName(), getAggregationEnumValue().getNameFor(this),
        aggregator != null ? aggregator.getValue() : null, getComparisonEnumValue().getName(),
        getValue(), buildHoldsForMessage());
  }

  private String buildSamplesMatchingMessage() {
    String regex = getRegex();
    return regex != null ? "samples matching '" + regex + "' " : "";
  }

  private String buildHoldsForMessage() {
    Duration holdsForSeconds = getHoldsForDuration();
    return !holdsForSeconds.isZero() ? " for more than " + prettyDuration(holdsForSeconds) : "";
  }

  private String prettyDuration(Duration value) {
    ChronoUnit outUnit;
    long outValue;
    if (value.getNano() != 0) {
      outUnit = ChronoUnit.MILLIS;
      outValue = value.toMillis();
    } else if (value.toMinutes() * 60 != value.getSeconds()) {
      outUnit = ChronoUnit.SECONDS;
      outValue = value.getSeconds();
    } else if (value.toHours() * 60 != value.toMinutes()) {
      outUnit = ChronoUnit.MINUTES;
      outValue = value.toMinutes();
    } else if (value.toDays() * 24 != value.toHours()) {
      outUnit = ChronoUnit.HOURS;
      outValue = value.toHours();
    } else {
      outUnit = ChronoUnit.DAYS;
      outValue = value.toDays();
    }
    return String.format(outValue + " " + outUnit.toString().toLowerCase());
  }

}
