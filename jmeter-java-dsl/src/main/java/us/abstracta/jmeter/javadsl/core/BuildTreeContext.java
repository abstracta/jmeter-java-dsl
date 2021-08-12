package us.abstracta.jmeter.javadsl.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains information shared by elements while building the test plan tree.
 * <p>
 * Eg: adding additional items to test plan when a particular protocol element is added.
 *
 * @since 0.17
 */
public class BuildTreeContext {

  private static final Logger LOG = LoggerFactory.getLogger(BuildTreeContext.class);

  private final HashTree root;
  private final Map<String, Object> entries = new HashMap<>();
  private final List<Future<Void>> visualizersClose = new ArrayList<>();

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

  public void addVisualizerCloseFuture(Future<Void> future) {
    visualizersClose.add(future);
  }

  public void awaitAllVisualizersClosed() {
    try {
      for (Future<Void> visualizerClose : visualizersClose) {
        try {
          visualizerClose.get();
        } catch (ExecutionException e) {
          LOG.warn("Problem waiting for a visualizer to close", e);
        }
      }
    } catch (InterruptedException e) {
      //just stop waiting for visualizers and reset interrupted flag
      Thread.interrupted();
    }
  }

}
