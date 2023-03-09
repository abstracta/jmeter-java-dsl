package us.abstracta.jmeter.javadsl.codegeneration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
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
  private final HashTree childrenTree;
  private final MethodCallContext parent;
  private final MethodCallContext root;
  private final MethodCallBuilderRegistry builderRegistry;
  private final Map<Object, Object> entries = new HashMap<>();
  private final List<MethodCallContextEndListener> endListeners = new ArrayList<>();
  private MethodCall methodCall;
  private final Map<TestElement, MethodCallContext> contextRegistry = new HashMap<>();
  private final Map<TestElement, UnaryOperator<MethodCall>> pendingReplacements = new HashMap<>();

  public MethodCallContext(TestElement testElement, HashTree childrenTree,
      MethodCallContext parent, MethodCallBuilderRegistry builderRegistry) {
    this.testElement = testElement;
    // sorting simplifies code builder
    this.childrenTree = childrenTree == null ? new ListedHashTree() : sortTree(childrenTree);
    this.parent = parent;
    this.root = parent == null ? this : parent.root;
    this.builderRegistry = builderRegistry;
  }

  private HashTree sortTree(HashTree tree) {
    ListedHashTree ret = new ListedHashTree();
    tree.list().stream()
        .sorted(Comparator.comparingInt(k -> k instanceof ConfigElement ? 0 : 1))
        .forEach(k -> ret.set(k, sortTree(tree.getTree(k))));
    return ret;
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
    return root;
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
    return childrenTree;
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
   * Gets existing entry or creates a new one using provided computation function.
   *
   * @param key         identifies the entry in context to later on be able to retrieve it.
   * @param computation function used to build the new entry for the given key, if none exists.
   * @see #getEntry(Object) for more details
   * @since 1.3
   */
  public <V> V computeEntryIfAbsent(Object key, Supplier<V> computation) {
    return (V) entries.computeIfAbsent(key, k -> computation.get());
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
      methodCall = builderRegistry.findBuilderMatchingContext(this)
          .map(b -> b.buildMethodCall(this))
          .orElseGet(() -> {
            LOG.warn("No builder found for {}({}). " + UNSUPPORTED_USAGE_WARNING,
                testElement.getClass(), testElement.getName());
            return MethodCall.buildUnsupported();
          });
      root.contextRegistry.put(testElement, this);
      methodCall.setCommented(!testElement.isEnabled());
      addChildrenTo(methodCall);
      executeEndListeners(methodCall);
      UnaryOperator<MethodCall> replacement = root.pendingReplacements.remove(testElement);
      if (replacement != null) {
        methodCall = replacement.apply(methodCall);
      }
      return methodCall;
    } catch (RuntimeException e) {
      LOG.warn("Could not build code for {}({}). " + UNSUPPORTED_USAGE_WARNING,
          testElement.getClass(), testElement.getName(), e);
      return MethodCall.buildUnsupported();
    }
  }

  private void executeEndListeners(MethodCall ret) {
    endListeners.forEach(l -> l.execute(this, ret));
  }

  private void addChildrenTo(MethodCall call) {
    childrenTree.list().stream()
        .map(e -> child((TestElement) e, childrenTree.getTree(e))
            .buildMethodCall())
        .forEach(call::child);
  }

  /**
   * Allows creating a child context for the given test element and tree.
   *
   * @param element      the test element associated to the child context.
   * @param childrenTree the test element subtree.
   * @return the created child method context.
   * @since 1.3
   */
  public MethodCallContext child(TestElement element, HashTree childrenTree) {
    return new MethodCallContext(element, childrenTree, this, builderRegistry);
  }

  /**
   * Adds a child method call context, as first child call, built using provided test element and
   * tree.
   * <p>
   * This is useful when it is needed to modify existing test plan, for example, to optimize it and
   * use default config elements that avoid code duplication in children elements.
   *
   * @param testElement  specifies the test element from which to create the new method call.
   * @param childrenTree specifies children elements of the test element, which are also going to be
   *                     built and attached as children method calls of the method call created for
   *                     the test element.
   * @return the created child method call context.
   * @since 1.8
   */
  public MethodCallContext prependChild(TestElement testElement, HashTree childrenTree) {
    MethodCallContext child = child(testElement, childrenTree);
    MethodCall childCall = child.buildMethodCall();
    methodCall.prependChild(childCall);
    return child;
  }

  /**
   * Allows removing an instance of the given test element class from the children tree.
   * <p>
   * If multiple instances exists, then only the first one is removed.
   *
   * @param filter specifies condition to be matched by test element to be removed.
   * @return the context associated to the removed test element, or null if no test element is
   * found.
   */
  public MethodCallContext removeChild(Predicate<TestElement> filter) {
    Optional<?> child = childrenTree.list().stream()
        .map(o -> (TestElement) o)
        .filter(filter)
        .findAny();
    child.ifPresent(childrenTree::remove);
    return child
        .map(c -> child((TestElement) c, childrenTree.getTree(c)))
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
    return builderRegistry.findBuilderByClass(builderClass);
  }

  /**
   * The method call created for this context.
   * <p>
   * This is useful mainly when method calls need to be modified after their creation, for example
   * in an end listener ({@link #addEndListener(MethodCallContextEndListener)}).
   *
   * @return the created method call, if it has been already created, null otherwise.
   * @since 1.8
   */
  public MethodCall getMethodCall() {
    return methodCall;
  }

  /**
   * Allows replacing or transforming the method call associated to a given test element.
   * <p>
   * This is particularly helpful in scenarios like module controller, where basic conversion of a
   * controller has to be replaced by a call to a test fragment containing the target controller
   * pointed by the module controller
   *
   * @param element  is the test element associated to the method call to be replaced/transformed.
   * @param operator provides the logic to be applied to create a new method call from the original
   *                 test element method call.
   * @since 1.3
   */
  public void replaceMethodCall(TestElement element, UnaryOperator<MethodCall> operator) {
    MethodCallContext elementContext = root.contextRegistry.get(element);
    if (elementContext != null) {
      MethodCall original = elementContext.methodCall;
      MethodCall replacement = operator.apply(original);
      if (replacement != original) {
        elementContext.parent.methodCall.replaceChild(original, replacement);
      }
    } else {
      root.pendingReplacements.put(element, operator);
    }
  }

  public Object getBuilderOption(String optionName) {
    return root.getEntry(optionName);
  }

  public interface MethodCallContextEndListener {

    void execute(MethodCallContext context, MethodCall call);

  }

}
