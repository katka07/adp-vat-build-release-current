package com.example.vat.util;

import com.example.vat.model.VatCalculation;
import java.math.BigDecimal;
import java.util.Map;

public final class JsonUtil {
    private JsonUtil() { }

    public static String calculation(VatCalculation c) {
        return "{\n" +
                "  \"invoiceNumber\": \"" + escape(c.getInvoiceNumber()) + "\",\n" +
                "  \"country\": \"" + escape(c.getCountry()) + "\",\n" +
                "  \"category\": \"" + c.getCategory().name() + "\",\n" +
                "  \"netAmount\": \"" + c.getNetAmount().toPlainString() + "\",\n" +
                "  \"vatRate\": \"" + c.getVatRate().toPlainString() + "\",\n" +
                "  \"vatAmount\": \"" + c.getVatAmount().toPlainString() + "\",\n" +
                "  \"grossAmount\": \"" + c.getGrossAmount().toPlainString() + "\"\n" +
                "}";
    }

    public static String vatReturn(int invoiceCount, BigDecimal net, BigDecimal vat,
                                   BigDecimal gross, BigDecimal reverseCharge,
                                   Map<String, BigDecimal> countryVatTotals) {
        StringBuilder out = new StringBuilder();
        out.append("{\n");
        out.append("  \"invoiceCount\": ").append(invoiceCount).append(",\n");
        out.append("  \"netTotal\": \"").append(net.toPlainString()).append("\",\n");
        out.append("  \"vatTotal\": \"").append(vat.toPlainString()).append("\",\n");
        out.append("  \"grossTotal\": \"").append(gross.toPlainString()).append("\",\n");
        out.append("  \"reverseChargeNet\": \"").append(reverseCharge.toPlainString()).append("\",\n");
        out.append("  \"countryVatTotals\": {");
        boolean first = true;
        for (Map.Entry<String, BigDecimal> entry : countryVatTotals.entrySet()) {
            if (!first) out.append(",");
            out.append("\n    \"").append(escape(entry.getKey())).append("\": \"")
                    .append(entry.getValue().toPlainString()).append("\"");
            first = false;
        }
        if (!countryVatTotals.isEmpty()) out.append("\n  ");
        out.append("}\n");
        out.append("}\n");
        return out.toString();
    }

    public static String error(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
