#### Headers

You might have already noticed in some of the examples that we have shown already some ways to set some headers. For instance, in the following snippet `Content-Type` header is being set in two different ways:

```java
httpSampler("http://my.service")
  .post("{\"field\":\"val\"}", Type.APPLICATION_JSON)
httpSampler("http://my.service")
  .contentType(Type.APPLICATION_JSON)
```

These are handy methods to specify the `Content-Type` header, but you can also set any header on a particular request using provided `header` method, like this:

```java
httpSampler("http://my.service")
  .header("X-First-Header", "val1")
  .header("X-Second-Header", "val2")
```

Additionally, you can specify headers to be used by all samplers in a test plan, thread group, transaction controllers, etc. For this you can use `httpHeaders` like this:

```java
testPlan(
    threadGroup(2, 10,
        httpHeaders()
          .header("X-Header", "val1"),
        httpSampler("http://my.service"),
        httpSampler("http://my.service/users")
    )
).run();
```

::: tip
You can also use lambda expressions for dynamically building HTTP Headers, but the same limitations apply as in other cases (running in BlazeMeter, OctoPerf, Azure, or using generated JMX file).
:::
