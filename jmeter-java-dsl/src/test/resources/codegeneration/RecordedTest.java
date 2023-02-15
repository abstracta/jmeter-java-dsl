testPlan()
        .tearDownOnlyAfterMainThreadsDone()
        .children(
          vars()
            .set("host", "abstracta.us")
            .set("scheme", "https"),
          httpDefaults()
            .url("https://abstracta.us")
            .encoding(StandardCharsets.UTF_8),
          httpCookies()
            .clearCookiesBetweenIterations(false),
          httpCache()
            .disable(),
          threadGroup(1, 1,
            transaction("/-66",
              httpSampler("/-66", "/")
                .port(443)
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("DNT", "1")
                .header("Sec-Fetch-User", "?1")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:97.0) Gecko/20100101 Firefox/97.0")
                .header("Sec-Fetch-Dest", "document")
            ),
            transaction("/solutions/software-testing-107",
              httpSampler("/solutions/software-testing-107", "/solutions/software-testing")
                .port(443)
                .header("Sec-Fetch-Mode", "navigate")
                .header("Referer", "${scheme}://${host}/")
                .header("Sec-Fetch-Site", "same-origin")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("DNT", "1")
                .header("Sec-Fetch-User", "?1")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:97.0) Gecko/20100101 Firefox/97.0")
                .header("Sec-Fetch-Dest", "document")
            )
          ),
          resultsTreeVisualizer()
        )