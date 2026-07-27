package com.travelmouse.controller;

import com.travelmouse.dto.ApiResponse;
import com.travelmouse.entity.Favorite;
import com.travelmouse.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ApiResponse<List<Favorite>> list() {
        return ApiResponse.success(favoriteService.findAll());
    }

    @PostMapping
    public ApiResponse<Favorite> create(@RequestBody Favorite favorite) {
        return ApiResponse.success(favoriteService.create(favorite));
    }

    @PutMapping("/{id}")
    public ApiResponse<Favorite> update(@PathVariable Long id, @RequestBody Favorite favorite) {
        return ApiResponse.success(favoriteService.update(id, favorite));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        favoriteService.delete(id);
        return ApiResponse.success(null);
    }
}
