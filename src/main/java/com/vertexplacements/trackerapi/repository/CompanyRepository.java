package com.vertexplacements.trackerapi.repository;

import com.vertexplacements.trackerapi.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByOwnerIdAndDeletedAtIsNull(Long ownerId);

    Optional<Company> findByIdAndOwnerIdAndDeletedAtIsNull(Long id, Long ownerId);

    @Query("SELECT c FROM Company c " +
            "WHERE c.owner.id = :ownerId AND c.deletedAt IS NULL " +
            "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND c.ctc >= :minCtc " +
            "ORDER BY c.visitDate DESC")
    List<Company> findByFilters(@Param("ownerId") Long ownerId, @Param("name") String name, @Param("minCtc") Double minCtc);

    @Query("SELECT MAX(c.ctc) FROM Company c WHERE c.owner.id = :ownerId AND c.deletedAt IS NULL")
    Optional<Double> findHighestCtcByOwnerId(@Param("ownerId") Long ownerId);

    long countByOwnerIdAndDeletedAtIsNullAndVisitDateGreaterThanEqual(Long ownerId, LocalDate date);

    long countByOwnerIdAndDeletedAtIsNull(Long ownerId);

    List<Company> findByOwnerIdAndDeletedAtIsNotNull(Long ownerId);

    /** Lookup regardless of deleted state — needed for restore / permanent delete. */
    Optional<Company> findByIdAndOwnerId(Long id, Long ownerId);
}