package com.vertexplacements.trackerapi.repository;

import com.vertexplacements.trackerapi.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Companies are shared across the whole college — every authenticated user (student or
 * officer) sees the same list. Only Placement Officers can create/edit/delete them
 * (enforced at the controller layer), so none of these queries are scoped by owner.
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByDeletedAtIsNull();

    Optional<Company> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT c FROM Company c " +
            "WHERE c.deletedAt IS NULL " +
            "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND c.ctc >= :minCtc " +
            "ORDER BY c.visitDate DESC")
    List<Company> findByFilters(@Param("name") String name, @Param("minCtc") Double minCtc);

    @Query("SELECT MAX(c.ctc) FROM Company c WHERE c.deletedAt IS NULL")
    Optional<Double> findHighestCtc();

    long countByDeletedAtIsNullAndVisitDateGreaterThanEqual(LocalDate date);

    long countByDeletedAtIsNull();

    List<Company> findByDeletedAtIsNotNull();
}