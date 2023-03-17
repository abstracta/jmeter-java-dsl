package us.abstracta.jmeter.javadsl.elasticsearch.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.httpSampler;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testPlan;
import static us.abstracta.jmeter.javadsl.JmeterDsl.testResource;
import static us.abstracta.jmeter.javadsl.JmeterDsl.threadGroup;
import static us.abstracta.jmeter.javadsl.elasticsearch.listener.ElasticsearchBackendListener.elasticsearchListener;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.HttpHost;
import org.elasticsearch.action.TimestampParsingException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import us.abstracta.jmeter.javadsl.JmeterDslTest;
import us.abstracta.jmeter.javadsl.codegeneration.MethodCallBuilderTest;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;

public class ElasticsearchBackendListenerTest extends JmeterDslTest {

  private static final String INDEX_NAME = "jmeter";

  @Test
  public void shouldSendMetricsToElasticsearchWhenElasticsearchListenerInPlan() throws Exception {
    try (ElasticsearchContainer container = new ElasticsearchContainer(
        "docker.elastic.co/elasticsearch/elasticsearch:7.14.0")
        .withEnv("discovery.type", "single-node")) {
      container.start();
      String hostAddress = container.getHttpHostAddress();
      try (RestHighLevelClient client = buildElasticsearchClient(hostAddress)) {
        createIndex(client);

        testPlan(
            threadGroup(1, TEST_ITERATIONS,
                httpSampler(SAMPLE_1_LABEL, wiremockUri),
                httpSampler(SAMPLE_2_LABEL, wiremockUri)
            ),
            elasticsearchListener("http://" + hostAddress + "/" + INDEX_NAME)
        ).run();

        assertThat(findRecordedMetrics(client))
            .isEqualTo(buildExpectedTotalCounts());
      }
    }
  }

  private RestHighLevelClient buildElasticsearchClient(String hostAddress) {
    return new RestHighLevelClient(RestClient.builder(HttpHost.create(hostAddress)));
  }

  private void createIndex(RestHighLevelClient client) throws IOException {
    client.indices().putTemplate(
        new PutIndexTemplateRequest("jmeter")
            .source(testResource("index-template.json").rawContents(), XContentType.JSON),
        RequestOptions.DEFAULT);
  }

  private Map<String, Long> findRecordedMetrics(RestHighLevelClient client)
      throws IOException, InterruptedException {
    String aggregationName = "by_label";
    SearchRequest search = new SearchRequest(INDEX_NAME);
    search.source().aggregation(AggregationBuilders.terms(aggregationName).field("SampleLabel"));
    SearchResponse response = searchUntilResultsCountIs(TEST_ITERATIONS * 2, search, client);
    return buildMetricsFromAggregation(response.getAggregations().get(aggregationName));
  }

  // this is required due to eventual consistency of elasticsearch search
  private SearchResponse searchUntilResultsCountIs(int resultsCount, SearchRequest search,
      RestHighLevelClient client) throws IOException, InterruptedException {
    Instant start = Instant.now();
    SearchResponse response = client.search(search, RequestOptions.DEFAULT);
    long currentCount = response.getHits().getHits().length;
    while (resultsCount != currentCount
        && Duration.between(start, Instant.now()).compareTo(Duration.ofSeconds(30)) < 0) {
      Thread.sleep(1000);
      response = client.search(search, RequestOptions.DEFAULT);
      currentCount = response.getHits().getHits().length;
    }
    if (resultsCount == currentCount) {
      return response;
    } else {
      throw new TimestampParsingException(
          "Timeout while waiting for sample results to appear in search");
    }
  }

  private HashMap<String, Long> buildMetricsFromAggregation(Terms aggregation) {
    List<? extends Bucket> buckets = aggregation.getBuckets();
    HashMap<String, Long> ret = new HashMap<>(buckets.stream()
        .collect(Collectors.toMap(MultiBucketsAggregation.Bucket::getKeyAsString,
            MultiBucketsAggregation.Bucket::getDocCount)));
    ret.put(OVERALL_STATS_LABEL,
        buckets.stream().mapToLong(MultiBucketsAggregation.Bucket::getDocCount).sum());
    return ret;
  }

  @Nested
  public class CodeBuilderTest extends MethodCallBuilderTest {

    public CodeBuilderTest() {
      codeGenerator.addBuildersFrom(ElasticsearchBackendListener.class);
    }

    public DslTestPlan testPlanWithElasticSearchListener() {
      return testPlan(
          threadGroup(1, 1,
              httpSampler("http://localhost"),
              elasticsearchListener("http://localhost/jmeter")
                  .credentials("user", "pass")
                  .queueSize(5)
          )
      );
    }

  }

}
