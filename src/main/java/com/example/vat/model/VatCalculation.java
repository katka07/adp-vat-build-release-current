package com.example.vat.model;

import java.math.BigDecimal;

public final class VatCalculation {
    private final String invoiceNumber;
    private final String country;
    private final VatCategory category;
    private final BigDecimal netAmount;
    private final BigDecimal vatRate;
    private final BigDecimal vatAmount;
    private final BigDecimal grossAmount;

    public VatCalculation(String invoiceNumber, String country, VatCategory category,
                          BigDecimal netAmount, BigDecimal vatRate,
                          BigDecimal vatAmount, BigDecimal grossAmount) {
        this.invoiceNumber = invoiceNumber;
        this.country = country;
        this.category = category;
        this.netAmount = netAmount;
        this.vatRate = vatRate;
        this.vatAmount = vatAmount;
        this.grossAmount = grossAmount;
    }

    public String getInvoiceNumber() { return invoiceNumber; }
    public String getCountry() { return country; }
    public VatCategory getCategory() { return category; }
    public BigDecimal getNetAmount() { return netAmount; }
    public BigDecimal getVatRate() { return vatRate; }
    public BigDecimal getVatAmount() { return vatAmount; }
    public BigDecimal getGrossAmount() { return grossAmount; }
}
