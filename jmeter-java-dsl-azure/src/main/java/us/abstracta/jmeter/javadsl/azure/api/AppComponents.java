package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AppComponents {

  private final Map<String, AppComponent> components;

  public AppComponents(List<String> resourceIds) {
    this(resourceIds.stream()
        .map(AppComponent::new)
        .collect(Collectors.toMap(c -> c.resourceId, c -> c)));
  }

  @JsonCreator
  public AppComponents(@JsonProperty("components") Map<String, AppComponent> components) {
    this.components = components;
  }

  public boolean updateWith(List<String> monitoredResources) {
    HashMap<String, AppComponent> prevComponents = new HashMap<>(components);
    components.entrySet().stream()
        .filter(e -> !monitoredResources.contains(e.getKey()))
        .forEach(e -> e.setValue(null));
    monitoredResources.stream()
        .filter(s -> !components.containsKey(s))
        .forEach(s -> components.put(s, new AppComponent(s)));
    return !prevComponents.equals(components);
  }

  private static class AppComponent {

    private static final Pattern RESOURCE_ID_PATTERN = Pattern.compile(
        "^/subscriptions/([^/]+)/resourceGroups/([^/]+)/providers/([^/]+)/(.+)$");

    private final String resourceId;
    private final String subscriptionId;
    private final String resourceGroup;
    private final String resourceType;
    private final String resourceName;

    @JsonCreator
    private AppComponent(@JsonProperty("resourceId") String resourceId) {
      this.resourceId = resourceId;
      Matcher matcher = RESOURCE_ID_PATTERN.matcher(resourceId);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(
            "Provided resources id has not expected format. Check that it matches "
                + RESOURCE_ID_PATTERN + " and if it doesn't and is still valid, "
                + "please create an issue in JMeter DSL GitHub repository.");
      }
      int groupNumber = 1;
      this.subscriptionId = matcher.group(groupNumber++);
      this.resourceGroup = matcher.group(groupNumber++);
      String provider = matcher.group(groupNumber++);
      String resourcePath = matcher.group(groupNumber);
      String[] resourceSegments = resourcePath.split("/");
      if (resourceSegments.length == 0 || resourceSegments.length % 2 != 0) {
        throw new IllegalArgumentException(
            "Provided resource id has an invalid resource path. Resource types and names "
                + "must appear in pairs: " + resourceId);
      }
      StringBuilder resourceTypeBuilder = new StringBuilder(provider);
      StringBuilder resourceNameBuilder = new StringBuilder();
      for (int i = 0; i < resourceSegments.length; i += 2) {
        String type = resourceSegments[i];
        String name = resourceSegments[i + 1];
        resourceTypeBuilder.append("/").append(type);
        if (resourceNameBuilder.length() > 0) {
          resourceNameBuilder.append("/");
        }
        resourceNameBuilder.append(name);
      }
      this.resourceType = resourceTypeBuilder.toString();
      this.resourceName = resourceNameBuilder.toString();
    }

  }

}
