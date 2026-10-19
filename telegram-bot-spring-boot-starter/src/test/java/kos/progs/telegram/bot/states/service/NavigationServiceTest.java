package kos.progs.telegram.bot.states.service;

import kos.progs.telegram.bot.states.repository.memory.MemoryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NavigationServiceTest {
    private NavigationService navigationService;

    @BeforeEach
    void setUp() {
        navigationService = new NavigationService(new MemoryHistoryRepository());
    }

    @Test
    void testNavigateAndRollback() {
        long chatId = 100L;
        navigationService.navigateTo(chatId, "/start");
        navigationService.navigateTo(chatId, "/catalog");
        navigationService.navigateTo(chatId, "/item-1");

        assertEquals("/item-1", navigationService.peakHistory(chatId));

        String back = navigationService.goBack(chatId, 1);
        assertEquals("/catalog", back);
        assertEquals("/catalog", navigationService.peakHistory(chatId));

        String back2 = navigationService.goBack(chatId, 1);
        assertEquals("/start", back2);

        String back3 = navigationService.goBack(chatId, 1);
        assertEquals("/start", back3); // fallback when stack empty
    }

    @Test
    void testDeduplication() {
        long chatId = 200L;
        navigationService.navigateTo(chatId, "/catalog");
        navigationService.navigateTo(chatId, "/catalog");
        navigationService.navigateTo(chatId, "/catalog");

        assertEquals("/catalog", navigationService.peakHistory(chatId));
        String back = navigationService.goBack(chatId, 1);
        assertEquals("/start", back); // only 1 entry was pushed due to deduplication
    }
}
