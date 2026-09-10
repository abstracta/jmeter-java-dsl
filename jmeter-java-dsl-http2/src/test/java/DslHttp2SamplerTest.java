import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.http2.Http2JmeterDsl.http2Sampler;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

public class DslHttp2SamplerTest extends JmeterDslTest {

  @Test
  public void shouldSendHttpRequestWhenHttp2Sampler() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            http2Sampler(wiremockUri)
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(1);
    assertThat(stats.overall().errorsCount()).isZero();
  }

  @Test
  public void shouldSendPostWhenHttp2SamplerWithPost() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            http2Sampler(wiremockUri)
                .post(JSON_BODY, ContentType.APPLICATION_JSON)
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(1);
    assertThat(stats.overall().errorsCount()).isZero();
  }

  @Test
  public void shouldUseHttp2OnlyProfileWhenHttp2Only() throws Exception {
    TestPlanStats stats = testPlan(
        threadGroup(1, 1,
            http2Sampler(wiremockUri)
                .http2Only()
        )
    ).run();
    assertThat(stats.overall().samplesCount()).isEqualTo(1);
    assertThat(stats.overall().errorsCount()).isZero();
  }

}
