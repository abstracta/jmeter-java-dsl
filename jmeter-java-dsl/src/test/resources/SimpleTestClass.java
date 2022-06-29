import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import java.io.IOException;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class PerformanceTest {

  @Test
  public void test() throws IOException {
    TestPlanStats stats = testPlan(
        threadGroup(1, 3,
          httpSampler("http://localhost")
            .post("{\"var\":\"val\"}", ContentType.APPLICATION_JSON),
          httpSampler("http://localhost")
        ),
        jtlWriter("results.jtl")
      ).run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

}