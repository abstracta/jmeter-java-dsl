testPlan(
        threadGroup(1, 1,
          DslWebsocketFactory.websocketConnect("ws://ws.postman-echo.com:80/raw"),
          DslWebsocketFactory.websocketWrite("Hello WebSocket Test!"),
          DslWebsocketFactory.websocketRead(),
          DslWebsocketFactory.websocketDisconnect()
        )
    )