package com.example.vat.smoke;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VatJarSmokeIT {
    @Test
    public void packagedJarPerformsRepresentativeCalculation() throws Exception {
        Path jar = packagedJar();
        Process process = new ProcessBuilder(
                javaBinary(), "-jar", jar.toString(),
                "calculate", "INV-SMOKE-001", "GB", "STANDARD", "1000.00")
                .redirectErrorStream(true)
                .start();

        String output = readAll(process.getInputStream());
        int exitCode = process.waitFor();

        assertEquals(0, exitCode, "packaged JAR should exit successfully: " + output);
        assertTrue(output.contains("\"vatRate\": \"20.00\""));
        assertTrue(output.contains("\"vatAmount\": \"200.00\""));
        assertTrue(output.contains("\"grossAmount\": \"1200.00\""));
    }

    private static Path packagedJar() {
        String directory = System.getProperty("project.build.directory");
        String finalName = System.getProperty("project.build.finalName");
        return Paths.get(directory, finalName + ".jar");
    }

    private static String javaBinary() {
        return Paths.get(System.getProperty("java.home"), "bin", "java").toString();
    }

    private static String readAll(InputStream input) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) >= 0) {
            out.write(buffer, 0, count);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
