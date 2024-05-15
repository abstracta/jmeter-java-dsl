package us.abstracta.jmeter.javadsl.core.threadgroups;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import us.abstracta.jmeter.javadsl.core.util.SingleSeriesTimelinePanel;

public class LoadTimeLine {

  private final String name;
  private final String loadUnit;
  private final List<TimePoint> timePoints = new ArrayList<>();

  public LoadTimeLine(String name, String loadUnit) {
    this.name = name;
    this.loadUnit = loadUnit;
  }

  public void add(long timeMillis, double value) {
    timePoints.add(new TimePoint(timeMillis, value));
  }

  public String getName() {
    return name;
  }

  public JComponent buildChart() {
    SingleSeriesTimelinePanel ret = new SingleSeriesTimelinePanel(loadUnit);
    for (TimePoint tp : timePoints) {
      ret.add(tp.timeMillis, tp.value);
    }
    return ret;
  }

  public long getMaxTime() {
    return timePoints.stream()
        .mapToLong(tp -> tp.timeMillis)
        .max()
        .orElse(0L);
  }

  private static class TimePoint {

    private final long timeMillis;
    private final double value;

    private TimePoint(long timeIncrMillis, double value) {
      this.timeMillis = timeIncrMillis;
      this.value = value;
    }

  }

}
