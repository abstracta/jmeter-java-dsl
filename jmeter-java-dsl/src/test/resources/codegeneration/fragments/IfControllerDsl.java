private DslIfController ifController() {
  return ifController("true",
      httpSampler("http://myservice.com")
  );
}