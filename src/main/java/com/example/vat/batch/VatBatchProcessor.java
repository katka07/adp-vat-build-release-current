package com.example.vat.batch;

import com.example.vat.core.VatCalculator;
import com.example.vat.core.VatRuleTable;
import com.example.vat.model.VatCalculation;
import com.example.vat.model.VatCategory;
import com.example.vat.util.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public final class VatBatchProcessor {
    private final VatCalculator calculator = new VatCalculator(new VatRuleTable());

    public String process(Path csvFile) throws IOException {
        int invoiceCount = 0;
        BigDecimal netTotal = BigDecimal.ZERO;
        BigDecimal vatTotal = BigDecimal.ZERO;
        BigDecimal grossTotal = BigDecimal.ZERO;
        BigDecimal reverseChargeNet = BigDecimal.ZERO;
        Map<String, BigDecimal> countryTotals = new TreeMap<String, BigDecimal>();

        BufferedReader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8);
        try {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;
                String[] fields = line.split(",");
                if (fields.length != 5) {
                    throw new IllegalArgumentException("invalid CSV row: " + line);
                }
                String invoice = fields[0].trim();
                String country = fields[1].trim();
                VatCategory category = VatCategory.valueOf(fields[2].trim().toUpperCase());
                BigDecimal net = new BigDecimal(fields[3].trim());

                VatCalculation result = calculator.calculate(invoice, country, category, net);
                invoiceCount++;
                netTotal = netTotal.add(result.getNetAmount());
                vatTotal = vatTotal.add(result.getVatAmount());
                grossTotal = grossTotal.add(result.getGrossAmount());
                if (category == VatCategory.REVERSE_CHARGE) {
                    reverseChargeNet = reverseChargeNet.add(result.getNetAmount());
                }
                BigDecimal old = countryTotals.get(country.toUpperCase());
                countryTotals.put(country.toUpperCase(),
                        (old == null ? BigDecimal.ZERO : old).add(result.getVatAmount()));
            }
        } finally {
            reader.close();
        }

        return JsonUtil.vatReturn(
                invoiceCount,
                money(netTotal),
                money(vatTotal),
                money(grossTotal),
                money(reverseChargeNet),
                moneyMap(countryTotals));
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static Map<String, BigDecimal> moneyMap(Map<String, BigDecimal> values) {
        Map<String, BigDecimal> result = new TreeMap<String, BigDecimal>();
        for (Map.Entry<String, BigDecimal> e : values.entrySet()) {
            result.put(e.getKey(), money(e.getValue()));
        }
        return result;
    }
}
