testPlan(
        threadGroup(1, 1,
          DslWebsocketFactory.websocketConnect("ws://echo.websocket.org:80/")
            .connectionTimeout(15000)
            .responseTimeout(10000),
          DslWebsocketFactory.websocketWrite("Hello from JMeter WebSocket Test"),
          DslWebsocketFactory.websocketRead()
            .responseTimeout(10000)
            .waitForResponse(false),
          DslWebsocketFactory.websocketDisconnect()
            .responseTimeout(2000)
            .statusCode("3000")
        )
    )