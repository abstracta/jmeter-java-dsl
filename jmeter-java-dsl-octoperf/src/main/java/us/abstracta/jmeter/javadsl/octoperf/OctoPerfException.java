package us.abstracta.jmeter.javadsl.octoperf;

import us.abstracta.jmeter.javadsl.engines.RemoteEngineException;

/**
 * Exception thrown when getting an unsuccessful response from OctoPerf API.
 *
 * @since 0.58
 */
// This class has been left to avoid breaking api compatibility
public class OctoPerfException extends RemoteEngineException {

  public OctoPerfException(int code, String errorResponse) {
    super(code, errorResponse);
  }

}
