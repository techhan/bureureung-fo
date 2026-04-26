package com.bureureung.fo.domain.user.repository;

import com.bureureung.fo.domain.user.entity.FoUserTerms;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserTermsRepository extends JpaRepository<FoUserTerms, Long> {

    List<FoUserTerms> findByFoUserId(@Param("userId") Long userId);
}
