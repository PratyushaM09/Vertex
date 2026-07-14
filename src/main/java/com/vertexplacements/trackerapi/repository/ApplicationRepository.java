package com.vertexplacements.trackerapi.repository;

import com.vertexplacements.trackerapi.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    @Query("SELECT a FROM Application a JOIN FETCH a.company WHERE a.owner.id = :ownerId ORDER BY a.applyDate DESC, a.id DESC")
    List<Application> findAllWithCompanyByOwnerId(@Param("ownerId") Long ownerId);
}
