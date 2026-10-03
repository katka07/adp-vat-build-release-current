package com.example.vat.core;

import com.example.vat.model.VatCategory;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class VatRuleTable {
    private final Map<String, Map<VatCategory, BigDecimal>> rules;

    public VatRuleTable() {
        Map<String, Map<VatCategory, BigDecimal>> data = new HashMap<String, Map<VatCategory, BigDecimal>>();
        data.put("GB", rates("20.00", "5.00"));
        data.put("DE", rates("19.00", "7.00"));
        data.put("FR", rates("20.00", "5.50"));
        this.rules = Collections.unmodifiableMap(data);
    }

    private static Map<VatCategory, BigDecimal> rates(String standard, String reduced) {
        Map<VatCategory, BigDecimal> result = new HashMap<VatCategory, BigDecimal>();
        result.put(VatCategory.STANDARD, new BigDecimal(standard));
        result.put(VatCategory.REDUCED, new BigDecimal(reduced));
        result.put(VatCategory.ZERO, BigDecimal.ZERO);
        result.put(VatCategory.REVERSE_CHARGE, BigDecimal.ZERO);
        return Collections.unmodifiableMap(result);
    }

    public BigDecimal rateFor(String country, VatCategory category) {
        if (country == null || category == null) {
            throw new IllegalArgumentException("country and category are required");
        }
        Map<VatCategory, BigDecimal> countryRules = rules.get(country.toUpperCase());
        if (countryRules == null) {
            throw new IllegalArgumentException("unsupported VAT country: " + country);
        }
        BigDecimal rate = countryRules.get(category);
        if (rate == null) {
            throw new IllegalArgumentException("unsupported VAT category: " + category);
        }
        return rate;
    }
}
