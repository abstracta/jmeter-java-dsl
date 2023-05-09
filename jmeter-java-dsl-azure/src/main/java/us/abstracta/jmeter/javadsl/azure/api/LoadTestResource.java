package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoadTestResource extends AzureResource {

  @JsonIgnore
  private String id;
  @JsonIgnore
  private final String name;
  @JsonIgnore
  private ResourceGroup resourceGroup;
  private String location;

  @JsonCreator
  public LoadTestResource(@JsonProperty("id") String id, @JsonProperty("name") String name,
      @JsonProperty("properties") LoadTestResourceProperties properties) {
    super(properties);
    this.id = id;
    this.name = name;
  }

  public LoadTestResource(String name, ResourceGroup resourceGroup) {
    super(new LoadTestResourceProperties(null, null));
    this.name = name;
    this.resourceGroup = resourceGroup;
    this.location = resourceGroup.getLocation();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public ResourceGroup getResourceGroup() {
    return resourceGroup;
  }

  public void setResourceGroup(ResourceGroup resourceGroup) {
    this.resourceGroup = resourceGroup;
  }

  @JsonIgnore
  public String getDataPlaneUri() {
    return ((LoadTestResourceProperties) properties).dataPlaneUri;
  }

  @JsonIgnore
  public String getUrl() {
    return resourceGroup.getUrl() + "/providers/Microsoft.LoadTestService/loadtests/" + name;
  }

  public static class LoadTestResourceProperties extends AzureResourceProperties {

    private final String dataPlaneUri;

    @JsonCreator
    public LoadTestResourceProperties(@JsonProperty("provisioningState") String provisioningState,
        @JsonProperty("dataPlaneURI") String dataPlaneUri) {
      super(provisioningState);
      this.dataPlaneUri = dataPlaneUri;
    }

  }

}
