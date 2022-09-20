package us.abstracta.jmeter.javadsl.octoperf.api;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.jmeter.threads.ThreadGroup;
import us.abstracta.jmeter.javadsl.core.threadgroups.BaseThreadGroup.SampleErrorAction;

public class UserLoad {

  // we don't need getters since Jackson gets the values from fields
  private final String name = "";
  private final String virtualUserId;
  private final String providerId;
  private final String region;
  private final UserLoadStrategy strategy;
  private final BandwidthSettings bandwidth = new BandwidthSettings();
  private final BrowserSettings browser = new BrowserSettings();
  private final DnsSettings dns = new DnsSettings();
  private final ThinkTimeSettings thinktime = new ThinkTimeSettings();
  private final MemorySettings memory = new MemorySettings();
  private final JtlSettings jtl = new JtlSettings();
  private final PropertiesSettings properties = new PropertiesSettings();
  private final SetUpTearDownSettings setUp = null;
  private final SetUpTearDownSettings tearDown = null;

  public UserLoad() {
    virtualUserId = null;
    providerId = null;
    region = null;
    strategy = null;
  }

  public UserLoad(String virtualUserId, String providerId, String region,
      UserLoadStrategy strategy) {
    this.virtualUserId = virtualUserId;
    this.providerId = providerId;
    this.region = region;
    this.strategy = strategy;
  }

  @JsonTypeInfo(use = NAME, include = PROPERTY)
  @JsonSubTypes({
      @JsonSubTypes.Type(UserLoadRampUp.class)
  })
  public abstract static class UserLoadStrategy {

  }

  @JsonTypeName("UserLoadRampup")
  public static class UserLoadRampUp extends UserLoadStrategy {

    private final int userload;
    private final long rampup;
    private final long peak;
    private final long delay = 0;
    private final SampleErrorAction onSampleError = SampleErrorAction.CONTINUE;

    public UserLoadRampUp() {
      userload = 0;
      rampup = 0;
      peak = 0;
    }

    public UserLoadRampUp(int userLoad, long rampUpMillis, long peakMillis) {
      this.userload = userLoad;
      this.rampup = rampUpMillis;
      this.peak = peakMillis;
    }

    public static UserLoadRampUp fromThreadGroup(ThreadGroup threadGroup) {
      if (threadGroup == null) {
        return new UserLoadRampUp(1, 0, 10000);
      }
      return new UserLoadRampUp(threadGroup.getNumThreads(),
          threadGroup.getRampUp() * 1000L,
          threadGroup.getDuration() != 0 ? threadGroup.getDuration() * 1000L : 10000);
    }

  }

  public static class SetUpTearDownSettings {

  }

  public static class BandwidthSettings {

    private final String name = "UNLIMITED";
    private final int bitsPerSecond = 0;

  }

  public static class BrowserSettings {

    private final String name = "AS_RECORDED";
    private final String userAgent = "";
    private final CacheManager cache = new CacheManager();
    private final CookiesManager cookies = new CookiesManager();

  }

  public static class CacheManager {

    private final int cacheSize = 5000;
    private final boolean clearCacheOnEachIteration = true;
    private final boolean useCacheControlHeaders = false;

  }

  public static class CookiesManager {

    private final boolean clearOnEachIteration = true;
    private final String policy = "STANDARD";

  }

  public static class DnsSettings {

    private final boolean clearEachIteration = false;
    private final List<String> servers = Collections.emptyList();
    private final Map<String, String> staticHosts = Collections.emptyMap();

  }

  public static class ThinkTimeSettings {

    private final ThinkTime thinktime = null;
    private final PacingSettings pacing = null;

  }

  public static class ThinkTime {

  }

  public static class PacingSettings {

  }

  public static class MemorySettings {

    private final Double vuMb = null;

  }

  public static class JtlSettings {

    private final String type = "ALL";
    private final List<String> settings = Collections.emptyList();

  }

  public static class PropertiesSettings {

    private final Map<String, String> map = Collections.emptyMap();

  }

}
