package us.abstracta.jmeter.javadsl.core;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.jorphan.collections.HashTree;
import us.abstracta.jmeter.javadsl.core.engines.TestStopper;
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
  private final Map<String, File> assetFiles;
  /*
   check comment on buildTreeFor to understand why this field is not final and not initialized in
   constructor
   */
  private DslTestElement element;
  private TestStopper testStopper;

  public BuildTreeContext() {
    this(null, new LinkedHashMap<>(), null);
  }

  private BuildTreeContext(BuildTreeContext parent,
      Map<DslVisualizer, Supplier<Component>> visualizers,
      Map<String, File> assetFiles) {
    this.parent = parent;
    this.visualizers = visualizers;
    this.assetFiles = assetFiles;
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

  public void setTestStopper(TestStopper testStopper) {
    this.testStopper = testStopper;
  }

  public TestStopper getTestStopper() {
    return getRoot().testStopper;
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
    return new BuildTreeContext(this, visualizers, assetFiles)
        .buildTreeFor(child, parentTree);
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

  public static BuildTreeContext buildRemoteExecutionContext() {
    return new BuildTreeContext(null, new LinkedHashMap<>(), new LinkedHashMap<>());
  }

  public String processAssetFile(String assetPath) {
    if (assetFiles == null) {
      return assetPath;
    } else {
      File asset = new File(assetPath);
      String fileName = asset.getName();
      int index = 1;
      while (assetFiles.containsKey(fileName) && !asset.equals(assetFiles.get(fileName))) {
        fileName = (index++) + "-" + asset.getName();
      }
      assetFiles.put(fileName, asset);
      return fileName;
    }
  }

  public Map<String, File> getAssetFiles() {
    return assetFiles;
  }

  public interface TreeContextEndListener {

    void execute(BuildTreeContext context, HashTree tree);

  }

}
