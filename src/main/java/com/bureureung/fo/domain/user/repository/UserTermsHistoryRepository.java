package com.bureureung.fo.domain.user.repository;

import com.bureureung.fo.domain.user.entity.FoUserTermsHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermsHistoryRepository extends JpaRepository<FoUserTermsHistory, Long> {
}
