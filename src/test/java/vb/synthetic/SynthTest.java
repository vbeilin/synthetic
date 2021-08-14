package vb.synthetic;

import static java.lang.System.out;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import vb.synthetic.testutils.PredefinedClassLoader;

class SynthTest {
    /*
     * UnderTest::synthetic is marked with @Synthetic, and must be callable but not compilable (via TryToUseSynth) 
     */
    @Test
    void test() throws Exception {
        String underTestName = "vb.synthetic.UnderTest";
        String tryToUseSynthName = "vb.synthetic.TryToUseSynth";
        String processedClassBaseLocation = "test/processed";

        // prepare
        String underTestBinaryName = underTestName.replace('.', '/');

        byte[] processed = Processor.processClass(getClass().getResourceAsStream("/" + underTestBinaryName + ".class"));
        storeProcessedClass(processedClassBaseLocation, underTestBinaryName, processed);

        
        // compile
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        String thisJavaLocation = "src/test/java";
        String thisClassLocation = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI())
                .getAbsolutePath();

        out.println(thisClassLocation);

        var stdout = new ByteArrayOutputStream();
        var stderr = new ByteArrayOutputStream();
        int result = compiler.run(null, stdout, stderr, "-cp",
                processedClassBaseLocation + File.pathSeparator + thisClassLocation, "-d", processedClassBaseLocation,
                new File(thisJavaLocation + "/" + tryToUseSynthName.replace('.', '/') + ".java").getAbsolutePath());

        String errContents = new String(stderr.toByteArray());
        String outContents = new String(stdout.toByteArray());
        out.println(result);
        out.println("** ERR");
        out.println(errContents);
        out.println("** OUT");
        out.println(outContents);

        assertEquals(1, result);
        assertThat(errContents, allOf(containsString("symbol:   method synthetic(String)"), containsString("1 error")));

        
        // load
        var cl = new PredefinedClassLoader(getClass().getClassLoader(), underTestBinaryName, processed);

        Class<?> processedClass = cl.loadClass(underTestBinaryName);
        Stream.of(processedClass.getMethods()).forEach(m -> out.println(m + (m.isSynthetic() ? " synthetic" : " ")));

        Class<?> klass = cl.loadClass(tryToUseSynthName);
        Object tryToUseSynth = klass.getConstructor().newInstance();
        assertEquals("compiled-1-2", klass.getMethod("a").invoke(tryToUseSynth));
    }

    private void storeProcessedClass(String base, String binaryName, byte[] processed) throws IOException {
        Path processedClassLocation = Paths.get(base, binaryName + ".class");
        Files.createDirectories(processedClassLocation.getParent());
        Files.copy(new ByteArrayInputStream(processed), processedClassLocation, StandardCopyOption.REPLACE_EXISTING);
    }
}
