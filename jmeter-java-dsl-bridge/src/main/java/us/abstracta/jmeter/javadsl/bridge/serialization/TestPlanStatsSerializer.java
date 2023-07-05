package us.abstracta.jmeter.javadsl.bridge.serialization;

import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.stats.CountMetricSummary;
import us.abstracta.jmeter.javadsl.core.stats.StatsSummary;
import us.abstracta.jmeter.javadsl.core.stats.TimeMetricSummary;

public class TestPlanStatsSerializer {

  public void serializeToWriter(TestPlanStats stats, Writer writer) {
    DumperOptions opts = new DumperOptions();
    opts.setDefaultFlowStyle(FlowStyle.BLOCK);
    new Yaml(opts).dump(stats2Map(stats), writer);
  }

  private Map<String, Object> stats2Map(TestPlanStats stats) {
    Map<String, Object> ret = new HashMap<>();
    ret.put("duration", duration2String(stats.duration()));
    ret.put("overall", statsSummary2Map(stats.overall()));
    ret.put("labels", stats.labels().stream()
        .collect(
            Collectors.toMap(l -> l, l -> statsSummary2Map(stats.byLabel(l)), (u, v) -> u,
                LinkedHashMap::new)));
    return ret;
  }

  private String duration2String(Duration duration) {
    return duration.toString();
  }

  private Map<String, Object> statsSummary2Map(StatsSummary stats) {
    Map<String, Object> ret = new HashMap<>();
    ret.put("firstTime", instant2String(stats.firstTime()));
    ret.put("endTime", instant2String(stats.endTime()));
    ret.put("samples", countMetric2Map(stats.samples()));
    ret.put("errors", countMetric2Map(stats.errors()));
    ret.put("sampleTime", timeMetric2Map(stats.sampleTime()));
    ret.put("receivedBytes", countMetric2Map(stats.receivedBytes()));
    ret.put("sentBytes", countMetric2Map(stats.sentBytes()));
    return ret;
  }

  private String instant2String(Instant instant) {
    return instant.toString();
  }

  private Map<String, Object> countMetric2Map(CountMetricSummary metric) {
    Map<String, Object> ret = new HashMap<>();
    ret.put("total", metric.total());
    ret.put("perSecond", metric.perSecond());
    return ret;
  }

  private Map<String, Object> timeMetric2Map(TimeMetricSummary metric) {
    Map<String, Object> ret = new HashMap<>();
    ret.put("max", duration2String(metric.max()));
    ret.put("min", duration2String(metric.min()));
    ret.put("mean", duration2String(metric.mean()));
    ret.put("median", duration2String(metric.median()));
    ret.put("perc90", duration2String(metric.perc90()));
    ret.put("perc95", duration2String(metric.perc95()));
    ret.put("perc99", duration2String(metric.perc99()));
    return ret;
  }

}
