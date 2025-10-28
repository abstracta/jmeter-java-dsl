    testPlan(
        threadGroup(1, 1,
            DslWebsocketSampler
                .connect()
                .server("ws.postman-echo.com")
                .port("80")
                .path("/raw")
                .tls(false)
                .connectionTimeout("10000")
                .responseTimeout("5000"),
            // DslWebsocketSampler
            //     .write()
            //     .requestData("Disabled write message")
            //     .createNewConnection(false),
            DslWebsocketSampler
                .read()
                .responseTimeout("5000")
                .createNewConnection(false),
            // DslWebsocketSampler
            //     .write()
            //     .requestData("Another disabled write")
            //     .createNewConnection(false),
            DslWebsocketSampler
                .disconnect()
                .responseTimeout("1000")
                .statusCode("1000")
        )
    )
