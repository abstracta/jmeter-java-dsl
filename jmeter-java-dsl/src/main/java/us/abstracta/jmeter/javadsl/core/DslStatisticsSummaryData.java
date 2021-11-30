package us.abstracta.jmeter.javadsl.core;

import org.apache.jmeter.report.processor.MeanAggregator;
import org.apache.jmeter.report.processor.PercentileAggregator;
import org.apache.jmeter.report.processor.StatisticsSummaryData;

public class DslStatisticsSummaryData extends StatisticsSummaryData {

	private final PercentileAggregator latencyPercentile1;
	private final PercentileAggregator latencyPercentile2;
	private final PercentileAggregator latencyPercentile3;
	private final MeanAggregator latencyMean;
	private final PercentileAggregator latencyMedian;
	private final PercentileAggregator processingPercentile1;
	private final PercentileAggregator processingPercentile2;
	private final PercentileAggregator processingPercentile3;
	private final MeanAggregator processingMean;
	private final PercentileAggregator processingMedian;
	private long latencyMin = 9223372036854775807L;
	private long latencyMax = -9223372036854775808L;
	private long processingMin = 9223372036854775807L;
	private long processingMax = -9223372036854775808L;
	private long latency = 0L;
	private long processing = 0L;

	public DslStatisticsSummaryData(double percentileIndex1, double percentileIndex2,
	                                double percentileIndex3) {
		super(percentileIndex1, percentileIndex2, percentileIndex3);
		this.latencyPercentile1 = new PercentileAggregator(percentileIndex1);
		this.latencyPercentile2 = new PercentileAggregator(percentileIndex2);
		this.latencyPercentile3 = new PercentileAggregator(percentileIndex3);
		this.latencyMean = new MeanAggregator();
		this.latencyMedian = new PercentileAggregator(50.0D);
		this.processingPercentile1 = new PercentileAggregator(percentileIndex1);
		this.processingPercentile2 = new PercentileAggregator(percentileIndex2);
		this.processingPercentile3 = new PercentileAggregator(percentileIndex3);
		this.processingMean = new MeanAggregator();
		this.processingMedian = new PercentileAggregator(50.0D);
	}

	public final PercentileAggregator getLatencyPercentile1() {
		return this.latencyPercentile1;
	}

	public final PercentileAggregator getLatencyPercentile2() {
		return this.latencyPercentile2;
	}

	public final PercentileAggregator getLatencyPercentile3() {
		return this.latencyPercentile3;
	}

	public MeanAggregator getLatencyMean() {
		return this.latencyMean;
	}

	public PercentileAggregator getLatencyMedian() {
		return this.latencyMedian;
	}

	public final PercentileAggregator getProcessingPercentile1() {
		return this.processingPercentile1;
	}

	public final PercentileAggregator getProcessingPercentile2() {
		return this.processingPercentile2;
	}

	public final PercentileAggregator getProcessingPercentile3() {
		return this.processingPercentile3;
	}

	public MeanAggregator getProcessingMean() {
		return this.processingMean;
	}

	public PercentileAggregator getProcessingMedian() {
		return this.processingMedian;
	}

	public final long getLatencyMin() {
		return this.latencyMin;
	}

	public final void setLatencyMin(long min) {
		this.latencyMin = Math.min(this.latencyMin, min);
	}

	public final long getLatencyMax() {
		return this.latencyMax;
	}

	public final void setLatencyMax(long max) {
		this.latencyMax = Math.max(this.latencyMax, max);
	}

	public final long getProcessingMin() {
		return this.processingMin;
	}

	public final void setProcessingMin(long min) {
		this.processingMin = Math.min(this.processingMin, min);
	}

	public final long getProcessingMax() {
		return this.processingMax;
	}

	public final void setProcessingMax(long max) {
		this.processingMax = Math.max(this.processingMax, max);
	}

	public final void setLatency(long latency) {
		this.latency = latency;
	}

	public final long getLatencyTime() {
		return this.latency;
	}

	public final void setProcessing(long processing) {
		this.processing = processing;
	}

	public final long getProcessingTime() {
		return this.processing;
	}

}
