package us.abstracta.jmeter.javadsl.core.configs;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.modifiers.gui.CounterConfigGui;
import org.apache.jmeter.testelement.TestElement;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;

/**
 * Allows easy usage of auto incremental values in test plans.
 * <p>
 * This element is handy for generating incremental IDs, positions in certain list, etc.
 *
 * @since 1.10
 */
public class DslCounter extends BaseConfigElement {

  private final String varName;
  private String start = "0";
  private long increment = 1;
  private long max = Long.MAX_VALUE;
  private boolean perThread = false;

  public DslCounter(String varName) {
    super(varName, CounterConfigGui.class);
    this.varName = varName;
  }

  /**
   * Allows specifying the starting value of the counter.
   *
   * @param start specifies the value to start the counter with. When not specified, 0 will be
   *              used.
   * @return the counter for further configuration and usage.
   */
  public DslCounter startingValue(long start) {
    return startingValue(String.valueOf(start));
  }

  /**
   * Same as {@link #startingValue(long)} but allowing to use JMeter expressions for starting
   * value.
   * <p>
   * This method allows to extract the initial value of the counter for example from a JMeter
   * property (eg: ${__P(COUNT_INIT)}).
   *
   * @param start specifies a jmeter expression evaluating to a number that specifies the initial
   *              value for the counter.
   * @return the counter for further configuration and usage.
   */
  public DslCounter startingValue(String start) {
    this.start = start;
    return this;
  }

  /**
   * Specifies how much the counter will increase in each iteration.
   *
   * @param inc specifies how much to increase the counter in each iteration. By default, 1.
   * @return the counter for further configuration and usage.
   * @since 1.22
   */
  public DslCounter increment(long inc) {
    this.increment = inc;
    return this;
  }

  /**
   * Specifies the maximum value of the counter.
   * <p>
   * When the value exceeds this value, the counter is reset to its starting value.
   *
   * @param max specifies the maximum value to use. When not specified, {@link Long#MAX_VALUE} is
   *            used.
   * @return the counter for further configuration and usage.
   * @since 1.22
   */
  public DslCounter maximumValue(long max) {
    this.max = max;
    return this;
  }

  /**
   * Specifies to use a separate counter for each thread.
   *
   * @param perThread specifies to use a separate counter for each thread. When not specified, then
   *                  the counter is shared, and incremented, by all thread group threads. By
   *                  default, it is set to false.
   * @return the counter for further configuration and usage.
   * @since 1.22
   */
  public DslCounter perThread(boolean perThread) {
    this.perThread = perThread;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    CounterConfig ret = new CounterConfig();
    ret.setVarName(varName);
    ret.setStart(start);
    ret.setIncrement(increment);
    ret.setEnd(max);
    ret.setIsPerUser(perThread);
    return ret;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<CounterConfig> {

    public CodeBuilder(List<Method> builderMethods) {
      super(CounterConfig.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(CounterConfig testElement, MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement,
          "CounterConfig");
      MethodCall ret = buildMethodCall(paramBuilder.stringParam("name"));
      ret.chain("startingValue", paramBuilder.longParam("start", 0L));
      ret.chain("increment", paramBuilder.longParam("incr", 1L));
      ret.chain("maximumValue", paramBuilder.longParam("end", Long.MAX_VALUE));
      ret.chain("perThread", paramBuilder.boolParam("per_user", false));
      return ret;
    }

  }

}
