package us.abstracta.jmeter.javadsl.blazemeter;

import java.io.IOException;

/**
 * Exception thrown when getting an unsuccessful response from BlazeMeter API.
 *
 * @since 0.2
 */
public class BlazeMeterException extends IOException {

  private final int code;
  private final String body;

  public BlazeMeterException(int code, String errorResponse) {
    super("Error response obtained from BlazeMeter: " + errorResponse);
    this.code = code;
    this.body = errorResponse;
  }

  /**
   * @return the status code contained in BlazeMeter API response.
   */
  public int code() {
    return code;
  }

  /**
   * @return message body contained in BlazeMeter API response.
   */
  public String body() {
    return body;
  }

}
