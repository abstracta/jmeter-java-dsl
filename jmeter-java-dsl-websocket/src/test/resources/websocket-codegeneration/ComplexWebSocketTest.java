testPlan(
        threadGroup(1, 1,
          webSocketSampler().connect("ws://echo.websocket.org:80/")
            .connectionTimeout("15000")
            .responseTimeout("10000"),
          webSocketSampler().write()
            .requestData("Hello from JMeter WebSocket Test"),
          webSocketSampler().read()
            .responseTimeout("10000"),
          webSocketSampler().disconnect()
            .responseTimeout("2000")
            .statusCode("1000")
        )
    )