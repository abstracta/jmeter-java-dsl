package us.abstracta.jmeter.javadsl.octoperf.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TableEntry {

  private final String actionPath;
  private final List<TableValue> values;

  @JsonCreator
  public TableEntry(@JsonProperty("actionPath") String actionPath,
      @JsonProperty("values") List<TableValue> values) {
    this.actionPath = actionPath;
    this.values = values;
  }

  public String getActionPath() {
    return actionPath;
  }

  public List<TableValue> getValues() {
    return values;
  }

  public static class TableValue {

    private final double value;

    @JsonCreator
    public TableValue(@JsonProperty("value") double value) {
      this.value = value;
    }

    public double getValue() {
      return value;
    }

  }

}
