package us.abstracta.jmeter.javadsl.azure.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FileInfo {

  private final String fileName;
  private final String validationStatus;

  @JsonCreator
  public FileInfo(@JsonProperty("fileName") String fileName,
      @JsonProperty("validationStatus") String validationStatus) {
    this.fileName = fileName;
    this.validationStatus = validationStatus;
  }

  public String getFileName() {
    return fileName;
  }

  public String getValidationStatus() {
    return validationStatus;
  }

}
