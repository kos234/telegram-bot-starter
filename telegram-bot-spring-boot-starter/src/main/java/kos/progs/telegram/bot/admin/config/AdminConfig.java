package kos.progs.telegram.bot.admin.config;

import kos.progs.telegram.bot.admin.button.AdminsCommand;
import kos.progs.telegram.bot.admin.button.AdminsManipulateButton;
import kos.progs.telegram.bot.admin.button.AdminsViewListButton;
import kos.progs.telegram.bot.admin.command.AdminPanelCommand;
import kos.progs.telegram.bot.admin.presenter.TelegramAdminsPresenter;
import kos.progs.telegram.bot.admin.repository.AdminsRepository;
import kos.progs.telegram.bot.admin.repository.jpa.adapters.AdminRepositoryAdapter;
import kos.progs.telegram.bot.admin.repository.jpa.repository.JPAAdminUserRepository;
import kos.progs.telegram.bot.admin.repository.memory.MemoryAdminsRepository;
import kos.progs.telegram.bot.admin.service.AdminService;
import kos.progs.telegram.bot.admin.state.AdminsManipulateState;
import kos.progs.telegram.bot.i18n.service.MessagesService;
import kos.progs.telegram.bot.spi.AdminMenuItemProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Configuration
public class AdminConfig {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "telegram.bot.admin-storage", havingValue = "memory", matchIfMissing = true)
    public static class MemoryAdminStorageConfiguration {
        @Bean
        @ConditionalOnMissingBean(AdminsRepository.class)
        public AdminsRepository memoryAdminsRepository() {
            return new MemoryAdminsRepository();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(JPAAdminUserRepository.class)
    @ConditionalOnProperty(name = "telegram.bot.admin-storage", havingValue = "jpa")
    public static class JpaAdminStorageConfiguration {
        @Bean
        @ConditionalOnBean(JPAAdminUserRepository.class)
        @ConditionalOnMissingBean(AdminsRepository.class)
        public AdminsRepository jpaAdminRepositoryAdapter(JPAAdminUserRepository jpaAdminUserRepository) {
            return new AdminRepositoryAdapter(jpaAdminUserRepository);
        }
    }

    @Bean
    @ConditionalOnMissingBean(AdminService.class)
    public AdminService adminService(AdminsRepository adminsRepository) {
        return new AdminService(adminsRepository);
    }

    @Bean
    @ConditionalOnMissingBean(AdminsCommand.class)
    public AdminsCommand adminsCommand() {
        return new AdminsCommand();
    }

    @Bean
    @ConditionalOnMissingBean(TelegramAdminsPresenter.class)
    public TelegramAdminsPresenter telegramAdminsPresenter(AdminService adminService,
                                                           AdminsCommand adminsCommand,
                                                           MessagesService messagesService,
                                                           TelegramClient telegramClient) {
        return new TelegramAdminsPresenter(adminService, adminsCommand, messagesService, telegramClient);
    }

    @Bean
    @ConditionalOnMissingBean(AdminPanelCommand.class)
    public AdminPanelCommand adminPanelCommand(MessagesService messagesService,
                                               AdminService adminService,
                                               TelegramClient telegramClient,
                                               AdminsCommand adminsCommand,
                                               @Autowired(required = false) List<AdminMenuItemProvider> menuItemProviders) {
        return new AdminPanelCommand(messagesService, adminService, telegramClient, adminsCommand, menuItemProviders);
    }

    @Bean
    @ConditionalOnMissingBean(AdminsManipulateButton.class)
    public AdminsManipulateButton adminsManipulateButton(AdminsCommand adminsCommand,
                                                         MessagesService messagesService,
                                                         AdminService adminService,
                                                         TelegramClient telegramClient) {
        return new AdminsManipulateButton(adminsCommand, messagesService, adminService, telegramClient);
    }

    @Bean
    @ConditionalOnMissingBean(AdminsViewListButton.class)
    public AdminsViewListButton adminsViewListButton(AdminsCommand adminsCommand,
                                                     TelegramAdminsPresenter telegramAdminsPresenter) {
        return new AdminsViewListButton(adminsCommand, telegramAdminsPresenter);
    }

    @Bean
    @ConditionalOnMissingBean(AdminsManipulateState.class)
    public AdminsManipulateState adminsManipulateState(MessagesService messagesService,
                                                       TelegramAdminsPresenter telegramAdminsPresenter,
                                                       AdminService adminService,
                                                       TelegramClient telegramClient) {
        return new AdminsManipulateState(messagesService, telegramAdminsPresenter, adminService, telegramClient);
    }
}
