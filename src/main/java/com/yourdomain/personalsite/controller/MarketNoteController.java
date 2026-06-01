package com.yourdomain.personalsite.controller;

import com.yourdomain.personalsite.dto.ApiResponse;
import com.yourdomain.personalsite.entity.MarketNote;
import com.yourdomain.personalsite.service.MarketNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market-notes")
@RequiredArgsConstructor
public class MarketNoteController {

    private final MarketNoteService service;

    @GetMapping
    public ApiResponse<List<MarketNote>> list() {
        return ApiResponse.success(service.listAll());
    }

    @PostMapping
    public ApiResponse<MarketNote> create(@RequestBody MarketNote note) {
        return ApiResponse.success(service.create(note));
    }

    @PutMapping("/{id}")
    public ApiResponse<MarketNote> update(@PathVariable Long id, @RequestBody MarketNote note) {
        return ApiResponse.success(service.update(id, note));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
