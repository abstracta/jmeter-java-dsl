#### Cookies & caching

jmeter-java-dsl automatically adds a cookie manager and cache manager for automatic HTTP cookie and caching handling, emulating a browser behavior. If you need to disable them you can use something like this:

```java
testPlan(
    httpCookies().disable(),
    httpCache().disable(),
    threadGroup(2, 10,
        httpSampler("http://my.service")
    )
)
```
