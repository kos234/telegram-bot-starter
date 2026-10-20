package kos.progs.telegram.bot.user.repository.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kos.progs.telegram.bot.common.model.Lang;

@Converter(autoApply = true)
public class LangJpaConverter implements AttributeConverter<Lang, String> {

    @Override
    public String convertToDatabaseColumn(Lang attribute) {
        return attribute != null ? attribute.getCode() : null;
    }

    @Override
    public Lang convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return Lang.saveValueOf(dbData);
    }
}
