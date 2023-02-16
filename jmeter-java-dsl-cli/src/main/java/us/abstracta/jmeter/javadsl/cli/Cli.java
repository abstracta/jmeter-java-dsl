package us.abstracta.jmeter.javadsl.cli;

import java.net.URL;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.IVersionProvider;

@Command(name = "jmdsl", mixinStandardHelpOptions = true, usageHelpAutoWidth = true,
    versionProvider = Cli.ManifestVersionProvider.class,
    header = "This tool includes utility commands which complement JMeter DSL library.",
    subcommands = {Jmx2DslCommand.class, RecorderCommand.class, HelpCommand.class})
public class Cli {

  public static class ManifestVersionProvider implements IVersionProvider {

    public String[] getVersion() throws Exception {
      URL manifestResource = Cli.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
      Manifest manifest = new Manifest(manifestResource.openStream());
      return new String[]{manifest.getMainAttributes().getValue(Name.IMPLEMENTATION_VERSION)};
    }

  }

  public static void main(String[] args) throws Exception {
    System.setProperty("log4j2.Script.enableLanguages", "bsh");
    String version = new ManifestVersionProvider().getVersion()[0];
    System.setProperty("jmdsl.version", version != null ? version : "");
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    int exitCode = new CommandLine(new Cli()).execute(args);
    System.exit(exitCode);
  }

}
