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
As previously mentioned, even though using Java Lambdas has several benefits, they are also less portable. Check [this section](../../response-processing/lambdas.md#lambdas) for more details.
:::
