package dev.jialiang.personalsite.repository;

import dev.jialiang.personalsite.entity.FinanceNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FinanceNewsRepository extends JpaRepository<FinanceNews, Long> {
    Optional<FinanceNews> findByUrl(String url);

    @Modifying
    @Transactional
    @Query("DELETE FROM FinanceNews n WHERE n.createdAt < :before")
    int deleteOlderThan(LocalDateTime before);
}
