package kos.progs.telegram.bot.common.config;

import jakarta.annotation.PostConstruct;
import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.config.TelegramBotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Slf4j
public class CustomLangAndCurrencyInitializer {
    private final TelegramBotProperties properties;
    private final List<Lang> customLangBeans;
    private final List<Currency> customCurrencyBeans;

    @PostConstruct
    public void init() {
        if (customLangBeans != null) {
            for (Lang lang : customLangBeans) {
                Lang.register(lang);
                log.info("Registered custom Lang bean: {} ({})", lang.getCode(), lang.getTitle());
            }
        }

        if (customCurrencyBeans != null) {
            for (Currency currency : customCurrencyBeans) {
                Currency.register(currency);
                log.info("Registered custom Currency bean: {} ({})", currency.getCode(), currency.getTitle());
            }
        }

        if (properties != null) {
            if (properties.getLanguage() != null && properties.getLanguage().getCustom() != null) {
                for (var customLang : properties.getLanguage().getCustom()) {
                    Locale locale = customLang.getTag() != null && !customLang.getTag().isBlank()
                            ? Locale.forLanguageTag(customLang.getTag())
                            : Locale.forLanguageTag(customLang.getCode().toLowerCase());
                    Lang lang = new Lang(customLang.getId(), customLang.getCode(), locale, customLang.getTitle());
                    Lang.register(lang);
                    log.info("Registered custom Lang from properties: {} ({})", lang.getCode(), lang.getTitle());
                }
            }

            if (properties.getCurrency() != null && properties.getCurrency().getCustom() != null) {
                for (var customCurrency : properties.getCurrency().getCustom()) {
                    Currency currency = new Currency(customCurrency.getId(), customCurrency.getCode(), customCurrency.getTitle());
                    Currency.register(currency);
                    log.info("Registered custom Currency from properties: {} ({})", currency.getCode(), currency.getTitle());
                }
            }
        }
    }
}
