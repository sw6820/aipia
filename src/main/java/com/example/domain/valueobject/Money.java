package com.example.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Value object representing monetary amounts.
 * Immutable and self-validating with currency support.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Money {
    
    private BigDecimal amount;
    private String currency;
    
    public Money(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        
        this.amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
        this.currency = currency.getCurrencyCode();
    }
    
    public Money(BigDecimal amount, String currencyCode) {
        this(amount, Currency.getInstance(currencyCode));
    }
    
    /**
     * Factory method for creating Money with USD currency.
     */
    public static Money usd(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("USD"));
    }
    
    /**
     * Factory method for creating Money with KRW currency.
     */
    public static Money krw(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("KRW"));
    }
    
    /**
     * Factory method for creating Money from string amount.
     */
    public static Money of(String amount, String currencyCode) {
        return new Money(new BigDecimal(amount), currencyCode);
    }
    
    /**
     * Adds another Money amount to this one.
     * Both amounts must have the same currency.
     */
    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), Currency.getInstance(this.currency));
    }
    
    /**
     * Subtracts another Money amount from this one.
     * Both amounts must have the same currency.
     */
    public Money subtract(Money other) {
        validateSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Result cannot be negative");
        }
        return new Money(result, Currency.getInstance(this.currency));
    }
    
    /**
     * Multiplies this Money by a factor.
     */
    public Money multiply(BigDecimal factor) {
        if (factor == null) {
            throw new IllegalArgumentException("Factor cannot be null");
        }
        return new Money(this.amount.multiply(factor), Currency.getInstance(this.currency));
    }
    
    /**
     * Checks if this Money is greater than another.
     */
    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }
    
    /**
     * Checks if this Money is greater than or equal to another.
     */
    public boolean isGreaterThanOrEqual(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) >= 0;
    }
    
    /**
     * Checks if this Money is zero.
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    
    /**
     * Gets the BigDecimal amount.
     */
    public BigDecimal getAmount() {
        return amount;
    }
    
    /**
     * Gets the currency code.
     */
    public String getCurrency() {
        return currency;
    }
    
    /**
     * Gets the Currency object.
     */
    public Currency getCurrencyObject() {
        return Currency.getInstance(currency);
    }
    
    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Cannot perform operation on different currencies: " + 
                this.currency + " and " + other.currency
            );
        }
    }
    
    @Override
    public String toString() {
        return amount + " " + currency;
    }
}