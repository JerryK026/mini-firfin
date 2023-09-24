package com.soko.minifirfin;

import com.soko.minifirfin.domain.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, String> {

    @Override
    public String convertToDatabaseColumn(Money attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toString();
    }

    @Override
    public Money convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        } else {
            String value = dbData.substring(0, dbData.length() - 3);
            String currency = dbData.substring(dbData.length() - 3);
            return new Money(new BigDecimal(value), currency);
        }
    }
}