package us.abstracta.jmeter.javadsl.http;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.gui.CacheManagerGui;
import org.apache.jmeter.testelement.TestElement;

/**
 * Allows configuring caching behavior used by HTTP samplers.
 * <p>
 * This element can only be added as child of test plan, and currently allows only to disable HTTP
 * caching which is enabled by default (emulating browser behavior).
 * <p>
 * This element has to be added before any http sampler to be considered, and if you add multiple
 * instances of cache manager to a test plan, only the first one will be considered.
 *
 * @since 0.17
 */
public class DslCacheManager extends AutoEnabledHttpConfigElement {

  public DslCacheManager() {
    super("HTTP Cache Manager", CacheManagerGui.class);
  }

  /**
   * disables HTTP caching for the test plan.
   *
   * @return the DslCacheManager to allow fluent API usage.
   */
  public DslCacheManager disable() {
    enabled = false;
    return this;
  }

  @Override
  protected TestElement buildTestElement() {
    CacheManager ret = new CacheManager();
    ret.setUseExpires(true);
    ret.setClearEachIteration(true);
    return ret;
  }

  public static class CodeBuilder extends AutoEnabledHttpConfigElement.CodeBuilder<CacheManager> {

    public CodeBuilder(List<Method> builderMethods) {
      super(CacheManager.class, builderMethods);
    }

  }

}
