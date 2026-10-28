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

By default, cookies are cleared on each thread iteration, emulating a new user session in each iteration. If you want to keep cookies between iterations (for example, to log in once and then iterate over some actions), you can use `httpCookies().clearCookiesBetweenIterations(false)`. Additionally, you can change the cookie policy with `httpCookies().cookiePolicy(...)` if the application under test requires a specific cookie specification. These settings need to be added before any HTTP sampler in the scope you want them to apply to.

If you need to send custom cookies (for example, a session cookie obtained by some other means), you can add them with `httpCookies().add(...)` and `httpCookie(...)`:

```java
testPlan(
    httpCookies()
        .add(httpCookie("MyCookie", "val", "my.service")
            .path("/users")
            .secure(true)
            .expires(Instant.now().plusSeconds(3600))
        ),
    threadGroup(2, 10,
        httpSampler("http://my.service/users")
    )
)
```

The domain is required, and the cookie is sent to requests to the given domain and its subdomains. Path, secure and expiration are optional, and expired cookies are not added. You can use JMeter expressions in cookies names, values, domains and paths (eg: `${MY_VAR}`).

When `httpCookies().add(...)` is used at test plan level, as in the previous example, cookies are set at the beginning of each thread iteration and sent in all matching requests. If you need to set cookies at a particular point of the test plan execution, only affecting requests after it, you can add it inside a thread group or controller:

```java
testPlan(
    threadGroup(2, 10,
        httpSampler("http://my.service/public"), // no custom cookie is sent here
        httpCookies()
            .add(httpCookie("MyCookie", "val", "my.service")),
        httpSampler("http://my.service/users") // custom cookie is sent here
    )
)
```

::: warning
When used inside a thread group or controller, cookies are set by a JSR223 sampler which is excluded from collected statistics. Since it is a sampler, timers, pre-processors, post-processors and assertions in its scope also apply to it. For example, a timer at thread group level adds a pause before setting the cookies.
:::
