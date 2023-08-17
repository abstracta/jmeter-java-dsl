#### Lambdas

As previously mentioned, using Java lambdas is in general more performant than using Groovy scripts ([here](https://github.com/abstracta/jmeter-java-dsl/releases/tag/v1.14) are some comparisons) and are easier to develop and maintain due to type safety, IDE autocompletion, etc.

But, they are also **less portable**.

For instance, they will not work out of the box with remote engines (like `BlazeMeterEngine`) or while saving JMX and running it in standalone JMeter.

One option is using groovy scripts and `__groovy` function, but doing so, you lose the previously mentioned benefits.

Here is another approach to still benefit from Java code (vs Groovy script) and run in remote engines and standalone JMeter.

##### Lambdas in remote engine

Here are the steps to run test plans containing Java lambdas in `BlazeMeterEngine`:

1. Replace all Java lambdas with public static classes implementing proper script interface.

   For example, if you have the following test:

   ```java
   public class PerformanceTest {
   
     @Test
     public void testPerformance() throws Exception {
       testPlan(
           threadGroup(2, 10,
               httpSampler("http://my.service")
                   .children(
                       jsr223PostProcessor(s -> {
                         if ("429".equals(s.prev.getResponseCode())) {
                           s.prev.setSuccessful(true);
                         }
                       })
                   )
           )
       ).runIn(new BlazeMeterEngine(System.getenv("BZ_TOKEN")));
     }
     
   }
   ```
   
   You can change it to:

   ```java
   public class PerformanceTest {
     
     public static class StatusSuccessProcessor implements PostProcessorScript {
   
       @Override
       public void runScript(PostProcessorVars s) {
         if ("429".equals(s.prev.getResponseCode())) {
           s.prev.setSuccessful(true);
         }
       }
       
     }
   
     @Test
     public void testPerformance() throws Exception {
       testPlan(
           threadGroup(2, 10,
               httpSampler("http://my.service")
                   .children(
                       jsr223PostProcessor(StatusSuccessProcessor.class)
                   )
           )
       ).runIn(new BlazeMeterEngine(System.getenv("BZ_TOKEN")));
     }
   
   }
   ```
   
   > Script interface to implement, depends on where you use the lambda code. Available interfaces are `PropertyScript`, `PreProcessorScript`, `PostProcessorScript`, and `SamplerScript`.

2. Upload your test code and dependencies to BlazeMeter.

   If you use maven, here is what you can add to your project to configure this:

   ```xml
   <plugins>
     ...
     <!-- this generates a jar containing your test code (including the public static class previously mentioned) -->
     <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>3.3.0</version>
        <executions>
          <execution>
            <goals>
              <goal>test-jar</goal>
            </goals>
          </execution>
        </executions>
     </plugin>
     <!-- this copies project dependencies to target/libs directory -->
     <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-dependency-plugin</artifactId>
       <version>3.6.0</version>
       <executions>
         <execution>
           <id>copy-dependencies</id>
           <phase>package</phase>
           <goals>
             <goal>copy-dependencies</goal>
           </goals>
           <configuration>
             <outputDirectory>${project.build.directory}/libs</outputDirectory>
             <!-- include here, separating by commas, any additional dependencies (just the artifacts ids) you need to upload to BlazeMeter -->
             <!-- AzureEngine automatically uploads JMeter dsl artifacts, so only transitive or custom dependencies would be required -->
             <!-- if you would like for BlazeMeterEngine and OctoPerfEngine to automatically upload JMeter DSL artifacts, please create an issue in GitHub repository -->
             <includeArtifactIds>jmeter-java-dsl</includeArtifactIds>
           </configuration>
         </execution>
       </executions>
     </plugin>
     <!-- this takes care of executing tests classes ending with IT after test jar is generated and dependencies are copied -->
     <!-- additionally, it sets some system properties as to easily identify test jar file -->
     <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-failsafe-plugin</artifactId>
       <version>3.0.0-M7</version>
       <configuration>
         <systemPropertyVariables>
           <testJar.path>${project.build.directory}/${project.artifactId}-${project.version}-tests.jar</testJar.path> 
         </systemPropertyVariables>
       </configuration>
       <executions>
         <execution>
           <goals>
             <goal>integration-test</goal>
             <goal>verify</goal>
           </goals>
         </execution>
       </executions>
     </plugin>
   </plugins>
   ```

   Additionally, rename your test class to use IT suffix (so it runs after test jar is created and dependencies are copied), and add to `BlazeMeterEngine` logic to upload the jars. For example:

   ```java
   // Here we renamed from PerformanceTest to PerformanceIT
   public class PerformanceIT {
     
      ...
      
      @Test
      public void testPerformance() throws Exception {
         testPlan(
                 ...
         ).runIn(new BlazeMeterEngine(System.getenv("BZ_TOKEN"))
                 .assets(findAssets()));
      }
   
      private File[] findAssets() {
         File[] libsFiles = new File("target/libs").listFiles();
         File[] ret = new File[libsFiles.length + 1];
         ret[0] = new File(System.getProperty("testJar.path"));
         System.arraycopy(libsFiles, 0, ret, 1, libsFiles.length);
         return ret;
      }
   
   }
   ```
      
   ::: tip
   Currently only `BlazeMeterEngine` provides a way to upload assets. If you need support for other engines, please request it [in an issue](https://github.com/abstracta/issues).
   :::

3. Execute your tests with maven (either with `mvn clean verify` or as part of `mvn clean install`) or IDE (by first packaging your project, and then executing `PerformanceIT` test).

##### Lambdas in standalone JMeter

If you save your test plan with the `saveAsJmx()` test plan method and then want to execute the test plan in JMeter, you will need to:

1. Replace all Java lambdas with public static classes implementing proper script interface.
   
   Same as the previous section.

2. Package your test code in a jar.

   Same as the previous section.

3. Copy all dependencies, in addition to `jmeter-java-dsl`, required by the lambda code to JMeter `lib/ext` folder.
   
   You can also use `maven-dependency-plugin` and run `mvn package -DskipTests` to get the actual jars.
   If the test plan requires any particular jmeter plugin, then you would need to copy those as well.
