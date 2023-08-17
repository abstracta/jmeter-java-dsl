package us.abstracta.jmeter.javadsl.core.listeners.autostop;

import java.util.function.Function;
import org.apache.jmeter.samplers.SampleResult;

public enum AutoStopMetric {
  SAMPLE_TIME("sample time", SampleResult::getTime),
  LATENCY("latency", SampleResult::getLatency),
  CONNECT_TIME("connect time", SampleResult::getConnectTime),
  SAMPLES("samples", res -> 1L),
  ERRORS("errors", res -> res.isSuccessful() ? 0L : 1L),
  SENT_BYTES("sent bytes", SampleResult::getSentBytes),
  RECEIVED_BYTES("received bytes", SampleResult::getBytesAsLong);

  private final String name;
  private final Function<SampleResult, Long> extractor;

  AutoStopMetric(String name, Function<SampleResult, Long> extractor) {
    this.name = name;
    this.extractor = extractor;
  }

  public long extractFrom(SampleResult result) {
    return extractor.apply(result);
  }

  public String getName() {
    return name;
  }

}
