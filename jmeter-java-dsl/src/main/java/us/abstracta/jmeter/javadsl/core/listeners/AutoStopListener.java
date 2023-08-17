package us.abstracta.jmeter.javadsl.core.listeners;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.engines.TestStopper;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.AutoStopAggregation;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.AutoStopComparison;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.AutoStopConditionElement;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.AutoStopMetric;
import us.abstracta.jmeter.javadsl.core.listeners.autostop.AutoStopTestBean;
import us.abstracta.jmeter.javadsl.core.testelements.BaseTestElement;

/**
 * Allows stopping a test plan execution when collected statistics meet some defined criteria.
 *
 * @since 1.19
 */
public class AutoStopListener extends BaseTestElement implements DslListener {

  protected final List<AutoStopCondition> conditions = new ArrayList<>();
  protected Pattern regex;
  protected TestStopper testStopper;

  public AutoStopListener(String name) {
    super(name != null ? name : "AutoStop", TestBeanGUI.class);
  }

  /**
   * Specifies a regular expression used to filter samples for which specified conditions will be
   * evaluated.
   * <p>
   * If a condition defines another regular expression, then this expression is ignored for such
   * condition.
   * <p>
   * This is handy in case you want to define a set of conditions that apply only to a sub set of
   * samples that share a common label structure. Eg: to samplers with same name but in different
   * parts of test plan (e.g.: under different ifControllers).
   * <p>
   * For targeting a single sampler or all the samplers contained within a controller or thread
   * group, prefer just placing the autoStop listener in the correct scope of the test plan.
   *
   * @param regex specifies the regular expression to filter samples.
   * @return the listener for further configuration or usage in a test plan.
   */
  public AutoStopListener samplesMatching(String regex) {
    this.regex = Pattern.compile(regex);
    return this;
  }

  /**
   * Specifies conditions that when met will cause the test plan execution to stop.
   *
   * @param condition specifies the condition that samples will be evaluated against.
   * @return the listener for further configuration or usage in a test plan.
   */
  public AutoStopListener when(AutoStopCondition condition) {
    conditions.add(condition);
    return this;
  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    testStopper = context.getTestStopper();
    return super.buildTreeUnder(parent, context);
  }

  @Override
  protected TestElement buildTestElement() {
    return new AutoStopTestBean(regex, conditions.stream()
        .map(c -> c.element)
        .collect(Collectors.toList()),
        testStopper);
  }

  /**
   * Specifies a condition that when met, will make test plan execution to stop.
   */
  public static class AutoStopCondition {

    private final AutoStopConditionElement element;

    private AutoStopCondition(AutoStopConditionElement element) {
      this.element = element;
    }

    /**
     * Specifies a regular expression used to filter samples for which the condition will be
     * evaluated.
     * <p>
     * When the condition defines a regular expression, then the one specified in the autoStop
     * listener is ignored.
     * <p>
     * This is handy in case you want to define a condition that apply only to a sub set of samples
     * that share a common label structure. Eg: to samplers with same name but in different parts of
     * test plan (e.g.: under different ifControllers).
     * <p>
     * For targeting a single sampler or all the samplers contained within a controller or thread
     * group, prefer just placing the autoStop listener in the correct scope of the test plan.
     *
     * @param regex specifies the regular expression to filter samples.
     * @return a condition builder to complete the condition definition.
     */
    public static AutoStopConditionBuilder samplesMatching(String regex) {
      return new AutoStopConditionBuilder()
          .samplesMatching(regex);
    }

    /**
     * Specifies to create a condition that evaluates sample time (time between last request byte is
     * sent to service under test and last response byte is received).
     *
     * @return a condition builder to complete the condition definition.
     */
    public static TimeMetricConditionBuilder sampleTime() {
      return new AutoStopConditionBuilder()
          .sampleTime();
    }

    /**
     * Specifies to create a condition that evaluates latency time (time between last request byte
     * is sent to service under test and first response byte is received).
     *
     * @return a condition builder to complete the condition definition.
     */
    public static TimeMetricConditionBuilder latencyTime() {
      return new AutoStopConditionBuilder()
          .latencyTime();
    }

