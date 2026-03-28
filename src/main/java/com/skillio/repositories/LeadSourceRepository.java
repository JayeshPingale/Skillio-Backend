package com.skillio.repositories;

import com.skillio.entities.LeadSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface LeadSourceRepository extends JpaRepository<LeadSource, Long> {
    Optional<LeadSource> findByName(String name);
    List<LeadSource> findByChannel(String channel);
    List<LeadSource> findByIsActive(Boolean isActive);
	List<LeadSource> findByIsActiveTrue();
}
