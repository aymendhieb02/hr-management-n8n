package com.xtensus.hrmanagementapi.repository;

import com.xtensus.hrmanagementapi.domain.entity.JourFerie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JourFerieRepository extends JpaRepository<JourFerie, Long> {
    List<JourFerie> findByActifTrueOrderByDateAsc();
    List<JourFerie> findAllByOrderByDateAsc();
    Optional<JourFerie> findByDate(LocalDate date);
    boolean existsByDateAndIdNot(LocalDate date, Long id);
}
