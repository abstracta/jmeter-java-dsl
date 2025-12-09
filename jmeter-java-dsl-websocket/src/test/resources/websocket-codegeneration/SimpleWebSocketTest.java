testPlan(
        threadGroup(1, 1,
          websocketConnect("ws://ws.postman-echo.com:80/raw"),
          websocketWrite("Hello WebSocket Test!"),
          websocketRead(),
          websocketDisconnect()
        )
    )