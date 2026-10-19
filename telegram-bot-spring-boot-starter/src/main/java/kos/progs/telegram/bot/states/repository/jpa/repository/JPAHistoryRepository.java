package kos.progs.telegram.bot.states.repository.jpa.repository;

import kos.progs.telegram.bot.states.repository.jpa.entity.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JPAHistoryRepository extends JpaRepository<HistoryEntity, Long> {
}
