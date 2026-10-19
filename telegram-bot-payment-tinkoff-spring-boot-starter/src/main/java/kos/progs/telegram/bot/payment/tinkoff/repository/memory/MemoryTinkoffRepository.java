package kos.progs.telegram.bot.payment.tinkoff.repository.memory;

import kos.progs.telegram.bot.payment.tinkoff.dto.TinkofURLDTO;
import kos.progs.telegram.bot.payment.tinkoff.repository.TinkoffRepository;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryTinkoffRepository implements TinkoffRepository {
    private record CachedUrl(TinkofURLDTO urls, ZonedDateTime expTime) {}

    private final Map<Long, CachedUrl> cache = new ConcurrentHashMap<>();

    @Override
    public void savePaymentId(long orderId, TinkofURLDTO urls, ZonedDateTime expTime) {
        if (urls != null) {
            cache.put(orderId, new CachedUrl(urls, expTime));
        }
    }

    @Override
    public Optional<TinkofURLDTO> getActiveUrl(long orderId) {
        CachedUrl cached = cache.get(orderId);
        if (cached == null) {
            return Optional.empty();
        }
        if (cached.expTime() != null && ZonedDateTime.now().isAfter(cached.expTime())) {
            cache.remove(orderId);
            return Optional.empty();
        }
        return Optional.of(cached.urls());
    }
}
