package com.travelmouse.controller;

import com.travelmouse.dto.ApiResponse;
import com.travelmouse.entity.Destination;
import com.travelmouse.entity.DestinationImage;
import com.travelmouse.service.DestinationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/days/{dayId}/destinations")
public class DestinationController {
    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public ApiResponse<List<Destination>> list(@PathVariable Long dayId) {
        return ApiResponse.success(destinationService.findByDailyPlanId(dayId));
    }

    @PostMapping
    public ApiResponse<Destination> create(@PathVariable Long dayId, @RequestBody Destination dest) {
        return ApiResponse.success(destinationService.create(dayId, dest));
    }

    @PutMapping("/{id}")
    public ApiResponse<Destination> update(@PathVariable Long dayId, @PathVariable Long id,
                                           @RequestBody Destination dest) {
        return ApiResponse.success(destinationService.update(id, dest));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long dayId, @PathVariable Long id) {
        destinationService.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/images")
    public ApiResponse<List<DestinationImage>> listImages(@PathVariable Long dayId, @PathVariable Long id) {
        return ApiResponse.success(destinationService.findImages(id));
    }

    @PostMapping("/{id}/images")
    public ApiResponse<DestinationImage> uploadImage(@PathVariable Long dayId, @PathVariable Long id,
                                                     @RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.success(destinationService.uploadImage(id, file));
    }

    @DeleteMapping("/{id}/images/{imgId}")
    public ApiResponse<Void> deleteImage(@PathVariable Long dayId, @PathVariable Long id,
                                         @PathVariable Long imgId) {
        destinationService.deleteImage(imgId);
        return ApiResponse.success(null);
    }
}
