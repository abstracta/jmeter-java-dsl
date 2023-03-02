testPlan(
        threadGroup(1, 1,
          httpDefaults()
            .encoding(StandardCharsets.UTF_8),
          httpSampler("/-1", "http://localhost:8088"),
          httpSampler("/home-3", "http://localhost:8088/home")
            .children(
              regexExtractor("productId#2", "name=\"productId\" value=\"([^\"]+)\"")
                .defaultValue("productId#2_NOT_FOUND")
            ),
          httpSampler("/cart-16", "http://localhost:8088/cart")
            .method(HTTPConstants.POST)
            .contentType(ContentType.APPLICATION_FORM_URLENCODED)
            .rawParam("productId", "${productId#2}"),
          httpSampler("/cart-17", "http://localhost:8088/cart")
            .children(
              regexExtractor("productId#4", "name=\"productId\" value=\"([^\"]+)\"")
                .defaultValue("productId#4_NOT_FOUND")
            )
        )
    )