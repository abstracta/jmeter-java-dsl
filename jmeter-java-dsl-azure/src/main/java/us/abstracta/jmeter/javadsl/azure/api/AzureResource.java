package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AzureResource {

  @JsonIgnore
  protected AzureResourceProperties properties;

  protected AzureResource(AzureResourceProperties properties) {
    this.properties = properties;
  }

  @JsonIgnore
  public String getProvisioningState() {
    return properties.provisioningState;
  }

  public void setProvisioningState(String provisioningState) {
    properties.provisioningState = provisioningState;
  }

  @JsonIgnore
  public boolean isPendingProvisioning() {
    String lowerState = properties.provisioningState.toLowerCase();
    return !("succeeded".equals(lowerState) || "failed".equals(lowerState) || "canceled".equals(
        lowerState));
  }

  @JsonIgnore
  public boolean isProvisioned() {
    return "succeeded".equalsIgnoreCase(properties.provisioningState);
  }

  public static class AzureResourceProperties {

    private String provisioningState;

    @JsonCreator
    public AzureResourceProperties(@JsonProperty("provisioningState") String provisioningState) {
      this.provisioningState = provisioningState;
    }

  }

}
