testPlan(
        threadGroup(1, 1,
          DslWebsocketSampler.connect("ws://ws.postman-echo.com:80/raw")
            .connectionTimeout("10000")
            .responseTimeout("5000"),
          //DslWebsocketSampler.write()
          //  .requestData("Disabled write message"),
          DslWebsocketSampler.read()
            .responseTimeout("5000"),
          //DslWebsocketSampler.write()
          //  .requestData("Another disabled write"),
          DslWebsocketSampler.disconnect()
            .responseTimeout("1000")
            .statusCode("1000")
        )
    )