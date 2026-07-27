package com.travelmouse.service;

import com.travelmouse.entity.Destination;
import com.travelmouse.entity.DestinationImage;
import com.travelmouse.repository.DestinationImageRepository;
import com.travelmouse.repository.DestinationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class DestinationService {
    private final DestinationRepository destinationRepository;
    private final DestinationImageRepository imageRepository;
    private final DailyPlanService dailyPlanService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    public DestinationService(DestinationRepository destinationRepository,
                              DestinationImageRepository imageRepository,
                              DailyPlanService dailyPlanService) {
        this.destinationRepository = destinationRepository;
        this.imageRepository = imageRepository;
        this.dailyPlanService = dailyPlanService;
    }

    public List<Destination> findByDailyPlanId(Long dailyPlanId) {
        return destinationRepository.findByDailyPlanIdOrderBySortOrder(dailyPlanId);
    }

    @Transactional
    public Destination create(Long dailyPlanId, Destination dest) {
        dailyPlanService.findById(dailyPlanId); // 验证存在
        dest.setDailyPlanId(dailyPlanId);
        List<Destination> existing = destinationRepository.findByDailyPlanIdOrderBySortOrder(dailyPlanId);
        dest.setSortOrder(existing.size());
        return destinationRepository.save(dest);
    }

    @Transactional
    public Destination update(Long id, Destination updated) {
        Destination dest = destinationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("目的地不存在: " + id));
        if (updated.getName() != null) dest.setName(updated.getName());
        if (updated.getNoteText() != null) dest.setNoteText(updated.getNoteText());
        if (updated.getArriveTime() != null) dest.setArriveTime(updated.getArriveTime());
        if (updated.getDurationMinutes() != null) dest.setDurationMinutes(updated.getDurationMinutes());
        if (updated.getLeaveTime() != null) dest.setLeaveTime(updated.getLeaveTime());
        if (updated.getInRoute() != null) dest.setInRoute(updated.getInRoute());
        if (updated.getRouteOrder() != null) dest.setRouteOrder(updated.getRouteOrder());
        return destinationRepository.save(dest);
    }

    @Transactional
    public void delete(Long id) {
        Destination dest = destinationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("目的地不存在: " + id));
        // 业务层级联：先删图片
        imageRepository.deleteByDestinationId(id);
        destinationRepository.delete(dest);
    }

    public List<DestinationImage> findImages(Long destinationId) {
        return imageRepository.findByDestinationId(destinationId);
    }

    @Transactional
    public DestinationImage uploadImage(Long destinationId, MultipartFile file) throws IOException {
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("仅支持 jpg/png/webp 格式");
        }
        destinationRepository.findById(destinationId)
                .orElseThrow(() -> new NoSuchElementException("目的地不存在: " + destinationId));

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".jpg";
        String filename = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Path filePath = dir.resolve(filename);
        file.transferTo(filePath.toFile());

        DestinationImage img = new DestinationImage();
        img.setDestinationId(destinationId);
        img.setFilePath("/uploads/" + filename);
        return imageRepository.save(img);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        DestinationImage img = imageRepository.findById(imageId)
                .orElseThrow(() -> new NoSuchElementException("图片不存在: " + imageId));
        imageRepository.delete(img);
    }
}
