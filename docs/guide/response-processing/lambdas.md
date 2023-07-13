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

2. Package your test code in a jar.

   If you use maven, you use `maven-jar-plugin` `test-jar` goal, like this:

   ```xml
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
   ```

3. Copy all dependencies required by the lambda code in addition to `jmeter-java-dsl`.

   This can be easily achieved with `maven-dependency-plugin`, like this:

   ```xml
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
           <includeArtifactIds>jmeter-java-dsl</includeArtifactIds>
         </configuration>
       </execution>
     </executions>
   </plugin>
   ```

4. Add to `BlazeMeterEngine` jars required for the static classes execution.

   You can change the previous example like this:

   ```java
   public class PerformanceTest {
     
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
         ret[0] = new File(String.format("target/myproject-%s-tests.jar", System.getProperty("project.version"))); // change name of project to match
         System.arraycopy(libsFiles, 0, ret, 1, libsFiles.length);
         return ret;
      }
   
   }
   ```
      
   ::: tip
   Currently only `BlazeMeterEngine` provides a way to upload assets. If you need support for other engines, please request it [in an issue](https://github.com/abstracta/issues).
   :::

5. Execute your tests after the test jar package is built.
   
   For this, you can use `maven-failsafe-plugin`, or package your project and run the test individually.

   To use `maven-failsafe-plugin`, add the plugin configuration, like this:

   ```xml
   <plugin>
     <groupId>org.apache.maven.plugins</groupId>
     <artifactId>maven-failsafe-plugin</artifactId>
     <version>3.0.0-M7</version>
     <configuration>
       <!-- Setting this property and then using it code avoids to have to change the code every time version changes -->
       <systemPropertyVariables>
         <project.version>${project.version}</project.version> 
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
   ```
   
   And rename the test class to end in IT (which is the default suffix used by the plugin for detecting tests). Eg.: from `PerformanceTest` to `PerformanceIT`.

   Now you can just run your test with `mvn clean verify` or as part of `mvn clean install`.

##### Lambdas in standalone JMeter

If you save your test plan with the `saveAsJmx()` test plan method and then want to execute the test plan in JMeter, you will need to:

1. Replace all Java lambdas with public static classes implementing proper script interface.
   
   Same as the previous section.

2. Package your test code in a jar.

   Same as the previous section.

3. Copy all dependencies, in addition to `jmeter-java-dsl`, required by the lambda code to JMeter `lib/ext` folder.
   
   You can also use `maven-dependency-plugin` and run `mvn package -DskipTests` to get the actual jars.
