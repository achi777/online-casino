package com.casino.repository;

import com.casino.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByStatusOrderByDisplayOrderAsc(Banner.BannerStatus status);
    List<Banner> findByPositionAndStatusOrderByDisplayOrderAsc(Banner.BannerPosition position, Banner.BannerStatus status);
}
