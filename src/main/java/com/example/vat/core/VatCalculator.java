package com.example.vat.core;

import com.example.vat.model.VatCalculation;
import com.example.vat.model.VatCategory;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class VatCalculator {
    private final VatRuleTable ruleTable;

    public VatCalculator(VatRuleTable ruleTable) {
        this.ruleTable = ruleTable;
    }

    public VatCalculation calculate(String invoiceNumber, String country,
                                    VatCategory category, BigDecimal netAmount) {
        if (invoiceNumber == null || invoiceNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("invoice number is required");
        }
        if (netAmount == null || netAmount.signum() < 0) {
            throw new IllegalArgumentException("net amount must be zero or positive");
        }
        BigDecimal rate = ruleTable.rateFor(country, category);
        BigDecimal vat = netAmount
                .multiply(rate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal gross = netAmount.add(vat).setScale(2, RoundingMode.HALF_UP);
        return new VatCalculation(
                invoiceNumber,
                country.toUpperCase(),
                category,
                netAmount.setScale(2, RoundingMode.HALF_UP),
                rate.setScale(2, RoundingMode.HALF_UP),
                vat,
                gross);
    }
}
