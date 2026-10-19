package kos.progs.telegram.bot.states.repository.jpa.repository;

import kos.progs.telegram.bot.states.repository.jpa.entity.StateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JPAStateRepository extends JpaRepository<StateEntity, Long> {
}
