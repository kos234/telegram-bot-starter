package kos.progs.telegram.bot.user.repository.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kos.progs.telegram.bot.common.model.Currency;

@Converter(autoApply = true)
public class CurrencyJpaConverter implements AttributeConverter<Currency, String> {

    @Override
    public String convertToDatabaseColumn(Currency attribute) {
        return attribute != null ? attribute.getCode() : null;
    }

    @Override
    public Currency convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return Currency.saveValueOf(dbData);
    }
}
