package dev.jialiang.personalsite.repository;

import dev.jialiang.personalsite.entity.MarketNote;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketNoteRepository extends JpaRepository<MarketNote, Long> {
    List<MarketNote> findAll(Sort sort);
}
