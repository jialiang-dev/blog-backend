package com.yourdomain.personalsite.service;

import com.yourdomain.personalsite.entity.Photo;
import com.yourdomain.personalsite.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;

    public List<Photo> listAll() {
        return photoRepository.findAll(Sort.by(Sort.Direction.DESC, "takenDate"));
    }

    public Page<Photo> list(Pageable pageable) {
        return photoRepository.findAll(pageable);
    }

    public Optional<Photo> getById(Long id) {
        return photoRepository.findById(id);
    }

    public Photo create(Photo photo) {
        return photoRepository.save(photo);
    }

    public Photo update(Long id, Photo photo) {
        Photo existing = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("照片不存在: " + id));
        existing.setTitle(photo.getTitle());
        existing.setDescription(photo.getDescription());
        existing.setUrl(photo.getUrl());
        existing.setThumbnailUrl(photo.getThumbnailUrl());
        existing.setTakenDate(photo.getTakenDate());
        return photoRepository.save(existing);
    }

    public void delete(Long id) {
        if (!photoRepository.existsById(id)) {
            throw new RuntimeException("照片不存在: " + id);
        }
        photoRepository.deleteById(id);
    }
}
