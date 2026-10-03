package com.example.vat;

import com.example.vat.core.VatCalculator;
import com.example.vat.core.VatRuleTable;
import com.example.vat.model.VatCalculation;
import com.example.vat.model.VatCategory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VatCalculatorTest {
    private final VatCalculator calculator = new VatCalculator(new VatRuleTable());

    @Test
    public void calculatesGbStandardVat() {
        VatCalculation result = calculator.calculate(
                "INV-1", "GB", VatCategory.STANDARD, new BigDecimal("1000.00"));
        assertEquals("200.00", result.getVatAmount().toPlainString());
        assertEquals("1200.00", result.getGrossAmount().toPlainString());
    }

    @Test
    public void calculatesDeReducedVat() {
        VatCalculation result = calculator.calculate(
                "INV-2", "DE", VatCategory.REDUCED, new BigDecimal("100.00"));
        assertEquals("7.00", result.getVatAmount().toPlainString());
    }

    @Test
    public void supportsZeroRatedVat() {
        VatCalculation result = calculator.calculate(
                "INV-3", "FR", VatCategory.ZERO, new BigDecimal("125.55"));
        assertEquals("0.00", result.getVatAmount().toPlainString());
    }

    @Test
    public void supportsReverseCharge() {
        VatCalculation result = calculator.calculate(
                "INV-4", "GB", VatCategory.REVERSE_CHARGE, new BigDecimal("300.00"));
        assertEquals("0.00", result.getVatAmount().toPlainString());
    }

    @Test
    public void rejectsUnsupportedCountry() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate("INV-5", "US", VatCategory.STANDARD, new BigDecimal("100.00")));
    }

    @Test
    public void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate("INV-6", "GB", VatCategory.STANDARD, new BigDecimal("-1.00")));
    }
}
