package com.paymedia.administrations.repository;

import com.paymedia.administrations.entity.DualAuthData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DualAuthDataRepository extends JpaRepository<DualAuthData, Integer> {
}

