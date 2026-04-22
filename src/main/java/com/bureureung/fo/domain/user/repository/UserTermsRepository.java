package com.bureureung.fo.domain.user.repository;

import com.bureureung.fo.domain.user.entity.FoUserTerms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermsRepository extends JpaRepository<FoUserTerms, Long> {
}
