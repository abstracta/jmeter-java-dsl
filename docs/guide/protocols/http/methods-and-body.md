#### Methods & body

As previously seen, you can do simple gets and posts like in the following snippet:

```java
httpSampler("http://my.service") // A simple get
httpSampler("http://my.service")
    .post("{\"field\":\"val\"}", Type.APPLICATION_JSON) // simple post
```

But you can also use additional methods to specify any HTTP method and body:

```java
httpSampler("http://my.service")
  .method(HTTPConstants.PUT)
  .contentType(Type.APPLICATION_JSON)
  .body("{\"field\":\"val\"}")
```

Additionally, when in need to generate dynamic URLs or bodies, you can use lambda expressions (as previously seen in some examples):

```java
httpSampler("http://my.service")
  .post(s -> buildRequestBody(s.vars), Type.TEXT_PLAIN)
httpSampler("http://my.service")
  .body(s -> buildRequestBody(s.vars))
httpSampler(s -> buildRequestUrl(s.vars)) // buildRequestUrl is just an example of a custom method you could implement with your own logic
```

::: warning
As previously mentioned for other lambdas, using them will only work with the embedded JMeter engine. So, prefer using [JSR223 pre-processors](../../request-generation/jsr223-pre-processor#provide-request-parameters-programmatically-per-request) with a groovy script instead if you want to be able to run the test at scale or use generated JMX.
:::
