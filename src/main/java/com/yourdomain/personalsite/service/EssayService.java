package com.yourdomain.personalsite.service;

import com.yourdomain.personalsite.entity.Essay;
import com.yourdomain.personalsite.repository.EssayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EssayService {

    private final EssayRepository essayRepository;

    public Page<Essay> list(Pageable pageable) {
        return essayRepository.findAll(pageable);
    }

    public Optional<Essay> getById(Long id) {
        return essayRepository.findById(id);
    }

    public Essay create(Essay essay) {
        return essayRepository.save(essay);
    }

    public Essay update(Long id, Essay essay) {
        Essay existing = essayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("随笔不存在: " + id));
        existing.setTitle(essay.getTitle());
        existing.setContent(essay.getContent());
        existing.setSummary(essay.getSummary());
        existing.setCoverImageUrl(essay.getCoverImageUrl());
        return essayRepository.save(existing);
    }

    public void delete(Long id) {
        if (!essayRepository.existsById(id)) {
            throw new RuntimeException("随笔不存在: " + id);
        }
        essayRepository.deleteById(id);
    }
}
