%s

%s

public class PerformanceTest {

  @Test
  public void test() throws IOException {
    TestPlanStats stats = %s.run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

}
