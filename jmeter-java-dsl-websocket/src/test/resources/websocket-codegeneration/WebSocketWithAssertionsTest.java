testPlan(
        threadGroup(1, 1,
          DslWebsocketSampler.connect("ws://ws.postman-echo.com:80/raw")
            .connectionTimeout("10000")
            .responseTimeout("5000"),
          DslWebsocketSampler.write()
            .requestData("Test message with assertions"),
          DslWebsocketSampler.read()
            .responseTimeout("5000")
            .children(
              responseAssertion()
                .containsRegexes("Test message with assertions")
            ),
          DslWebsocketSampler.disconnect()
            .responseTimeout("1000")
            .statusCode("1000")
        )
    )