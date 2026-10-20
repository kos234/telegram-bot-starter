package kos.progs.telegram.bot.settings.button;

import kos.progs.telegram.bot.button.handler.ButtonHandler;
import kos.progs.telegram.bot.button.model.PrimitiveArgs;
import kos.progs.telegram.bot.common.model.Lang;
import kos.progs.telegram.bot.core.model.HandleResult;
import kos.progs.telegram.bot.settings.presenter.SettingsPresenter;
import kos.progs.telegram.bot.user.model.UserInfo;
import kos.progs.telegram.bot.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.nio.ByteBuffer;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class ChangeLanguageButton implements ButtonHandler {
    private final UserInfoService userInfoService;
    private final SettingsPresenter settingsPresenter;
    private final ChangeLangCommand changeLangCommand;

    @Override
    public boolean isHandle(String command) {
        return command.equals(changeLangCommand.getBaseCommand());
    }

    @Override
    public HandleResult handle(CallbackQuery callbackQuery, UserInfo userInfo, String command, ByteBuffer rawArgs) throws TelegramApiException {
        var args = new PrimitiveArgs<>(Byte.class);
        args.fromByteBuffer(rawArgs);

        var newLang = Lang.valueOf(args.getValue());
        if (Objects.equals(newLang, userInfo.lang())) {
            return HandleResult.unhandled();
        }

        userInfoService.updateLang(callbackQuery.getFrom().getId(), newLang);

        return settingsPresenter.presentSettings(
                userInfo.withLang(newLang),
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId()
        );
    }
}
