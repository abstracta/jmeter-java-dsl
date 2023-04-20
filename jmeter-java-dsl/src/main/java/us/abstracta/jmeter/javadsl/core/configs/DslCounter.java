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

  @Override
  protected TestElement buildTestElement() {
    CounterConfig ret = new CounterConfig();
    ret.setVarName(varName);
    ret.setStart(start);
    ret.setIncrement(1);
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
      return ret;
    }

  }

}
