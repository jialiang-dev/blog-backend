package com.yourdomain.personalsite.controller;

import com.yourdomain.personalsite.dto.ApiResponse;
import com.yourdomain.personalsite.entity.Essay;
import com.yourdomain.personalsite.service.EssayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/essays")
@RequiredArgsConstructor
public class EssayController {

    private final EssayService essayService;

    @GetMapping
    public ApiResponse<Page<Essay>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(essayService.list(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<Essay> getById(@PathVariable Long id) {
        return essayService.getById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "随笔不存在"));
    }

    @PostMapping
    public ApiResponse<Essay> create(@RequestBody Essay essay) {
        return ApiResponse.success(essayService.create(essay));
    }

    @PutMapping("/{id}")
    public ApiResponse<Essay> update(@PathVariable Long id, @RequestBody Essay essay) {
        return ApiResponse.success(essayService.update(id, essay));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        essayService.delete(id);
        return ApiResponse.success(null);
    }
}
