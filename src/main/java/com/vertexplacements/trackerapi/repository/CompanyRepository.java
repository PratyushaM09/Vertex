package com.vertexplacements.trackerapi.repository;

import com.vertexplacements.trackerapi.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByOwnerId(Long ownerId);

    Optional<Company> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Day 4 requirement: custom JPQL filtering query, scoped to the current user's own data.
     * Mirrors the frontend's search box (name contains, case-insensitive)
     * combined with the "Minimum CTC" range slider.
     */
    @Query("SELECT c FROM Company c " +
           "WHERE c.owner.id = :ownerId " +
           "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND c.ctc >= :minCtc " +
           "ORDER BY c.visitDate DESC")
    List<Company> findByFilters(@Param("ownerId") Long ownerId, @Param("name") String name, @Param("minCtc") Double minCtc);

    @Query("SELECT MAX(c.ctc) FROM Company c WHERE c.owner.id = :ownerId")
    Optional<Double> findHighestCtcByOwnerId(@Param("ownerId") Long ownerId);

    long countByOwnerIdAndVisitDateGreaterThanEqual(Long ownerId, LocalDate date);

    long countByOwnerId(Long ownerId);
}
