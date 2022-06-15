package us.abstracta.jmeter.javadsl.octoperf;

import java.io.IOException;

/**
 * Exception thrown when getting an unsuccessful response from OctoPerf API.
 *
 * @since 0.58
 */
public class OctoPerfException extends IOException {

  private final int code;
  private final String body;

  public OctoPerfException(int code, String errorResponse) {
    super("Error response obtained from OctoPerf: " + errorResponse);
    this.code = code;
    this.body = errorResponse;
  }

  /**
   * @return the status code contained in OctoPerf API response.
   */
  public int code() {
    return code;
  }

  /**
   * @return message body contained in OctoPerf API response.
   */
  public String body() {
    return body;
  }

}
