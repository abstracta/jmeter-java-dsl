testPlan(
        threadGroup(1, 1,
          DslWebsocketFactory.websocketConnect("ws://${WEBSOCKET_SERVER}:${WEBSOCKET_PORT}/raw")
            .connectionTimeout("${timeout}")
            .responseTimeout("${timeout}"),
          DslWebsocketFactory.websocketWrite("${MESSAGE}"),
          DslWebsocketFactory.websocketRead()
            .responseTimeout("${timeout}"),
          DslWebsocketFactory.websocketDisconnect()
            .responseTimeout("${timeout}")
            .statusCode("${statusCode}")
        )
    )