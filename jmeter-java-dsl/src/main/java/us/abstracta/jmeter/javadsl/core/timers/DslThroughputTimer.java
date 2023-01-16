package us.abstracta.jmeter.javadsl.core.timers;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.timers.ConstantThroughputTimer;
import org.apache.jmeter.timers.ConstantThroughputTimer.Mode;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCall;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallContext;
import us.abstracta.jmeter.javadsl.codegeneration.SingleTestElementCallBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.TestElementParamBuilder;
import us.abstracta.jmeter.javadsl.codegeneration.params.EnumParam.EnumPropertyValue;
import us.abstracta.jmeter.javadsl.core.BuildTreeContext;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

/**
 * Allows using JMeter Constant Throughput Timers which pauses samplers under its control to limit
 * the maximum number of samples per minute.
 * <p>
 * If you want the general throughput to be close to the specified number, and not significantly
 * lower, then you have to make sure that the number of threads must be sufficient according to
 * expected sampler response times.
 * <p>
 * The positioning of the timer determines its scope (as any other timer). I.e. at test plan level
 * to control the pacing of multiple samples across multiple thread groups, under a Thread Group to
 * just influence samplers in that Thread Group, or as a child of a sampler to only control that
 * sampler.
 * <p>
 * When located at test plan level, this timer will use  by default JMeter Constant Throughput Timer
 * All active threads with non-sharing calculation mode. Which means that it will control that the
 * total throughput for all requests in the test plan, across different thread groups, is at maximum
 * the given one. If you locate inside a thread group (or sampler), it will use All active threads
 * in current thread group with non-sharing mode. Avoiding potential problem of having two timers in
 * separate thread group interfering with each other. Additionally, default calculation modes don't
 * use shared modes since we have detected unexpected behaviors when using multiple timers.
 * Basically, timers delays calculations interfering with each other due to single test plan or
 * thread group mark used in calculation (instead of one per timer).
 * <p>
 * If you want to change the default calculation method, then you can use
 * {@link #calculation(ThroughputMode)} method. But in general avoid using it since may lead to
 * unexpected behaviors.
 *
 * @since 1.5
 */
public class DslThroughputTimer extends BaseTimer {

  protected double throughput;
  protected ThroughputMode calculation;

  public DslThroughputTimer(double samplesPerMinute) {
    super("Constant Throughput Timer", TestBeanGUI.class);
    this.throughput = samplesPerMinute;
  }

  /**
   * Allows specifying that configured throughput should be counted per thread.
   * <p>
   * I.e: if you have 10 active threads and configure throughput of 10 per thread, then the total
   * maximum throughput will be 10 * 10 = 100 requests per minute.
   *
   * @return the timer for further configuration or usage.
   */
  public DslThroughputTimer perThread() {
    calculation = ThroughputMode.PER_THREAD;
    return this;
  }

  /**
   * Allows specifying the exact method of calculation of throughput regardless of the location of
   * the timer.
   * <p>
   * This method should only be used when you actually need such control, since may lead to
   * unexpected behaviors when not used correctly. Eg: when there are two timers, each in different
   * thread group, and both of them using ALL_THREADS_ACCURATE.
   *
   * @param calculation specifies the way of calculating/control the throughput. When not specified,
   *                    the timer will use {@link ThroughputMode#ALL_THREADS_EVEN} if timer is at
   *                    test plan level, {@link ThroughputMode#THREAD_GROUP_EVEN} if timer is inside
   *                    a thread group and {@link ThroughputMode#PER_THREAD} if {@link #perThread()}
   *                    was specified.
   * @return the timer for further configuration or usage.
   * @see ThroughputMode
   */
  public DslThroughputTimer calculation(ThroughputMode calculation) {
    this.calculation = calculation;
    return this;
  }

