testPlan(
        vars()
          .set("WEBSOCKET_SERVER", "ws.postman-echo.com")
          .set("WEBSOCKET_PORT", "80")
          .set("MESSAGE", "Hello from variable"),
        threadGroup(1, 1,
          DslWebsocketSampler.connect("ws://${WEBSOCKET_SERVER}:${WEBSOCKET_PORT}/raw")
            .connectionTimeout("10000")
            .responseTimeout("5000"),
          DslWebsocketSampler.write()
            .requestData("${MESSAGE}"),
          DslWebsocketSampler.read()
            .responseTimeout("5000"),
          DslWebsocketSampler.disconnect()
            .responseTimeout("1000")
            .statusCode("1000")
        )
    )