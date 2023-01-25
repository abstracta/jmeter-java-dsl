package us.abstracta.jmeter.javadsl.core;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.listeners.DslVisualizer;

/**
 * Contains information that can be used by elements to share info
 * <p>
 * Eg: adding additional items to test plan when a particular protocol element is added.
 *
 * @since 0.17
 */
public class BuildTreeContext {

  private final BuildTreeContext parent;
  private final Map<String, Object> entries = new HashMap<>();
  private final List<TreeContextEndListener> endListeners = new ArrayList<>();
  private final Map<DslVisualizer, Supplier<Component>> visualizers;
  /*
   check comment on buildTreeFor to understand why this field is not final and not initialized in
   constructor
   */
  private DslTestElement element;

  public BuildTreeContext() {
    this(null, new LinkedHashMap<>());
  }

  private BuildTreeContext(BuildTreeContext parent,
      Map<DslVisualizer, Supplier<Component>> visualizers) {
    this.parent = parent;
    this.visualizers = visualizers;
  }

  public BuildTreeContext getParent() {
    return parent;
  }

  public DslTestElement getTestElement() {
    return element;
  }

  public BuildTreeContext getRoot() {
    return isRoot() ? this : parent.getRoot();
  }

  public boolean isRoot() {
    return parent == null;
  }

  public Object getEntry(String key) {
    return entries.get(key);
  }

  public <T> T getOrCreateEntry(String key, Supplier<T> supplier) {
    return (T) entries.computeIfAbsent(key, k -> supplier.get());
  }

  public void setEntry(String key, Object value) {
    entries.put(key, value);
  }

  public void addEndListener(TreeContextEndListener endListener) {
    endListeners.add(endListener);
  }

  public void addVisualizer(DslVisualizer visualizer, Supplier<Component> guiBuilder) {
    visualizers.put(visualizer, guiBuilder);
  }

  public Map<DslVisualizer, Supplier<Component>> getVisualizers() {
    return visualizers;
  }

  public HashTree buildChild(DslTestElement child, HashTree parentTree) {
    return new BuildTreeContext(this, visualizers).buildTreeFor(child, parentTree);
  }

  /*
  Instead of passing the element as argument to this method we could initialize it in constructor.
  We might implement this change in a future major release, but for the time being is left as is to
  avoid breaking API compatibility.
   */
  public HashTree buildTreeFor(DslTestElement element, HashTree parentTree) {
    this.element = element;
    HashTree ret = element.buildTreeUnder(parentTree, this);
    endListeners.forEach(l -> l.execute(this, ret));
    return ret;
  }

  public interface TreeContextEndListener {
    
    void execute(BuildTreeContext context, HashTree tree);

  }

}
