package com.example.vat.qa;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VatQaSqaQualificationIT {
    @Test
    public void candidateJarReportsExpectedVersion() throws Exception {
        ProcessResult result = run("version");
        assertEquals(0, result.exitCode, result.output);
        assertEquals(System.getProperty("expected.version"), result.output.trim());
    }

    @Test
    public void candidateJarProcessesQaSqaQualificationDataset() throws Exception {
        Path output = Paths.get("target", "qa-vat-return.json").toAbsolutePath();
        Files.deleteIfExists(output);
        ProcessResult result = run(
                "batch",
                System.getProperty("qa.data"),
                output.toString());
        assertEquals(0, result.exitCode, result.output);
        assertTrue(Files.exists(output), "QA output should be generated");
        String actual = normalize(new String(Files.readAllBytes(output), StandardCharsets.UTF_8));
        String expected = normalize(new String(
                Files.readAllBytes(Paths.get(System.getProperty("qa.expected"))),
                StandardCharsets.UTF_8));
        assertEquals(expected, actual);
    }

    @Test
    public void candidateJarRejectsUnsupportedVatCountry() throws Exception {
        ProcessResult result = run("calculate", "QA-NEG-001", "ZZ", "STANDARD", "100.00");
        assertTrue(result.exitCode != 0, "Unsupported country must fail");
    }

    private ProcessResult run(String... args) throws Exception {
        String[] command = new String[3 + args.length];
        command[0] = javaBinary();
        command[1] = "-jar";
        command[2] = System.getProperty("vat.jar");
        System.arraycopy(args, 0, command, 3, args.length);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = readAll(process.getInputStream());
        return new ProcessResult(process.waitFor(), output);
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

    private static final class ProcessResult {
        private final int exitCode;
        private final String output;
        private ProcessResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }
}
