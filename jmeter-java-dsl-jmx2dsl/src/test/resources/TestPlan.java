testPlan(
        threadGroup(1, 3,
          httpSampler("http://localhost")
            .post("{\"var\":\"val\"}", ContentType.APPLICATION_JSON),
          httpSampler("http://localhost")
        )
    )