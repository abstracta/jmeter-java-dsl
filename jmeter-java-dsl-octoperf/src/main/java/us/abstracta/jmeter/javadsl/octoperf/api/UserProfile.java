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

public class UserProfile {

  // we don't need getters since Jackson gets the values from fields
  private final String name = "";
  private final String virtualUserId;
  private final String providerId;
  private final String location;
  private final UserLoadStrategy load;
  private final MemorySettings memory = new MemorySettings();
  private final Engine engine = new Engine();

  public UserProfile() {
    virtualUserId = null;
    providerId = null;
    location = null;
    load = null;
  }

  public UserProfile(String virtualUserId, String providerId, String location,
      UserLoadStrategy strategy) {
    this.virtualUserId = virtualUserId;
    this.providerId = providerId;
    this.location = location;
    this.load = strategy;
  }

  @JsonTypeInfo(use = NAME, include = PROPERTY)
  @JsonSubTypes({
      @JsonSubTypes.Type(UserLoadRampUp.class)
  })
  public abstract static class UserLoadStrategy {

  }

  @JsonTypeName("UserProfileLoadRampUp")
  public static class UserLoadRampUp extends UserLoadStrategy {

    private final int plateauVus;
    private final long rampUpMs;
    private final long plateauMs;
    private final long delayMs = 0;

    public UserLoadRampUp() {
      plateauVus = 0;
      rampUpMs = 0;
      plateauMs = 0;
    }

    public UserLoadRampUp(int userLoad, long rampUpMillis, long peakMillis) {
      this.plateauVus = userLoad;
      this.rampUpMs = rampUpMillis;
      this.plateauMs = peakMillis;
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

  @JsonTypeInfo(use = NAME, include = PROPERTY)
  @JsonTypeName("JmeterUserProfileEngine")
  public static class Engine {

    private final EngineSettings settings = new EngineSettings();
    private final BrowserSettings browser = new BrowserSettings();
    private final BandwidthSettings bandwidth = new BandwidthSettings();
    private final DnsSettings dns = new DnsSettings();
    private final JtlSettings jtl = new JtlSettings();
    private final PropertiesSettings properties = new PropertiesSettings();

  }

  public static class EngineSettings {

    private final ExternalLiveReportingSettings externalLiveReporting = 
          new ExternalLiveReportingSettings();
    private final SampleErrorAction errorHandling = SampleErrorAction.CONTINUE;
    private final ThinkTimeSettings thinkTime = new ThinkTimeSettings();
    private final SetUpTearDownSettings setUp = null;
    private final SetUpTearDownSettings tearDown = null;

  }

  @JsonTypeInfo(use = NAME, include = PROPERTY)
  @JsonTypeName("JmeterExternalLiveReportingSettings")
  public static class ExternalLiveReportingSettings {

    private final List<String> listeners = Collections.emptyList();
    private final int queueSize = 5000;

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
    private final Boolean downloadResources = null;
    private final Boolean keepAlive = null;

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
