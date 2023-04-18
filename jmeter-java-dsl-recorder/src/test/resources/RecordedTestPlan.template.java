testPlan(
        threadGroup(1, 1,
          httpDefaults()
            .encoding(StandardCharsets.UTF_8)
            .followRedirects(false),
          httpSampler("/-{{\d+}}", "http://localhost:{{\d+}}"),
          httpSampler("/home-{{\d+}}", "http://localhost:{{\d+}}/home")
            .children(
              regexExtractor("productId#2", "name=\"productId\" value=\"([^\"]+)\"")
                .defaultValue("productId#2_NOT_FOUND")
            ),
          httpSampler("/cart-{{\d+}}", "http://localhost:{{\d+}}/cart")
            .method(HTTPConstants.POST)
            .contentType(ContentType.APPLICATION_FORM_URLENCODED)
            .param("productId", "${productId#2}"),
          httpSampler("/cart-{{\d+}}", "http://localhost:{{\d+}}/cart")
            .children(
              regexExtractor("productId#4", "name=\"productId\" value=\"([^\"]+)\"")
                .defaultValue("productId#4_NOT_FOUND")
            )
        )
    )