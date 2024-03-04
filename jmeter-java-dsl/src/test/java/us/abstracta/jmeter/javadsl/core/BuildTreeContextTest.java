package us.abstracta.jmeter.javadsl.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

public class BuildTreeContextTest {

    @Test
    public void setEntryAddsKeyValuePair() {
        BuildTreeContext context = new BuildTreeContext();
        String key = "key";
        String value = "value";

        context.setEntry(key, value);

        assertEquals(value, context.getEntry(key));
    }

    @Test
    public void buildRemoteExecutionContextInitializesMaps() {
        BuildTreeContext context = BuildTreeContext.buildRemoteExecutionContext();

        assertNotNull(context.getVisualizers());
        assertTrue(context.getVisualizers().isEmpty());
        assertNotNull(context.getAssetFiles());
        assertTrue(context.getAssetFiles().isEmpty());
    }

    @Test
    public void processAssetFileHandlesDuplicatesByPrependingIndex() throws Exception {
        BuildTreeContext context = BuildTreeContext.buildRemoteExecutionContext();
        setAssetFilesField(context, new LinkedHashMap<>());

        Path tempFile1 = Files.createTempFile("sample", ".txt");
        String processedPath1 = context.processAssetFile(tempFile1.toString());
        assertEquals(tempFile1.getFileName().toString(), new File(processedPath1).getName());

        String processedPath2 = context.processAssetFile(tempFile1.toString());

        assertEquals(tempFile1.getFileName().toString(), processedPath2, "Processed file name should have a index for duplicates.");

        Files.deleteIfExists(tempFile1);
    }

    private void setAssetFilesField(BuildTreeContext context, LinkedHashMap<String, File> assetFiles) throws Exception {
        java.lang.reflect.Field field = BuildTreeContext.class.getDeclaredField("assetFiles");
        field.setAccessible(true);
        field.set(context, assetFiles);
    }
}