    /**
     * Specifies to create a condition that evaluates connection time (time it takes to establish a
     * connection with the service under test).
     *
     * @return a condition builder to complete the condition definition.
     */
    public static TimeMetricConditionBuilder connectionTime() {
      return new AutoStopConditionBuilder()
          .connectionTime();
    }

    /**
     * Specifies to create a condition that evaluates the number of samples.
     *
     * @return a condition builder to complete the condition definition.
     */
    public static CountMetricConditionBuilder samples() {
      return new AutoStopConditionBuilder()
          .samples();
    }

    /**
     * Specifies to create a condition that evaluates the number of not successful samples.
     *
     * @return a condition builder to complete the condition definition.
     */
    public static ErrorsConditionBuilder errors() {
      return new AutoStopConditionBuilder()
          .errors();
    }

    /**
     * Specifies to create a condition that evaluates the number bytes sent (request bytes) to the
     * service under test.
     *
     * @return a condition builder to complete the condition definition.
     */
    public static CountMetricConditionBuilder sentBytes() {
      return new AutoStopConditionBuilder()
          .sentBytes();
    }

    /**
     * Specifies to create a condition that evaluates the number bytes (response bytes) received
     * from the service under test.
     *
     * @return a condition builder to complete the condition definition.
     */
    public static CountMetricConditionBuilder receivedBytes() {
      return new AutoStopConditionBuilder()
          .receivedBytes();
    }

    /**
     * Specifies for how long the specified condition has to hold (be met) before the test plan
     * execution is stopped.
     * <p>
     * This is useful to avoid potential short temporal conditions that may need to be ignored.
     *
     * @param duration specifies the duration to wait the condition to hold before stopping test
     *                 plan execution.
     *                 <p>
     *                 The granularity of the period has to be seconds or greater (milliseconds are
     *                 ignored).
     *                 <p>
     *                 By default, it is set to 0, which means that it will stop test plan as soon
     *                 as the condition is met.
     * @return the condition for its usage.
     */
    public AutoStopCondition holdsFor(Duration duration) {
      element.setHoldsForSeconds(duration.getSeconds());
      return this;
    }

  }

  public static class AutoStopConditionBuilder extends BaseAutoStopConditionBuilder {

    public AutoStopConditionBuilder() {
      super(new AutoStopConditionElement());
    }

    /**
     * @see AutoStopCondition#samplesMatching(String)
     */
    public AutoStopConditionBuilder samplesMatching(String regex) {
      ret.setRegex(regex);
      return this;
    }

    /**
     * @see AutoStopCondition#sampleTime()
     */
    public TimeMetricConditionBuilder sampleTime() {
      return new TimeMetricConditionBuilder(ret, AutoStopMetric.SAMPLE_TIME);
    }

    /**
     * @see AutoStopCondition#latencyTime()
     */
    public TimeMetricConditionBuilder latencyTime() {
      return new TimeMetricConditionBuilder(ret, AutoStopMetric.LATENCY);
    }

    /**
     * @see AutoStopCondition#connectionTime()
     */
    public TimeMetricConditionBuilder connectionTime() {
      return new TimeMetricConditionBuilder(ret, AutoStopMetric.CONNECT_TIME);
    }

    /**
     * @see AutoStopCondition#samples()
     */
    public CountMetricConditionBuilder samples() {
      return new CountMetricConditionBuilder(ret, AutoStopMetric.SAMPLES);
    }

    /**
     * @see AutoStopCondition#errors()
     */
    public ErrorsConditionBuilder errors() {
      return new ErrorsConditionBuilder(ret);
    }

    /**
     * @see AutoStopCondition#sentBytes()
     */
    public CountMetricConditionBuilder sentBytes() {
      return new CountMetricConditionBuilder(ret, AutoStopMetric.SENT_BYTES);
    }

    /**
     * @see AutoStopCondition#receivedBytes()
     */
    public CountMetricConditionBuilder receivedBytes() {
      return new CountMetricConditionBuilder(ret, AutoStopMetric.RECEIVED_BYTES);
    }

  }

  public abstract static class BaseAutoStopConditionBuilder {

