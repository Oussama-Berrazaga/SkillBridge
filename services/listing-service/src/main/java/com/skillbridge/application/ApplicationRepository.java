package com.skillbridge.application;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

  public boolean existsByListingIdAndTechnicianId(Long technicianId, Long listingId);
}
