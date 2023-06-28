package us.abstracta.jmeter.javadsl.datadog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class DatadogApiClient implements Closeable {

  private final DatadogApi api;
  private final OkHttpClient httpClient;

  public DatadogApiClient(String apiKey, String applicationKey) {
    httpClient = new Builder()
        .addInterceptor(chain -> {
          Request request = chain.request()
              .newBuilder()
              .header("DD-API-KEY", apiKey)
              .header("DD-APPLICATION-KEY", applicationKey)
              .build();
          return chain.proceed(request);
        })
        .build();
    api = new Retrofit.Builder()
        .baseUrl("https://api.datadoghq.com/api/")
        .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        ))
        .client(httpClient)
        .build()
        .create(DatadogApi.class);
  }

  private interface DatadogApi {

    @GET("v1/query")
    Call<MetricsDataPoints> queryMetricsDataPoints(@Query("from") long from, @Query("to") long to,
        @Query("query") String query);

  }

  public static class MetricsDataPoints {

    private final List<MetricDataPoints> series;

    @JsonCreator
    public MetricsDataPoints(@JsonProperty("series") List<MetricDataPoints> series) {
      this.series = series;
    }
  }

  public static class MetricDataPoints {

    private final List<Double[]> pointList;

    @JsonCreator
    public MetricDataPoints(@JsonProperty("pointlist") List<Double[]> pointList) {
      this.pointList = pointList;
    }
  }

  public int getResponsesCount(Instant since, Instant until, String tag) throws IOException {
    MetricsDataPoints ret = execApiCall(api.queryMetricsDataPoints(since.getEpochSecond(),
        until.getEpochSecond(), String.format("sum:jmeter.responses_count{%s}.as_count()", tag)));
    return ret.series.stream()
        .mapToInt(s -> s.pointList != null
            ? s.pointList.stream()
            .mapToInt(l -> (int) Math.round(l[1]))
            .sum()
            : 0)
        .sum();
  }

  private <T> T execApiCall(Call<T> call) throws IOException {
    retrofit2.Response<T> response = call.execute();
    if (!response.isSuccessful()) {
      try (ResponseBody errorBody = response.errorBody()) {
        throw new IOException(
            String.format("Problem invoking DataDog API: (%d) %s", response.code(),
                errorBody.string()));
      }
    }
    return response.body();
  }

  @Override
  public void close() {
    httpClient.dispatcher().executorService().shutdown();
    httpClient.connectionPool().evictAll();
  }

}
