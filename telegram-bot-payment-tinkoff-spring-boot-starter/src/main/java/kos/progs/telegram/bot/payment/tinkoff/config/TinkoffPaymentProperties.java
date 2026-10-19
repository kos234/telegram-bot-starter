package kos.progs.telegram.bot.payment.tinkoff.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot.payment.tinkoff")
@Getter
@Setter
public class TinkoffPaymentProperties {
    private boolean enabled = true;
    private String serverUrl;
    private String terminalKey;
    private String password;
    private String debugTerminalKey;
    private String debugPassword;
    private int debugMode = 0;
    private int debugTinkoffMode = 0;
    private String storage = "memory";
}
