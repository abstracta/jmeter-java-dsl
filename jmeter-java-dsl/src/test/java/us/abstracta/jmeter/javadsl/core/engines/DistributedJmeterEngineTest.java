package us.abstracta.jmeter.javadsl.core.engines;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.jmeter.engine.RemoteJMeterEngineImpl;
import org.apache.jmeter.rmi.RmiUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DistributedJmeterEngineTest extends JmeterDslTest {

  @Test
  public void shouldGetExpectedCountWhenRunTestInRemoteEngine() throws Exception {
    String keystoreFileName = "rmi_keystore.jks";
    File keystoreResource = testResource(keystoreFileName).file();
    JmeterEnvironment env = new JmeterEnvironment();
    try (TempFileCopy ignored = new TempFileCopy(keystoreResource, new File(keystoreFileName))) {
      RemoteJMeterEngineImpl.startServer(RmiUtils.getRmiRegistryPort());
      TestPlanStats stats = testPlan(
          threadGroup(1, 1,
              httpSampler(wiremockUri)
          )
      ).runIn(new DistributedJmeterEngine(RmiUtils.getRmiHost().getHostName())
          .localJMeterEnv(env)
          .stopEnginesOnTestEnd());
      assertThat(stats.overall().samplesCount()).isEqualTo(1);
    }
  }

  private static class TempFileCopy implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(TempFileCopy.class);
    private final File tempFile;

    public TempFileCopy(File src, File target) throws IOException {
      if (target.exists()) {
        LOG.warn("File {} already exists. Overwriting it!", target.getPath());
        target.delete();
      }
      tempFile = target;
      Files.copy(src.toPath(), target.toPath());
    }

    @Override
    public void close() {
      tempFile.delete();
    }

  }
}
