package us.abstracta.jmeter.javadsl.octoperf.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TableEntry {

  private final String actionId;
  private final List<TableValue> values;

  @JsonCreator
  public TableEntry(@JsonProperty("actionId") String actionId,
      @JsonProperty("values") List<TableValue> values) {
    this.actionId = actionId;
    this.values = values;
  }

  public String getActionId() {
    return actionId;
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
