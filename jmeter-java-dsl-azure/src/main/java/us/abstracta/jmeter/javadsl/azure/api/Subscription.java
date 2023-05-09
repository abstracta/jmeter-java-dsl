package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Subscription {

  private final String subscriptionId;
  private final String tenantId;

  @JsonCreator
  public Subscription(@JsonProperty("subscriptionId") String id,
      @JsonProperty("tenantId") String tenantId) {
    this.subscriptionId = id;
    this.tenantId = tenantId;
  }

  public String getId() {
    return subscriptionId;
  }

  public String getUrl() {
    return String.format("https://portal.azure.com/#@%s/resource/subscriptions/%s", tenantId,
        subscriptionId);
  }

}
