testPlan(
        threadGroup(1, 1,
          webSocketSampler().connect("ws://ws.postman-echo.com:80/raw")
            .connectionTimeout("10000")
            .responseTimeout("5000"),
          webSocketSampler().write()
            .requestData("Test message with assertions"),
          webSocketSampler().read()
            .responseTimeout("5000")
            .children(
              responseAssertion()
                .containsRegexes("Test message with assertions")
            ),
          webSocketSampler().disconnect()
            .responseTimeout("1000")
            .statusCode("1000")
        )
    )