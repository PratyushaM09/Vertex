package com.vertexplacements.trackerapi.repository;

import com.vertexplacements.trackerapi.entity.SavedCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SavedCompanyRepository extends JpaRepository<SavedCompany, Long> {

    @Query("SELECT s FROM SavedCompany s JOIN FETCH s.company c " +
            "WHERE s.user.id = :userId AND c.deletedAt IS NULL ORDER BY s.savedAt DESC")
    List<SavedCompany> findActiveByUserId(@Param("userId") Long userId);

    Optional<SavedCompany> findByUserIdAndCompanyId(Long userId, Long companyId);
}