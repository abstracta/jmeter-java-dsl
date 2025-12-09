testPlan(
        threadGroup(1, 1,
          websocketConnect("ws://echo.websocket.org:80/")
            .connectionTimeout(15000)
            .responseTimeout(10000),
          websocketWrite("Hello from JMeter WebSocket Test"),
          websocketRead()
            .responseTimeout(10000)
            .waitForResponse(false),
          websocketDisconnect()
            .responseTimeout(2000)
            .statusCode("3000")
        )
    )