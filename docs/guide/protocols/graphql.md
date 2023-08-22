### GraphQL

When you want to test a GraphQL service, having properly set each field in an HTTP request and knowing the exact syntax for each of them, can quickly start becoming tedious. For this purpose, jmeter-java-dsl provides `graphqlSampler`. To use it you need to include this dependency:

:::: code-group
::: code-group-item Maven
```xml
<dependency>
  <groupId>us.abstracta.jmeter</groupId>
  <artifactId>jmeter-java-dsl-graphql</artifactId>
  <version>1.19</version>
  <scope>test</scope>
</dependency>
```
:::
::: code-group-item Gradle
```groovy
testImplementation 'us.abstracta.jmeter:jmeter-java-dsl-graphql:1.19'
```
:::
::::

And then you can make simple GraphQL requests like this:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.graphql.DslGraphqlSampler.*;

import org.junit.jupiter.api.Test;

public class PerformanceTest {

  @Test
  public void test() throws Exception {
    String url = "https://myservice.com";
    testPlan(
        threadGroup(1, 1,
            graphqlSampler(url, "{user(id: 1) {name}}"),
            graphqlSampler(url, "query UserQuery($id: Int) { user(id: $id) {name}}")
                .operationName("UserQuery")
                .variable("id", 2)
        )
    ).run();
  }

}
```

::: tip
GraphQL Sampler is based on HTTP Sampler, so all test elements that affect HTTP Samplers, like `httpHeaders`, `httpCookies`, `httpDefaults`, and JMeter properties, also affect GraphQL sampler.
:::

::: warning
`grapqlSampler` sets by default `application/json` `Content-Type` header.

This has been done to ease the most common use cases and to avoid users the common pitfall of missing the proper `Content-Type` header value.

If you need to modify `graphqlSampler` content type to be other than `application/json`, then you can use `contentType` method, potentially parameterizing it to reuse the same value in multiple samplers like in the following example:

```java
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;
import static us.abstracta.jmeter.javadsl.graphql.DslGraphqlSampler.*;

import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import us.abstracta.jmeter.javadsl.graphql.DslGraphqlSampler;

public class PerformanceTest {

  private DslGraphqlSampler myGraphqlRequest(String query) {
    return graphqlSampler("https://myservice.com", query)
        .contentType(ContentType.create("myContentType"));
  }

  @Test
  public void test() throws Exception {
    testPlan(
        threadGroup(1, 1,
            myGraphqlRequest("{user(id: 1) {name}}"),
            myGraphqlRequest("{user(id: 5) {address}}")
        )
    ).run();
  }

}
```
:::
