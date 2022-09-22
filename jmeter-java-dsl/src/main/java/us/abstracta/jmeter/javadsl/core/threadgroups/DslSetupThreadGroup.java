package us.abstracta.jmeter.javadsl.core.threadgroups;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.threads.gui.SetupThreadGroupGui;

/**
 * A thread group that allows running any logic before any other thread group.
 * <p>
 * Usually this thread group is used to set the target environment in a predefined state, or to load
 * information that is required in the rest of the test.
 * <p>
 * Take into consideration when loading information, as this is a separate thread group, jmeter
 * variables (which are thread scoped) are not available in other thread groups. One way of passing
 * information from one thread group to others can be JMeter Properties, or files. But use them with
 * care, taking into consideration potentially parallel execution of threads in same thread group
 * (or other thread groups).
 * <p>
 * Also consider that if you add multiple setup thread groups in a plan, they will run in parallel,
 * before "regular" thread groups.
 * <p>
 * By default, the thread group will be configured with 1 thread and 1 iteration. You can change
 * this with provided methods.
 *
 * @since 0.33
 */
public class DslSetupThreadGroup extends DslSimpleThreadGroup<DslSetupThreadGroup> {

  private static final String DEFAULT_NAME = "setUp Thread Group";

  public DslSetupThreadGroup(String name, List<ThreadGroupChild> children) {
    super(name != null ? name : DEFAULT_NAME, SetupThreadGroupGui.class, children);
  }

  @Override
  protected AbstractThreadGroup buildSimpleThreadGroup() {
    return new SetupThreadGroup();
  }

  public static class CodeBuilder extends SimpleThreadGroupCodeBuilder<SetupThreadGroup> {

    public CodeBuilder(List<Method> builderMethods) {
      super(SetupThreadGroup.class, DEFAULT_NAME, builderMethods);
    }

  }

}
