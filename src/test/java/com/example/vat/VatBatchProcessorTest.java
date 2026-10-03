package com.example.vat;

import com.example.vat.batch.VatBatchProcessor;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VatBatchProcessorTest {
    @Test
    public void aggregatesRepresentativeBatch() throws Exception {
        Path csv = Files.createTempFile("vat-test", ".csv");
        try {
            String input = "invoice_id,country,category,net_amount,customer_id\n" +
                    "INV-1,GB,STANDARD,100.00,C1\n" +
                    "INV-2,DE,REDUCED,100.00,C2\n" +
                    "INV-3,FR,REVERSE_CHARGE,50.00,C3\n";
            Files.write(csv, input.getBytes(StandardCharsets.UTF_8));
            String result = new VatBatchProcessor().process(csv);
            assertTrue(result.contains("\"invoiceCount\": 3"));
            assertTrue(result.contains("\"vatTotal\": \"27.00\""));
            assertTrue(result.contains("\"reverseChargeNet\": \"50.00\""));
        } finally {
            Files.deleteIfExists(csv);
        }
    }
}
