package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileInfo {

  private final String fileName;
  private final String validationStatus;
  private final String validationFailureDetails;

  @JsonCreator
  public FileInfo(@JsonProperty("fileName") String fileName,
      @JsonProperty("validationStatus") String validationStatus,
      @JsonProperty("validationFailureDetails") String validationFailureDetails) {
    this.fileName = fileName;
    this.validationStatus = validationStatus;
    this.validationFailureDetails = validationFailureDetails;
  }

  public String getFileName() {
    return fileName;
  }

  public String getValidationStatus() {
    return validationStatus;
  }

  public String getValidationFailureDetails() {
    return validationFailureDetails;
  }

}
