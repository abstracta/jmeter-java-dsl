///usr/bin/env jbang "$0" "$@" ; exit $?
/*
These commented lines make the class executable if you have jbang installed by making file
executable (eg: chmod +x ./PerformanceTest.java) and just executing it with ./PerformanceTest.java
*/
%s

%s

%s

public class PerformanceTest {

  @Test
  public void test() throws IOException {
    TestPlanStats stats = %s.run();
    assertThat(stats.overall().errorsCount()).isEqualTo(0);
  }

  /*
   This method is only included to make test class self executable. You can remove it when
   executing tests with maven, gradle or some other tool.
   */
  public static void main(String[] args) {
    SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
    LauncherFactory.create()
        .execute(LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(PerformanceTest.class))
                .build(),
            summaryListener);
    TestExecutionSummary summary = summaryListener.getSummary();
    summary.printFailuresTo(new PrintWriter(System.err));
    System.exit(summary.getTotalFailureCount() > 0 ? 1 : 0);
  }

}
