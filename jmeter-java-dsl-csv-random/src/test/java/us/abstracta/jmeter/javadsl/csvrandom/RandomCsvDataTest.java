package us.abstracta.jmeter.javadsl.csvrandom;


import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;

import static us.abstracta.jmeter.javadsl.csvrandom.RandomCsvDataSetConfig.csvRandomDataSet;
import static us.abstracta.jmeter.javadsl.JmeterDsl.jsr223Sampler;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;

public class RandomCsvDataTest extends JmeterDslTest {

  @Test
  public void shouldGetDataFromCSVDataSetRandom()
      throws Exception {
    URL res = getClass().getResource("/test_random.csv");
    File file = Paths.get(res.toURI()).toFile();
    String path = file.getAbsolutePath();

    System.out.println(path);
    TestPlanStats stats = testPlan(
        csvRandomDataSet(path).variableNames("test"),
        threadGroup(1, 1,
                jsr223Sampler("if (vars.get('test')!='foo') {SampleResult.setSuccessful(false); return 'FAIL'} else { return 'OK'}")
            )
    ).run();
    assertThat(stats.overall().errorsCount()).isLessThan(1);
  }

}
