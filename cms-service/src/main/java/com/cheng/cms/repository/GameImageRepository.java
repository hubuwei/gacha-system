package com.cheng.cms.repository;

import com.cheng.cms.entity.GameImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameImageRepository extends JpaRepository<GameImage, Long> {
    List<GameImage> findByGameIdOrderBySortOrderAsc(Long gameId);
    void deleteByGameId(Long gameId);
}