  /**
   * Specifies the calculation/control method to control the configured throughput.
   */
  public enum ThroughputMode implements EnumPropertyValue {
    /**
     * The configured throughput specifies the maximum throughput for each thread. E.g.: if you
     * configured 10tpm, and you have 10 active threads, then the total throughput might be
     * 10*10=100 tpm.
     * <p>
     * This is the same as JMeter option "this thread only".
     */
    PER_THREAD,
    /**
     * The configured throughput will be divided among active threads (across thread groups) and
     * each thread will control that part of the throughput regardless if other threads were far
     * from expected throughput. E.g.: if you place the timer at test plan level, configure 10 tpm,
     * have 2 thread groups with 5 active threads each, then each thread will try to achieve 1tpm.
     * If one thread is slower than the rest, the other threads will not take that into account to
     * adjust their throughput to try to achieve the expected total throughput.
     * <p>
     * Check {@link #ALL_THREADS_ACCURATE} as an alternative.
     * <p>
     * In general use this mode only on test plan level timers, otherwise it might lead to confusion
     * or unexpected behavior. E.g.: avoid having timers inside thread groups with ALL_THREADS_EVEN.
     * Prefer in such scenarios {@link #THREAD_GROUP_EVEN}).
     * <p>
     * This is the same as Jmeter option "all active threads".
     */
    ALL_THREADS_EVEN,
    /**
     * The configured throughput will be divided among active threads of the current thread group
     * (ignoring other thread groups) and each thread will control that part of the throughput
     * regardless if other threads in the same thread group were far from expected throughput. E.g.:
     * if you place the timer inside a thread group, configure 10 tpm, and 10 active threads in the
     * thread group, each thread in the thread group will try to achieve 1tpm. If one thread in the
     * thread group is slower than the rest, the other threads in the thread group will not take
     * that into account to adjust their throughput to try to achieve the expected total
     * throughput.
     * <p>
     * If you place the timer at test plan level, then each thread group in the test plan will try
     * to achieve configured throughput.
     * <p>
     * Check {@link #THREAD_GROUP_ACCURATE} as an alternative.
     * <p>
     * This is the same as Jmeter option "all active threads in current thread group".
     */
    THREAD_GROUP_EVEN,
    /**
     * The configured throughput is controlled checking the last time each active thread executed.
     * <p>
     * This avoids not achieving the configured throughput when one thread is particularly slow,
     * making the timer behavior more accurate with expected behavior (than
     * {@link #ALL_THREADS_EVEN}). BUT, this timer uses only one mark for last thread execution,
     * shared across all timers using same calculation mode, which means that one timer calculation
     * might/will affect other timers calculations. So, if you use this method, make sure you only
     * use one timer with such method.
     * <p>
     * To avoid this issue you might use {@link #ALL_THREADS_EVEN}, but total throughput would not
     * adjust when some threads are slower than others.
     * <p>
     * In general use this mode only on test plan level timers, otherwise it might lead to confusion
     * or unexpected behavior. E.g.: avoid having timers inside thread groups with
     * ALL_THREADS_ACCURATE. Prefer in such scenarios {@link #THREAD_GROUP_ACCURATE}).
     * <p>
     * This is the same as Jmeter option "all active threads (shared)".
     */
    ALL_THREADS_ACCURATE,
    /**
     * The configured throughput is controlled checking the last time each active thread, in the
     * thread group, executed.
     * <p>
     * This avoids not achieving the configured throughput when one thread is particularly slow,
     * making the timer behavior more accurate with expected behavior (than
     * {@link #THREAD_GROUP_EVEN}). BUT, this timer uses only one mark per thread group for last
     * thread execution, shared across all timers in same thread group using same calculation mode,
     * which means that one timer calculation might/will affect other timers calculations in same
     * thread group. So, if you use this method, make sure you only use one timer per thread group
     * with such method.
     * <p>
     * To avoid this issue you might use {@link #THREAD_GROUP_EVEN}, but total throughput would not
     * adjust when some threads are slower than others.
     * <p>
     * If you place the timer at test plan level, then each thread group in the test plan will try
     * to achieve configured throughput.
     * <p>
     * This is the same as Jmeter option "all active threads in current thread group (shared)".
     */
    THREAD_GROUP_ACCURATE;

    @Override
    public String propertyValue() {
      return String.valueOf(ordinal());
    }

  }

  @Override
  public HashTree buildTreeUnder(HashTree parent, BuildTreeContext context) {
    ConstantThroughputTimer ret = new ConstantThroughputTimer();
    ret.setThroughput(throughput);
    ThroughputMode calcMode = calculation;
    if (calcMode == null) {
      calcMode = parentIsTestPlan(context) ? ThroughputMode.ALL_THREADS_EVEN
          : ThroughputMode.THREAD_GROUP_EVEN;
    }
    ret.setCalcMode(calcMode.ordinal());
    return parent.add(configureTestElement(ret, name, guiClass));
  }

  private boolean parentIsTestPlan(BuildTreeContext context) {
    return context.getParent().getTestElement() instanceof DslTestPlan;
  }

  @Override
  protected TestElement buildTestElement() {
    /*
     We just return null since the element is built in buldTreeUnder, and this method is never
     invoked.
     */
    return null;
  }

  public static class CodeBuilder extends SingleTestElementCallBuilder<ConstantThroughputTimer> {

    public CodeBuilder(List<Method> builderMethods) {
      super(ConstantThroughputTimer.class, builderMethods);
    }

    @Override
    protected MethodCall buildMethodCall(ConstantThroughputTimer testElement,
        MethodCallContext context) {
      TestElementParamBuilder paramBuilder = new TestElementParamBuilder(testElement);
      MethodCall ret = buildMethodCall(paramBuilder.doubleParam("throughput"));
      TestElement parent = context.getParent().getTestElement();
      int calcMode = testElement.getCalcMode();
      if (calcMode == Mode.ThisThreadOnly.ordinal()) {
        ret.chain("perThread");
      } else if (!(calcMode == Mode.AllActiveThreads.ordinal() && parent instanceof TestPlan
          || calcMode == Mode.AllActiveThreadsInCurrentThreadGroup.ordinal()
          && !(parent instanceof TestPlan))) {
        // using PER_THREAD as default, only because we know is not the case here
        ret.chain("calculation", paramBuilder.enumParam("calcMode", ThroughputMode.PER_THREAD));
      }
      return ret;
    }

  }

}
