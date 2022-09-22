package us.abstracta.jmeter.javadsl.core.threadgroups;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.PostThreadGroup;
import org.apache.jmeter.threads.gui.PostThreadGroupGui;

/**
 * A thread group that allows running any logic after any other thread group.
 * <p>
 * Usually this thread group is used to do some clean up in target environment, leave it in a
 * predefined state, or to use some information obtained from the rest of the test.
 * <p>
 * Take into consideration when using information from other thread groups that jmeter variables
 * (which are thread scoped) created or updated in other thread groups are not visible in this
 * thread group. One way of passing information from one thread group to others can be JMeter
 * Properties, or files. But use them with care, taking into consideration potentially parallel
 * execution of threads in same thread group (or other thread groups).
 * <p>
 * Also consider that if you add multiple teardown thread groups in a plan, they will run in
 * parallel, after "regular" thread groups.
 * <p>
 * By default, the thread group will be configured with 1 thread and 1 iteration. You can change
 * this with provided methods.
 *
 * @since 0.33
 */
public class DslTeardownThreadGroup extends DslSimpleThreadGroup<DslTeardownThreadGroup> {

  private static final String DEFAULT_NAME = "tearDown Thread Group";

  public DslTeardownThreadGroup(String name, List<ThreadGroupChild> children) {
    super(name != null ? name : DEFAULT_NAME, PostThreadGroupGui.class, children);
  }

  @Override
  protected AbstractThreadGroup buildSimpleThreadGroup() {
    return new PostThreadGroup();
  }

  public static class CodeBuilder extends SimpleThreadGroupCodeBuilder<PostThreadGroup> {

    public CodeBuilder(List<Method> builderMethods) {
      super(PostThreadGroup.class, DEFAULT_NAME, builderMethods);
    }

  }

}
