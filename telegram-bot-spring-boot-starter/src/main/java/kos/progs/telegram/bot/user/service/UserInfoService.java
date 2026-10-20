package kos.progs.telegram.bot.user.service;

import kos.progs.telegram.bot.common.model.Currency;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.repository.IUserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserInfoService {
    protected final IUserInfoRepository userInfoRepository;
    protected final DefaultUserInfoResolver defaultUserInfoResolver;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,15}$");

    @Autowired
    public UserInfoService(IUserInfoRepository userInfoRepository, DefaultUserInfoResolver defaultUserInfoResolver) {
        this.userInfoRepository = Objects.requireNonNull(userInfoRepository, "userInfoRepository must not be null");
        this.defaultUserInfoResolver = defaultUserInfoResolver != null
                ? defaultUserInfoResolver
                : new StandardDefaultUserInfoResolver(null);
    }

    public UserInfoService(IUserInfoRepository userInfoRepository) {
        this(userInfoRepository, new StandardDefaultUserInfoResolver(null));
    }

    public UserInfo getUserInfo(long userId, String systemLang) {
        var res = userInfoRepository.getUserInfo(userId);
        if (res.isEmpty()) {
            UserInfo userInfo = createDefaultUserInfo(userId, systemLang);
            userInfoRepository.setUserInfo(userInfo);
            res = Optional.of(userInfo);
        }
        return res.get();
    }

    protected UserInfo createDefaultUserInfo(long userId, String systemLang) {
        return defaultUserInfoResolver.createDefaultUserInfo(userId, systemLang);
    }

    public UserInfo updateLang(long userId, Lang lang) {
        var wrapper = userInfoRepository.getUserInfo(userId);
        if (wrapper.isEmpty()) {
            return null;
        }
        var newUserInfo = wrapper.get().withLang(lang);
        userInfoRepository.setUserInfo(newUserInfo);
        return newUserInfo;
    }

    public UserInfo updateCurrency(long userId, Currency currency) {
        var wrapper = userInfoRepository.getUserInfo(userId);
        if (wrapper.isEmpty()) {
            return null;
        }
        var newUserInfo = wrapper.get().withCurrency(currency);
        userInfoRepository.setUserInfo(newUserInfo);
        return newUserInfo;
    }

    public UserInfo updateEmail(long userId, String email) {
        var wrapper = userInfoRepository.getUserInfo(userId);
        if (wrapper.isEmpty() || !isValidEmail(email)) {
            return null;
        }
        var newUserInfo = wrapper.get().withEmail(email);
        userInfoRepository.setUserInfo(newUserInfo);
        return newUserInfo;
    }

    public UserInfo updatePhone(long userId, String phone) {
        var wrapper = userInfoRepository.getUserInfo(userId);
        if (wrapper.isEmpty()) {
            return null;
        }
        var normalPhone = normalizeAndValidatePhone(phone);
        if (normalPhone == null) {
            return null;
        }
        var newUserInfo = wrapper.get().withPhone(normalPhone);
        userInfoRepository.setUserInfo(newUserInfo);
        return newUserInfo;
    }

    public void setUserInfo(long userId, UserInfo userInfo) {
        userInfoRepository.setUserInfo(userInfo);
    }

    public void setUserInfo(UserInfo userInfo) {
        userInfoRepository.setUserInfo(userInfo);
    }

    public String normalizeAndValidatePhone(String rawPhone) {
        if (rawPhone == null) return null;

        String cleanDigits = rawPhone.replaceAll("\\D", "");

        if (cleanDigits.length() == 11 && cleanDigits.startsWith("8")) {
            cleanDigits = "7" + cleanDigits.substring(1);
        }

        if (cleanDigits.length() < 7 || cleanDigits.length() > 15) {
            return null;
        }

        return cleanDigits;
    }

    public String formatPhoneNumber(String cleanDigits) {
        if (cleanDigits == null || cleanDigits.isEmpty()) return "";
        int len = cleanDigits.length();

        if (len < 10) {
            return "+" + cleanDigits;
        }

        String tail = cleanDigits.substring(len - 2);
        String subTail = cleanDigits.substring(len - 4, len - 2);
        String body = cleanDigits.substring(len - 7, len - 4);
        String prefixPart = cleanDigits.substring(0, len - 7);

        String countryCode;
        String areaCode;

        if (len == 11) {
            countryCode = prefixPart.substring(0, 1);
            areaCode = prefixPart.substring(1);
        } else if (len == 12) {
            countryCode = prefixPart.substring(0, 3);
            areaCode = prefixPart.substring(3);
        } else {
            int splitIndex = Math.min(3, Math.max(1, prefixPart.length() - 2));
            countryCode = prefixPart.substring(0, splitIndex);
            areaCode = prefixPart.substring(splitIndex);
        }

        return String.format("+%s (%s) %s-%s-%s", countryCode, areaCode, body, subTail, tail);
    }

    public boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
