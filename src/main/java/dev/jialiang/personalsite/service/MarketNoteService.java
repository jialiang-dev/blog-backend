package dev.jialiang.personalsite.service;

import dev.jialiang.personalsite.entity.MarketNote;
import dev.jialiang.personalsite.repository.MarketNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketNoteService {

    private final MarketNoteRepository repository;

    public List<MarketNote> listAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"));
    }

    public Optional<MarketNote> getById(Long id) {
        return repository.findById(id);
    }

    public MarketNote create(MarketNote note) {
        if (note.getColor() == null) note.setColor("gray");
        return repository.save(note);
    }

    public MarketNote update(Long id, MarketNote note) {
        MarketNote existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("便利贴不存在: " + id));
        if (note.getTitle() != null) existing.setTitle(note.getTitle());
        if (note.getContent() != null) existing.setContent(note.getContent());
        if (note.getAssetCode() != null) existing.setAssetCode(note.getAssetCode());
        if (note.getColor() != null) existing.setColor(note.getColor());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
