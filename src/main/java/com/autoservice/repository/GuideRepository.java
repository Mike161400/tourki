package com.autoservice.repository;

import com.autoservice.domain.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GuideRepository extends JpaRepository<Guide, Long> {
    List<Guide> findByActiveTrue();

    boolean existsByEmail(String email);

    @Query("SELECT g FROM Guide g WHERE g.active = true ORDER BY (SELECT COUNT(t) FROM Tour t WHERE t.guide = g AND t.status IN ('OPEN_FOR_BOOKING', 'IN_PROGRESS')) ASC")
    List<Guide> findActiveGuidesByWorkloadAsc();

    @Query("SELECT COUNT(t) FROM Tour t WHERE t.guide.id = :guideId AND t.status IN ('OPEN_FOR_BOOKING', 'IN_PROGRESS')")
    long countActiveToursByGuide(@Param("guideId") Long guideId);
}
