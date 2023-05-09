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
  private final LoadTestInputArtifacts inputArtifacts;
  @JsonIgnore
  private LoadTestResource testResource;

  @JsonCreator
  public LoadTest(@JsonProperty("testId") String testId,
      @JsonProperty("displayName") String displayName,
      @JsonProperty("loadTestConfiguration") LoadTestConfiguration loadTestConfiguration,
      @JsonProperty("inputArtifacts") LoadTestInputArtifacts inputArtifacts) {
    this.testId = testId;
    this.displayName = displayName;
    this.loadTestConfiguration = loadTestConfiguration;
    this.inputArtifacts = inputArtifacts;
  }

  public LoadTest(String displayName, int engineInstances, LoadTestResource testResource) {
    this(UUID.randomUUID().toString(), displayName, new LoadTestConfiguration(engineInstances),
        new LoadTestInputArtifacts(null));
    this.testResource = testResource;
  }

  public String getTestId() {
    return testId;
  }

  @JsonIgnore
  public String getTestScriptFileName() {
    return inputArtifacts.testScriptFileInfo != null ? inputArtifacts.testScriptFileInfo.fileName
        : null;
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

  public void removeTestScriptFile() {
    inputArtifacts.testScriptFileInfo = null;
  }

  @JsonIgnore
  public boolean isPendingValidation() {
    String validationStatus = getValidationStatus();
    return "VALIDATION_INITIATED".equals(validationStatus) || "NOT_VALIDATED".equals(
        validationStatus);
  }

  @JsonIgnore
  private String getValidationStatus() {
    return inputArtifacts.testScriptFileInfo == null ? "VALIDATION_INITIATED"
        : inputArtifacts.testScriptFileInfo.validationStatus;
  }

  @JsonIgnore
  public boolean isSuccessValidation() {
    String validationStatus = getValidationStatus();
    return validationStatus == null || "VALIDATION_SUCCESS".equals(validationStatus);
  }

  public static class LoadTestConfiguration {

    private final int engineInstances;

    @JsonCreator
    public LoadTestConfiguration(@JsonProperty("engineInstances") int engineInstances) {
      this.engineInstances = engineInstances;
    }

  }

  public static class LoadTestInputArtifacts {

    private FileInfo testScriptFileInfo;

    @JsonCreator
    public LoadTestInputArtifacts(@JsonProperty("testScriptUrl") FileInfo testScriptFileInfo) {
      this.testScriptFileInfo = testScriptFileInfo;
    }

  }

  public static class FileInfo {

    private final String fileName;
    private final String validationStatus;

    @JsonCreator
    public FileInfo(@JsonProperty("fileName") String fileName,
        @JsonProperty("validationStatus") String validationStatus) {
      this.fileName = fileName;
      this.validationStatus = validationStatus;
    }

  }

}
