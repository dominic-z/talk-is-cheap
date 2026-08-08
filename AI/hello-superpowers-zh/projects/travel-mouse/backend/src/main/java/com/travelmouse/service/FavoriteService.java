package com.travelmouse.service;

import com.travelmouse.entity.Favorite;
import com.travelmouse.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    public List<Favorite> findAll() {
        return favoriteRepository.findAll();
    }

    public Favorite create(Favorite favorite) {
        return favoriteRepository.save(favorite);
    }

    public Favorite update(Long id, Favorite updated) {
        Favorite fav = favoriteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("收藏不存在: " + id));
        fav.setName(updated.getName());
        fav.setAddress(updated.getAddress());
        fav.setLongitude(updated.getLongitude());
        fav.setLatitude(updated.getLatitude());
        fav.setCategory(updated.getCategory());
        fav.setNote(updated.getNote());
        return favoriteRepository.save(fav);
    }

    public void delete(Long id) {
        Favorite fav = favoriteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("收藏不存在: " + id));
        favoriteRepository.delete(fav);
    }
}
