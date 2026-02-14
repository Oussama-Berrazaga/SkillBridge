package com.skillbridge.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

  public boolean existsByListingIdAndTechnicianId(Long technicianId, Long listingId);

  @Modifying
  @Query("UPDATE Application a SET a.status = 'REJECTED' " +
      "WHERE a.listing.id = :listingId AND a.id <> :acceptedAppId AND a.status = 'PENDING'")
  void rejectOtherPendingApplications(Long listingId, Long acceptedAppId);
}
