    testPlan(
        threadGroup(1, 1,
            webSocketSampler()
                .connect()
                .server("ws.postman-echo.com")
                .port("80")
                .path("/raw")
                .tls(false)
                .connectionTimeout("10000")
                .responseTimeout("5000"),
            // webSocketSampler()
            //     .write()
            //     .requestData("Disabled write message")
            //     .createNewConnection(false),
            webSocketSampler()
                .read()
                .responseTimeout("5000")
                .createNewConnection(false),
            // webSocketSampler()
            //     .write()
            //     .requestData("Another disabled write")
            //     .createNewConnection(false),
            webSocketSampler()
                .disconnect()
                .responseTimeout("1000")
                .statusCode("1000")
        )
    )
