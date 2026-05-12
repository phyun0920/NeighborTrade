package com.study.neighbortrade.repository;

import com.study.neighbortrade.domain.location.LocationVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationVerificationRepository extends JpaRepository<LocationVerification, Long> {
}
