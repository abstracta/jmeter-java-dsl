testPlan(
        threadGroup(1, 1,
          webSocketSampler().connect("ws://ws.postman-echo.com:80/raw")
            .connectionTimeout("10000")
            .responseTimeout("5000"),
          //webSocketSampler().write()
          //  .requestData("Disabled write message"),
          webSocketSampler().read()
            .responseTimeout("5000"),
          //webSocketSampler().write()
          //  .requestData("Another disabled write"),
          webSocketSampler().disconnect()
            .responseTimeout("1000")
            .statusCode("1000")
        )
    )