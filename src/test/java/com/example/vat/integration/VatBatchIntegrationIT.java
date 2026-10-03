package com.example.vat.integration;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VatBatchIntegrationIT {
    @Test
    public void packagedJarProcessesApprovedSampleBatch() throws Exception {
        Path baseDir = Paths.get(System.getProperty("project.basedir"));
        Path jar = Paths.get(
                System.getProperty("project.build.directory"),
                System.getProperty("project.build.finalName") + ".jar");
        Path input = baseDir.resolve("samples/invoices.csv");
        Path expected = baseDir.resolve("samples/expected-vat-return.json");
        Path actual = Paths.get(System.getProperty("project.build.directory"), "integration-vat-return.json");
        Files.deleteIfExists(actual);

        Process process = new ProcessBuilder(
                javaBinary(), "-jar", jar.toString(),
                "batch", input.toString(), actual.toString())
                .redirectErrorStream(true)
                .start();

        String output = readAll(process.getInputStream());
        int exitCode = process.waitFor();
        assertEquals(0, exitCode, "batch invocation should exit successfully: " + output);

        String expectedJson = normalize(new String(Files.readAllBytes(expected), StandardCharsets.UTF_8));
        String actualJson = normalize(new String(Files.readAllBytes(actual), StandardCharsets.UTF_8));
        assertEquals(expectedJson, actualJson);
    }

    private static String javaBinary() {
        return Paths.get(System.getProperty("java.home"), "bin", "java").toString();
    }

    private static String normalize(String value) {
        return value.replace("\r\n", "\n").trim();
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
