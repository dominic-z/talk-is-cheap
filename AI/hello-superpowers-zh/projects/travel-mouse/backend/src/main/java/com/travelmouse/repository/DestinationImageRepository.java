package com.travelmouse.repository;

import com.travelmouse.entity.DestinationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DestinationImageRepository extends JpaRepository<DestinationImage, Long> {
    List<DestinationImage> findByDestinationId(Long destinationId);
    void deleteByDestinationId(Long destinationId);
}
