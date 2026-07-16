package com.vertexplacements.trackerapi.repository;

import com.vertexplacements.trackerapi.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("SELECT a FROM Application a JOIN FETCH a.company " +
            "WHERE a.owner.id = :ownerId AND a.deletedAt IS NULL " +
            "ORDER BY a.applyDate DESC, a.id DESC")
    List<Application> findAllWithCompanyByOwnerId(@Param("ownerId") Long ownerId);

    Optional<Application> findByIdAndOwnerIdAndDeletedAtIsNull(Long id, Long ownerId);

    @Query("SELECT a FROM Application a JOIN FETCH a.company " +
            "WHERE a.owner.id = :ownerId AND a.deletedAt IS NOT NULL " +
            "ORDER BY a.deletedAt DESC")
    List<Application> findDeletedByOwnerId(@Param("ownerId") Long ownerId);

    Optional<Application> findByIdAndOwnerId(Long id, Long ownerId);

    @Query("SELECT a FROM Application a JOIN FETCH a.company JOIN FETCH a.owner " +
            "WHERE a.deletedAt IS NULL " +
            "ORDER BY a.applyDate DESC, a.id DESC")
    List<Application> findAllActiveWithCompanyAndOwner();
}