package com.tourly.common.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter that maps Boolean <-> CHAR(1) 'Y'/'N' in the database.
 * true  → 'Y'
 * false → 'N'
 */
@Converter
public class YNBooleanConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return "N";
        }
        return attribute ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return false;
        }
        return "Y".equalsIgnoreCase(dbData.trim());
    }
}
