package com.deliverXY.backend.NewCode.wallet.repository;

import com.deliverXY.backend.NewCode.wallet.domain.TopUpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopUpRepository extends JpaRepository<TopUpRequest, Long> {

}

