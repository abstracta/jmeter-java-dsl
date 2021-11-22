package us.abstracta.jmeter.javadsl.core.controllers;

import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

public class DslOnceOnlyControllerTest extends JmeterDslTest {


    @Test
    public void shouldExecuteOnlyOneTimeWhenOnceOnlyControllerInPlan() throws Exception {
        TestPlanStats stats = testPlan(
                threadGroup(1, 10,
                        onceOnlyController(
                                httpSampler(wiremockUri)
                        )
                )
        ).run();
        assertThat(stats.overall().samplesCount()).isEqualTo(1);
    }

}
