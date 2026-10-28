package us.abstracta.jmeter.javadsl.blazemeter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

public class BlazeMeterEngineSettingsTest {

  private final BlazeMeterEngine engine = new BlazeMeterEngine("keyId:keySecret");

  @Test
  public void shouldBuildSecretPropertyExpressionWhenSecret() {
    assertThat(BlazeMeterEngine.secret("mySecret")).isEqualTo("${__P(BZM_SECRET_mySecret)}");
  }

  @Test
  public void shouldEscapeDefaultValueWhenSecretWithDefault() {
    assertThat(BlazeMeterEngine.secret("mySecret", "a,b"))
        .isEqualTo("${__P(BZM_SECRET_mySecret,a\\,b)}");
  }

  @Test
  public void shouldThrowExceptionWhenNetworkLatencyOutOfRange() {
    assertThatThrownBy(() -> engine.networkEmulation(Duration.ofSeconds(6), 1000, 0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldThrowExceptionWhenNetworkBandwidthNotPositive() {
    assertThatThrownBy(() -> engine.networkEmulation(Duration.ofMillis(100), 0, 0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void shouldThrowExceptionWhenNetworkPacketLossOutOfRange() {
    assertThatThrownBy(() -> engine.networkEmulation(Duration.ofMillis(100), 1000, 101))
        .isInstanceOf(IllegalArgumentException.class);
  }

}
