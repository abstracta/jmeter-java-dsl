testPlan(
        threadGroup(1, 1,
          DslWebsocketSampler.connect("ws://echo.websocket.org:80/")
            .connectionTimeout("15000")
            .responseTimeout("10000"),
          DslWebsocketSampler.write()
            .requestData("Hello from JMeter WebSocket Test"),
          DslWebsocketSampler.read()
            .responseTimeout("10000"),
          DslWebsocketSampler.disconnect()
            .responseTimeout("2000")
            .statusCode("1000")
        )
    )