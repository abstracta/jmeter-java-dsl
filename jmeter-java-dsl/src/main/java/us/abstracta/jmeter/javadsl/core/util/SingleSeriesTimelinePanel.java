package us.abstracta.jmeter.javadsl.core.util;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.BorderFactory;
import kg.apc.charting.AbstractGraphRow;
import kg.apc.charting.DateTimeRenderer;
import kg.apc.charting.GraphPanelChart;
import kg.apc.charting.rows.GraphRowSimple;

/**
 * Simplifies the creation of {@link GraphPanelChart} which only contain one time series.
 *
 * @since 0.26
 */
public class SingleSeriesTimelinePanel extends GraphPanelChart {

  private final GraphRowSimple series;
  private long curTimeMillis;

  public SingleSeriesTimelinePanel(String seriesName) {
    super(false, true);
    getChartSettings().setDrawFinalZeroingLines(true);
    setxAxisLabel("Time");
    setYAxisLabel(seriesName);
    setxAxisLabelRenderer(new DateTimeRenderer(DateTimeRenderer.HHMMSS, 0));
    setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
    setForcedMinX(0);
    HashMap<String, AbstractGraphRow> model = new HashMap<>();
    series = buildSeries();
    model.put(seriesName, series);
    setRows(model);
  }

  private GraphRowSimple buildSeries() {
    GraphRowSimple ret = new GraphRowSimple();
    ret.setColor(Color.RED);
    ret.setDrawLine(true);
    ret.setMarkerSize(AbstractGraphRow.MARKER_SIZE_NONE);
    return ret;
  }

  public void add(long timeIncrMillis, double val) {
    curTimeMillis += timeIncrMillis;
    series.add(curTimeMillis, val);
  }

}
