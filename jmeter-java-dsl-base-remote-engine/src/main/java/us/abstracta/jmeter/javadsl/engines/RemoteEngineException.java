package us.abstracta.jmeter.javadsl.engines;

import java.io.IOException;

/**
 * Is thrown when there is some problem contacting a remote engine service.
 *
 * @since 1.10
 */
public class RemoteEngineException extends IOException {

  private final int code;
  private final String body;

  public RemoteEngineException(int code, String errorResponse) {
    super("Error response obtained remote engine: " + errorResponse);
    this.code = code;
    this.body = errorResponse;
  }

  /**
   * @return the status code contained in remote engine API response.
   */
  public int code() {
    return code;
  }

  /**
   * @return message body contained in remote engine API response.
   */
  public String body() {
    return body;
  }

}
