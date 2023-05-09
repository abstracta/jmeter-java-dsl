package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceGroup extends AzureResource {

  @JsonIgnore
  private String name;
  private String location;
  @JsonIgnore
  private Subscription subscription;

  @JsonCreator
  public ResourceGroup(@JsonProperty("properties") AzureResourceProperties properties) {
    super(properties);
  }

  public ResourceGroup(String name, Location location, Subscription subscription) {
    this(new AzureResourceProperties(null));
    this.name = name;
    this.location = location.getName();
    this.subscription = subscription;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Subscription getSubscription() {
    return subscription;
  }

  public void setSubscription(Subscription subscription) {
    this.subscription = subscription;
  }

  public String getLocation() {
    return location;
  }

  @JsonIgnore
  public String getUrl() {
    return subscription.getUrl() + "/resourceGroups/" + name;
  }

}
