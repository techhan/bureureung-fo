package com.bureureung.fo.domain.auth.repository;

import com.bureureung.fo.domain.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
}
