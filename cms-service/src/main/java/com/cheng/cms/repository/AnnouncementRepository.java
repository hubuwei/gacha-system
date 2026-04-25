package com.cheng.cms.repository;

import com.cheng.cms.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    Page<Announcement> findByIsActive(Boolean isActive, Pageable pageable);
    List<Announcement> findByIsActiveAndType(Boolean isActive, String type);
}
