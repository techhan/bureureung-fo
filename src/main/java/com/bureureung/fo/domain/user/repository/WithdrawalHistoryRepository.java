package com.bureureung.fo.domain.user.repository;

import com.bureureung.fo.domain.user.entity.WithdrawalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalHistoryRepository extends JpaRepository<WithdrawalHistory, Long> {

}
