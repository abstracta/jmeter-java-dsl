package us.abstracta.jmeter.javadsl.blazemeter;

import us.abstracta.jmeter.javadsl.engines.RemoteEngineException;

/**
 * Exception thrown when getting an unsuccessful response from BlazeMeter API.
 *
 * @since 0.2
 */
// This class has been left to avoid breaking api compatibility
public class BlazeMeterException extends RemoteEngineException {

  public BlazeMeterException(int code, String errorResponse) {
    super(code, errorResponse);
  }

}
