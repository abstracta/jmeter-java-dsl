package us.abstracta.jmeter.javadsl.engines;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import devcsrj.okhttp3.logging.HttpLoggingInterceptor;
import java.io.Closeable;
import java.io.IOException;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Contains common logic used to interact with a remote engine service.
 *
 * @since 1.10
 */
public abstract class BaseRemoteEngineApiClient implements Closeable {

  private final Logger logger;
  private final OkHttpClient httpClient;

  protected BaseRemoteEngineApiClient(Logger logger) {
    this.logger = logger;
    OkHttpClient.Builder cliBuilder = new OkHttpClient.Builder();
    configureHttpClient(cliBuilder);
    httpClient = cliBuilder.build();
  }

  protected void configureHttpClient(OkHttpClient.Builder builder) {
    builder.addInterceptor(this::authorizationInterceptor);
    if (logger.isDebugEnabled()) {
      builder.addInterceptor(new HttpLoggingInterceptor());
    }
  }

  private Response authorizationInterceptor(Chain chain) throws IOException {
    Request request = chain.request();
    String auth = buildAuthorizationHeaderValue(request);
    if (auth != null) {
      request = request
          .newBuilder()
          .header("Authorization", auth)
          .build();
    }
    return chain.proceed(request);
  }

  /**
   * Builds the value to be contained in Authorization header included in http requests.
   *
   * @param request contains information about the request which may be helpful to define the proper
   *                authorization value to be used.
   * @return the value to be used in Authorization header. null if no Authorization header should be
   * included in the request.
   * @throws IOException if there is some problem contacting remote engine service (e.g.: when
   *                     refreshing a token).
   */
  protected abstract String buildAuthorizationHeaderValue(Request request) throws IOException;

  /**
   * Allows to easily build a retrofit API annotated class instance for the given base URL and API
   * class.
   *
   * @param baseUrl  specifies the base URL to be used for all API methods of the given class.
   * @param apiClass specifies the class which contains all the API annotated methods.
   * @param <T>      specifies the type for the class that contains all the API annotated methods.
   * @return the API annotated class instance for interaction with the remote engine service.
   */
  protected <T> T buildApiFor(String baseUrl, Class<T> apiClass) {
    return new Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(JacksonConverterFactory.create(buildConverterMapper()))
        .client(httpClient)
        .build()
        .create(apiClass);
  }

  /**
   * Configures a Jackson ObjectMapper to be used by retrofit to serialized and deserialize requests
   * and responses for the API.
   * <p>
   * Any particular RemoteEngineApiClient can override this method to specify custom configuration
   * of the ObjectMapper.
   *
   * @return the ObjectMapper to be used by retrofit.
   */
  protected ObjectMapper buildConverterMapper() {
    return new ObjectMapper()
        .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule());
  }

  /**
   * Executes the given retrofit call, throwing an exception if the response code is not a
   * successful one, or returning the call response otherwise.
   *
   * @param call specifies the API method call to execute.
   * @param <T>  specifies the response type to be returned by the method call API invocation.
   * @return the API method call response.
   * @throws IOException if the response status code is not a successful one, or there is some
   *                     problem contacting the remote engine service.
   */
  protected <T> T execApiCall(Call<T> call) throws IOException {
    retrofit2.Response<T> response = call.execute();
    if (!response.isSuccessful()) {
      try (ResponseBody errorBody = response.errorBody()) {
        throw buildRemoteEngineException(response.code(), errorBody.string());
      }
    }
    return response.body();
  }

  /**
   * Builds a {@link RemoteEngineException} for a given status code and message.
   * <p>
   * This method is provided for backwards compatibility purposes, allowing existing remote engine
   * apis to keep throwing their custom exceptions.
   *
   * @param code    the response status code that generated the exception.
   * @param message the response body.
   * @return the exception to be thrown for the given response status code.
   */
  protected RemoteEngineException buildRemoteEngineException(int code, String message) {
    return new RemoteEngineException(code, message);
  }

  @Override
  public void close() {
    httpClient.dispatcher().executorService().shutdown();
    httpClient.connectionPool().evictAll();
  }

}