    protected final AutoStopConditionElement ret;

    protected BaseAutoStopConditionBuilder(AutoStopConditionElement ret) {
      this.ret = ret;
    }

  }

  public abstract static class MetricConditionBuilder extends BaseAutoStopConditionBuilder {

    protected MetricConditionBuilder(AutoStopConditionElement ret, AutoStopMetric metric) {
      super(ret);
      ret.setMetric(metric.toString());
    }

  }

  public static class TimeMetricConditionBuilder extends MetricConditionBuilder {

    protected TimeMetricConditionBuilder(AutoStopConditionElement ret, AutoStopMetric metric) {
      super(ret, metric);
    }

    /**
     * Specifies to check the minimum value of the selected condition metric.
     * <p>
     * By default, the minimum is not reset during a test plan execution and is evaluated on each
     * sample result. If you want to change this you can use
     * {@link AggregatedConditionBuilder#every(Duration)}.
     *
     * @return a condition builder to complete the condition definition.
     */
    public AggregatedConditionBuilder<Duration> min() {
      return new AggregatedConditionBuilder<>(ret, AutoStopAggregation.MIN);
    }

    /**
     * Specifies to check the maximum value of the selected condition metric.
     * <p>
     * By default, the maximum is not reset during a test plan execution and is evaluated on each
     * sample result. If you want to change this you can use
     * {@link AggregatedConditionBuilder#every(Duration)}.
     *
     * @return a condition builder to complete the condition definition.
     */
    public AggregatedConditionBuilder<Duration> max() {
      return new AggregatedConditionBuilder<>(ret, AutoStopAggregation.MAX);
    }

    /**
     * Specifies to check the mean/average value of the selected condition metric.
     * <p>
     * By default, the mean is not reset during a test plan execution and is evaluated on each
     * sample result. This might lead to the mean value being "stuck" by past sample results. Check
     * {@link AggregatedConditionBuilder#every(Duration)} for more details.
     *
     * @return a condition builder to complete the condition definition.
     */
    public AggregatedConditionBuilder<Duration> mean() {
      return new AggregatedConditionBuilder<>(ret, AutoStopAggregation.MEAN);
    }

    /**
     * Specifies to check a given percentile value of the selected condition metric.
     * <p>
     * By default, the percentile is not reset during a test plan execution and is evaluated on each
     * sample result. If you want to change this you can use
     * {@link AggregatedConditionBuilder#every(Duration)}.
     * <p>
     * <b>Warning:</b> as percentiles are calculated with P<SUP>2</SUP> algorithm, they may not be
     * accurate when evaluating a few samples. This is specially important when using small
     * aggregation periods with {@link AggregatedConditionBuilder#every(Duration)}.
     *
     * @param percentile specifies the percentile to use. For example to check the median, specify
     *                   50. To check the 90 percentile, then specify 90.
     * @return a condition builder to complete the condition definition.
     */
    public AggregatedConditionBuilder<Duration> percentile(double percentile) {
      ret.setPercentile(percentile);
      return new AggregatedConditionBuilder<>(ret, AutoStopAggregation.PERCENTILE);
    }

  }

