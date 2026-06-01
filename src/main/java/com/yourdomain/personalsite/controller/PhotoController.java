package com.yourdomain.personalsite.controller;

import com.yourdomain.personalsite.dto.ApiResponse;
import com.yourdomain.personalsite.entity.Photo;
import com.yourdomain.personalsite.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    @GetMapping
    public ApiResponse<Page<Photo>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.success(photoService.list(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "takenDate"))
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<Photo> getById(@PathVariable Long id) {
        return photoService.getById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "照片不存在"));
    }

    @PostMapping
    public ApiResponse<Photo> create(@RequestBody Photo photo) {
        return ApiResponse.success(photoService.create(photo));
    }

    @PutMapping("/{id}")
    public ApiResponse<Photo> update(@PathVariable Long id, @RequestBody Photo photo) {
        return ApiResponse.success(photoService.update(id, photo));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        photoService.delete(id);
        return ApiResponse.success(null);
    }
}
