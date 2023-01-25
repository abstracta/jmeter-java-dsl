#### Authentication

When in need to authenticate user associated to an HTTP request you can either use `httpAuth` or custom logic (with HTTP headers, regex extractors, variables, and other potential elements) to properly generate the required requests.

`httpAuth` greatly simplifies common scenarios like this example using basic auth:

```java
String baseUrl = "http://my.service";
testPlan(
    httpAuth()
        .basicAuth(baseUrl, System.getenv("AUTH_USER"), System.getenv("AUTH_PASSWORD")),
    threadGroup(2, 10,
        httpSampler(baseUrl + "/login"),
        httpSampler(baseUrl + "/users")
    )
).run();
```

::: tip
Even though you can specify an empty base URL to match any potential request, don't do it. Defining a non-specific enough base URL, may leak credentials to unexpected sites, for example, when used in combination with `downloadEmbeddedResources()`.
:::

::: tip
Avoid including credentials in repository where code is hosted, which might lead to security leaks. 

In provided example credentials are obtained from environment variable that have to be predefined by user when running tests, but you can also use other approaches to avoid security leaks.

Also take into consideration that if you use `jtlWriter` and chose to store HTTP request headers and/or bodies, then JTL could include used credentials and might be also a potential source for security leaks. 
:::

::: tip
Http Authorization Manager, the element used by `httpAuth`, automatically adds the `Authorization` header for each request that starts with the given base url. If you need more control (e.g.: only send the header in the first request or under certain condition), you might add `httpAuth` only to specific requests or just build custom logic through usage of `httpHeaders`, `regexExtractor` and `jsr223PreProcessor`.
:::

::: tip
Currently `httpAuth()` only provides `basicAuth` method. If you need other scenarios, please let us know by creating an [issue in the repository](https://github.com/abstracta/jmeter-java-dsl/issues).
:::

You can check additional details in [DslAuthManager](/jmeter-java-dsl/src/main/java/us/abstracta/jmeter/javadsl/http/DslAuthManager.java).
