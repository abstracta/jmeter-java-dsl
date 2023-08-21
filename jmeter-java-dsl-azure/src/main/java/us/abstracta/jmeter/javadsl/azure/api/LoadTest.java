package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LoadTest {

  private final String testId;
  private final String displayName;
  private final LoadTestConfiguration loadTestConfiguration;
  @JsonIgnore
  private LoadTestResource testResource;

  @JsonCreator
  public LoadTest(@JsonProperty("testId") String testId,
      @JsonProperty("displayName") String displayName,
      @JsonProperty("loadTestConfiguration") LoadTestConfiguration loadTestConfiguration) {
    this.testId = testId;
    this.displayName = displayName;
    this.loadTestConfiguration = loadTestConfiguration;
  }

  public LoadTest(String displayName, int engineInstances, boolean splitAllCsvs,
      LoadTestResource testResource) {
    this(UUID.randomUUID().toString(), displayName,
        new LoadTestConfiguration(engineInstances, splitAllCsvs));
    this.testResource = testResource;
  }

  public String getTestId() {
    return testId;
  }

  public String getDisplayName() {
    return displayName;
  }

  @JsonIgnore
  public int getEngineInstances() {
    return loadTestConfiguration.engineInstances;
  }

  @JsonIgnore
  public void setEngineInstances(int engineInstances) {
    this.loadTestConfiguration.engineInstances = engineInstances;
  }

  @JsonIgnore
  public boolean isSplitCsvs() {
    return loadTestConfiguration.splitAllCSVs;
  }

  @JsonIgnore
  public void setSplitCsvs(boolean splitCsvs) {
    this.loadTestConfiguration.splitAllCSVs = splitCsvs;
  }

  @JsonIgnore
  public void setTestResource(LoadTestResource testResource) {
    this.testResource = testResource;
  }

  @JsonIgnore
  public String getUrl() {
    try {
      return String.format(
          "https://portal.azure.com/#view/Microsoft_Azure_CloudNativeTesting/TestRun/testId/%s"
              + "/resourceId/%s/openingFromBlade~/true/openingFromTestBlade~/true;", testId,
          URLEncoder.encode(testResource.getId(), StandardCharsets.UTF_8.name()));
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static class LoadTestConfiguration {

    private int engineInstances;
    private boolean splitAllCSVs;

    @JsonCreator
    public LoadTestConfiguration(@JsonProperty("engineInstances") int engineInstances,
        @JsonProperty("splitAllCSVs") boolean splitAllCSVs) {
      this.engineInstances = engineInstances;
      this.splitAllCSVs = splitAllCSVs;
    }

  }

}
