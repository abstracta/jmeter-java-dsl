package us.abstracta.jmeter.javadsl.csvrandom;


import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.csvrandom.RandomCsvDataSetConfig.csvRandomDataSet;

public class RandomCsvDataTest {

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
                        jsr223Sampler("SampleResult.successful = (vars['test'] == 'foo')")
                )
        ).run();
        assertThat(stats.overall().errorsCount()).isLessThan(1);
    }

}
