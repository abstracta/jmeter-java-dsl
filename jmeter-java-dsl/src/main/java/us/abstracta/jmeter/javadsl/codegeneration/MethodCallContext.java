package us.abstracta.jmeter.javadsl.codegeneration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains information and logic needed by {@link MethodCallBuilder} instances to create
 * MethodCalls for a given JMeter test plan subtree.
 * <p>
 * One MethodCallContext is created for each JMeter test element in a test plan tree, and they are
 * linked in tree structure (through {@link #getParent()} method to provide entire structure to
 * {@link MethodCallBuilder} instances when building a MethodCall
 *
 * @since 0.45
 */
public class MethodCallContext {

  private static final Logger LOG = LoggerFactory.getLogger(MethodCallContext.class);
  private static final String UNSUPPORTED_USAGE_WARNING = "Using unsupported() as parent for "
      + "children's conversions and ease manual code completion.";

  private final TestElement testElement;
  private final HashTree childrenThree;
  private final MethodCallContext parent;
  private final DslCodeGenerator codeGenerator;
  private final Map<Object, Object> entries = new HashMap<>();
  private final List<MethodCallContextEndListener> endListeners = new ArrayList<>();

  protected MethodCallContext(TestElement testElement, HashTree childrenThree,
      DslCodeGenerator codeGenerator) {
    this(testElement, childrenThree, null, codeGenerator);
  }

  protected MethodCallContext(TestElement testElement, HashTree childrenThree,
      MethodCallContext parent, DslCodeGenerator codeGenerator) {
    this.testElement = testElement;
    this.childrenThree = childrenThree;
    this.parent = parent;
    this.codeGenerator = codeGenerator;
  }

  /**
   * Gets the JMeter test element associated to this context.
   *
   * @return the test element.
   */
  public TestElement getTestElement() {
    return testElement;
  }

  /**
   * Gets the parent context.
   * <p>
   * This is useful in some scenarios to register end listeners on parent node, or access root
   * context for globally shared entries (check {@link #setEntry(Object, Object)}).
   *
   * @return the parent context. Null is returned if the current context is the root context.
   */
  public MethodCallContext getParent() {
    return parent;
  }

  /**
   * Gets the root context associated to the test plan.
   * <p>
   * This is useful when some data has to only be processed once and at root of the test plan build
   * context.
   *
   * @return the parent context. Null is returned if the current context is the root context.
   */
  public MethodCallContext getRoot() {
    return parent == null ? this : parent.getRoot();
  }

  /**
   * Gets the JMeter test plan subtree of children elements of current context test element.
   * <p>
   * This is useful when some alteration or inspection is required in the tree before other builder
   * methods try to convert contained test elements.
   * <p>
   * Eg: {@link us.abstracta.jmeter.javadsl.http.DslHttpSampler.CodeBuilder} uses this method to
   * remove children HTTP Headers which are directly included as chained methods of httpSampler
   * method invocation.
   *
   * @return the JMeter test plan children subtree.
   */
  public HashTree getChildrenTree() {
    return childrenThree;
  }

  /**
   * Gets a value associated to a given key in the context.
   * <p>
   * The context allows you to store (through {@link #setEntry(Object, Object)}) any sort of
   * information on it that may be required later on be used by the builder in some other test
   * element context (for example: check if a test element already was processed by this builder).
   *
   * @param key is an object identifying an entry in the context. A simple way of sharing info for a
   *            MethodCallBuilder is just use the MethodCallBuilder class as key, storing some
   *            custom class instance with structured context info for the particular
   *            MethodCallBuilder.
   * @return the value associated to the key. Null is returned if no entry is associated to the key.
   */
  public Object getEntry(Object key) {
    return entries.get(key);
  }

  /**
   * Allows to store a value associated to a given key in the context.
   *
   * @param key   identifies the entry in context to later on be able to retrieve it.
   * @param value the value to store in the context, associated to the given key.
   * @see #getEntry(Object) for more details
   */
  public void setEntry(Object key, Object value) {
    entries.put(key, value);
  }

  /**
   * Allows registering logic that needs to be executed at the end of MethodCall build for this
   * context.
   * <p>
   * This allows to do some advance stuff, like registering some action/logic to be executed on a
   * parent context after processing current context and only under some specific condition (eg:
   * when no other sibling test element is included in parent context).
   *
   * @param listener specifies the listener containing the logic to be executed at the end of
   *                 MethodCall build.
   */
  public void addEndListener(MethodCallContextEndListener listener) {
    endListeners.add(listener);
  }

  /**
   * Builds a MethodCall for the current context.
   * <p>
   * This might be useful in some MethodCallBuilders to trigger a build of children context (after
   * removal for example).
   *
   * @return the {@link MethodCall} instance.
   */
  public MethodCall buildMethodCall() {
    try {
      MethodCall ret = codeGenerator.getBuilders().stream()
          .filter(b -> b.matches(this))
          .map(b -> b.buildMethodCall(this))
          .findAny()
          .orElseGet(() -> {
            LOG.warn("No builder found for {}. " + UNSUPPORTED_USAGE_WARNING, testElement);
            return MethodCall.buildUnsupported();
          });
      addChildrenTo(ret);
      endListeners.forEach(l -> l.execute(this, ret));
      return ret;
    } catch (UnsupportedOperationException e) {
      LOG.warn("Could not build code for {}. " + UNSUPPORTED_USAGE_WARNING, testElement, e);
      return MethodCall.buildUnsupported();
    }
  }

  private void addChildrenTo(MethodCall call) {
    List<MethodCall> children = buildChildrenMethodCalls();
    if (children.isEmpty()) {
      return;
    }
    children.forEach(call::child);
  }

  private List<MethodCall> buildChildrenMethodCalls() {
    return childrenThree == null ? Collections.emptyList() : childrenThree.list().stream()
        .map(c -> (TestElement) c)
        .filter(TestElement::isEnabled)
        .map(c -> new MethodCallContext(c, childrenThree.getTree(c), this,
            codeGenerator).buildMethodCall())
        .collect(Collectors.toList());
  }

  /**
   * Allows removing an instance of the given test element class from the children tree.
   * <p>
   * If multiple instances exists, then only the first one is removed.
   *
   * @param testElementClass specifies the class of the test elements to be removed.
   * @return the context associated to the removed test element, or null if no test element is
   * found.
   */
  public MethodCallContext removeChild(Class<? extends TestElement> testElementClass) {
    Optional<?> child = childrenThree.list().stream()
        .filter(testElementClass::isInstance)
        .map(testElementClass::cast)
        .findAny();
    child.ifPresent(childrenThree::remove);
    return child
        .map(c -> new MethodCallContext((TestElement) c, childrenThree.getTree(c), this,
            codeGenerator))
        .orElse(null);
  }

  /**
   * Finds the builder associated to the given JMeter test element class.
   *
   * @param builderClass is the class of the builder to find.
   * @param <T>          is the type of the builder to find. This provides proper type safety when
   *                     using the method.
   * @return the builder associated to the given JMeter test element class, or null if none is
   * found.
   */
  public <T extends MethodCallBuilder> T findBuilder(Class<T> builderClass) {
    return codeGenerator.findBuilder(builderClass);
  }

  public interface MethodCallContextEndListener {

    void execute(MethodCallContext context, MethodCall call);
  }

}
