package dev.jialiang.personalsite.service;

import dev.jialiang.personalsite.entity.FinanceNews;
import dev.jialiang.personalsite.repository.FinanceNewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceNewsService {

    private final FinanceNewsRepository newsRepository;

    public List<FinanceNews> getLatestNews(int limit) {
        return newsRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt"))
        ).getContent();
    }

    public boolean existsByUrl(String url) {
        return newsRepository.findByUrl(url).isPresent();
    }

    public FinanceNews save(FinanceNews news) {
        return newsRepository.findByUrl(news.getUrl())
                .orElseGet(() -> newsRepository.save(news));
    }

    public int deleteOlderThan(LocalDateTime before) {
        return newsRepository.deleteOlderThan(before);
    }
}
