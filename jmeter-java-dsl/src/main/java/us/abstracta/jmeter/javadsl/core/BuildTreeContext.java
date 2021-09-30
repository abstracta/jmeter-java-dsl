package us.abstracta.jmeter.javadsl.core;

import java.awt.Component;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.listeners.DslVisualizer;

/**
 * Contains information shared by elements while building the test plan tree.
 * <p>
 * Eg: adding additional items to test plan when a particular protocol element is added.
 *
 * @since 0.17
 */
public class BuildTreeContext {

  private final HashTree root;
  private final Map<String, Object> entries = new HashMap<>();
  private final Map<DslVisualizer, Supplier<Component>> visualizers = new LinkedHashMap<>();

  public BuildTreeContext(HashTree root) {
    this.root = root;
  }

  public HashTree getTestPlanTree() {
    return root.values().iterator().next();
  }

  public Object getEntry(String key) {
    return entries.get(key);
  }

  public void setEntry(String key, Object value) {
    entries.put(key, value);
  }

  public void addVisualizer(DslVisualizer visualizer, Supplier<Component> guiBuilder) {
    visualizers.put(visualizer, guiBuilder);
  }

  public Map<DslVisualizer, Supplier<Component>> getVisualizers() {
    return visualizers;
  }

}
