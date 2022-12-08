testPlan(
        httpCache()
          .disable(),
        threadGroup(1, 1,
          httpSampler("http://myservice.com")//,
          //ifController("true",
          //  httpSampler("http://myservice.com")
          //)
        )
    )