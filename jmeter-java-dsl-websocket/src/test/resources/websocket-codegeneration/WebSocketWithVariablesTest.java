testPlan(
        threadGroup(1, 1,
          websocketConnect("ws://${WEBSOCKET_SERVER}:${WEBSOCKET_PORT}/raw")
            .connectionTimeout("${timeout}")
            .responseTimeout("${timeout}"),
          websocketWrite("${MESSAGE}"),
          websocketRead()
            .responseTimeout("${timeout}"),
          websocketDisconnect()
            .responseTimeout("${timeout}")
            .statusCode("${statusCode}")
        )
    )