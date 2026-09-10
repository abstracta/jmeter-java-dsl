package us.abstracta.jmeter.javadsl.http2;

import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.ThreadGroupChild;

/**
 * Provides factory methods for HTTP/2 samplers and controllers.
 * <p>
 * This module uses the
 * <a href="https://github.com/Blazemeter/jmeter-http2-plugin">BlazeMeter HTTP plugin</a>.
 * It requires Java 17+ at runtime.
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
 * import static us.abstracta.jmeter.javadsl.http2.Http2JmeterDsl.*;
 * import us.abstracta.jmeter.javadsl.core.TestPlanStats;
 *
 * public class Test {
 *
 *   public static void main(String[] args) throws Exception {
 *     TestPlanStats stats = testPlan(
 *         threadGroup(1, 1,
 *             http2Sampler("https://example.com")
 *                 .http2Only()
 *         )
 *     ).run();
 *   }
 * }
 * }</pre>
 *
 * @since 2.3
 */
public class Http2JmeterDsl {

  private Http2JmeterDsl() {
  }

  /**
   * Builds an HTTP/2 sampler pointing to the given URL.
   * <p>
   * By default, the sampler uses the {@link DslHttp2Sampler.ClientProfile#BROWSER_LIKE}
   * profile which enables HTTP/3, HTTP/2, and HTTP/1.1 with automatic fallback.
   *
   * @param url specifies URL to send HTTP requests to (e.g. {@code https://example.com/path}).
   * @return the sampler for further configuration or usage.
   * @see DslHttp2Sampler
   */
  public static DslHttp2Sampler http2Sampler(String url) {
    return http2Sampler(null, url);
  }

  /**
   * Same as {@link #http2Sampler(String)} but allowing to set a name on the sampler.
   *
   * @param name is the label assigned to the sampler in collected metrics.
   * @param url  specifies URL to send HTTP requests to.
   * @return the sampler for further configuration or usage.
   * @see #http2Sampler(String)
   */
  public static DslHttp2Sampler http2Sampler(String name, String url) {
    return new DslHttp2Sampler(name, url);
  }

  /**
   * Builds an HTTP Async Controller to run HTTP/2 samplers concurrently within a thread
   * iteration.
   *
   * @param children test elements to execute concurrently.
   * @return the controller for further configuration or usage.
   * @see DslHttpAsyncController
   */
  public static DslHttpAsyncController httpAsyncController(ThreadGroupChild... children) {
    return DslHttpAsyncController.httpAsyncController(children);
  }

  /**
   * Same as {@link #httpAsyncController(ThreadGroupChild...)} but allowing to set a name on the
   * controller.
   *
   * @param name     is the label assigned to the controller in collected metrics.
   * @param children test elements to execute concurrently.
   * @return the controller for further configuration or usage.
   */
  public static DslHttpAsyncController httpAsyncController(String name,
      ThreadGroupChild... children) {
    return DslHttpAsyncController.httpAsyncController(name, children);
  }

}
