package us.abstracta.jmeter.javadsl.core;

import java.util.HashMap;
import java.util.Map;
import org.apache.jorphan.collections.HashTree;

/**
 * Contains information shared by elements while building the test plan tree.
 * <p>
 * Eg: adding additional items to test plan when a particular protocol element is added.
 */
public class BuildTreeContext {

  private final HashTree root;
  private final Map<String, Object> entries = new HashMap<>();

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

}
