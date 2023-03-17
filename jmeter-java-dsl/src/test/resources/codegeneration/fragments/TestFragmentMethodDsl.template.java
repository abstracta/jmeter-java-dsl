private DslTestFragmentController {{methodName}}() {
  return fragment({{fragmentName}}
      httpSampler("https://myservice.com")
  );
}