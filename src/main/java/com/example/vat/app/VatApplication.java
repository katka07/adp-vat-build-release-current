package com.example.vat.app;

import com.example.vat.batch.VatBatchProcessor;
import com.example.vat.core.VatCalculator;
import com.example.vat.core.VatRuleTable;
import com.example.vat.model.VatCategory;
import com.example.vat.util.JsonUtil;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class VatApplication {
    private VatApplication() { }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
            System.exit(2);
        }

        if ("version".equals(args[0])) {
            Package pkg = VatApplication.class.getPackage();
            String version = pkg == null ? null : pkg.getImplementationVersion();
            System.out.println(version == null ? "unknown" : version);
            return;
        }

        if ("calculate".equals(args[0])) {
            if (args.length != 5) {
                usage();
                System.exit(2);
            }
            VatCalculator calculator = new VatCalculator(new VatRuleTable());
            System.out.println(JsonUtil.calculation(calculator.calculate(
                    args[1], args[2], VatCategory.valueOf(args[3].toUpperCase()),
                    new BigDecimal(args[4]))));
            return;
        }

        if ("batch".equals(args[0])) {
            if (args.length != 3) {
                usage();
                System.exit(2);
            }
            Path input = Paths.get(args[1]);
            Path output = Paths.get(args[2]);
            String report = new VatBatchProcessor().process(input);
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(output, report.getBytes(StandardCharsets.UTF_8));
            System.out.println("VAT return written to " + output.toAbsolutePath());
            return;
        }

        usage();
        System.exit(2);
    }

    private static void usage() {
        System.err.println("Usage:");
        System.err.println("  java -jar vat-processing-service.jar version");
        System.err.println("  java -jar vat-processing-service.jar calculate <invoice> <country> <category> <net>");
        System.err.println("  java -jar vat-processing-service.jar batch <input.csv> <output.json>");
    }
}