  public static class AggregatedConditionBuilder<T extends Comparable<?>> extends
      BaseAutoStopConditionBuilder {

    protected AggregatedConditionBuilder(AutoStopConditionElement ret,
        AutoStopAggregation aggregation) {
      super(ret);
      ret.setAggregation(aggregation.toString());
    }

    /**
     * Specifies the period of time between each aggregation (min, max, etc) evaluation and reset.
     * <p>
     * Having aggregations being evaluated and reset for every given period of time, avoids the
     * aggregations to be "stuck" due to historical values and not focusing on the most current
     * collected metrics. For example if in your first 10 minutes of your test plan you collected
     * 10k requests with an average sample time of 500ms, but in the last 5 seconds you got 10
     * samples with an average sample time of 1 minute, you would probably like to stop test plan
     * execution regardless of the average since the beginning of the test plan being 500ms.
     * <p>
     * Evaluating aggregations in periods slots is particularly helpful for aggregations like
     * {@link TimeMetricConditionBuilder#mean}, {@link CountMetricConditionBuilder#perSecond()} and
     * {@link ErrorsConditionBuilder#percent}.
     *
     * @param period specifies the period of time for aggregation evaluation and reset.
     *               <p>
     *               The granularity of the period has to be seconds or greater (milliseconds are
     *               ignored).
     *               <p>
     *               By default, is set to 0, which means that aggregations are evaluated for every
     *               sample and never rest.
     * @return a condition builder to complete the condition definition.
     */
    public AggregatedConditionBuilder<T> every(Duration period) {
      ret.setAggregationResetPeriodSeconds(period.getSeconds());
      return this;
    }

    /**
     * Specifies to check the aggregated metric value to be less than a provided one.
     *
     * @param value specifies the value to check the aggregated value against.
     * @return a condition builder to complete the condition definition.
     */
    public AutoStopCondition lessThan(T value) {
      return comparing(AutoStopComparison.LT, value);
    }

    private AutoStopCondition comparing(AutoStopComparison comparison, T value) {
      ret.setComparison(comparison.toString());
      ret.setValue(value instanceof Duration ? ((Duration) value).toMillis() : value);
      return new AutoStopCondition(ret);
    }

    /**
     * Specifies to check the aggregated metric value to be less than or equal to a provided one.
     *
     * @param value specifies the value to check the aggregated value  against.
     * @return a condition builder to complete the condition definition.
     */
    public AutoStopCondition lessThanOrEqualTo(T value) {
      return comparing(AutoStopComparison.LTE, value);
    }

    /**
     * Specifies to check the aggregated metric value to be greater than a provided one.
     *
     * @param value specifies the value to check the aggregated value  against.
     * @return a condition builder to complete the condition definition.
     */
    public AutoStopCondition greaterThan(T value) {
      return comparing(AutoStopComparison.GT, value);
    }

    /**
     * Specifies to check the aggregated metric value to be greater than or equal to a provided
     * one.
     *
     * @param value specifies the value to check the aggregated value  against.
     * @return a condition builder to complete the condition definition.
     */
    public AutoStopCondition greaterThanOrEqualTo(T value) {
      return comparing(AutoStopComparison.GTE, value);
    }

  }

  public static class CountMetricConditionBuilder extends MetricConditionBuilder {

    protected CountMetricConditionBuilder(AutoStopConditionElement ret, AutoStopMetric metric) {
      super(ret, metric);
    }

    /**
     * Specifies to check the total sum of selected condition metric.
     * <p>
     * By default, the total is not reset during a test plan execution and is evaluated on each
     * sample result. If you want to change this you can use
     * {@link AggregatedConditionBuilder#every(Duration)}.
     *
     * @return a condition builder to complete the condition definition.
     */
    public AggregatedConditionBuilder<Long> total() {
      return new AggregatedConditionBuilder<>(ret, AutoStopAggregation.TOTAL);
    }

    /**
     * Specifies to check the average sum per second of selected condition metric.
     * <p>
     * By default, the aggregated value is not reset during a test plan execution and is evaluated
     * on each sample result. This might lead to the value being "stuck" by past sample results.
     * Check {@link AggregatedConditionBuilder#every(Duration)} for more details.
     *
     * @return a condition builder to complete the condition definition.
     */
    public AggregatedConditionBuilder<Double> perSecond() {
      return new AggregatedConditionBuilder<>(ret, AutoStopAggregation.PER_SECOND);
    }

  }

  public static class ErrorsConditionBuilder extends CountMetricConditionBuilder {

    protected ErrorsConditionBuilder(AutoStopConditionElement ret) {
      super(ret, AutoStopMetric.ERRORS);
    }

    /**
     * Specifies to check the error percentage.
     * <p>
     * By default, the percentage is not reset during a test plan execution and is evaluated on each
     * sample result. This might lead to the value being "stuck" by past sample results. Check
     * {@link AggregatedConditionBuilder#every(Duration)} for more details.
     *
     * @return a condition builder to complete the condition definition.
     */
    public AggregatedConditionBuilder<Double> percent() {
      return new AggregatedConditionBuilder<>(ret, AutoStopAggregation.PERCENT);
    }

  }

}
