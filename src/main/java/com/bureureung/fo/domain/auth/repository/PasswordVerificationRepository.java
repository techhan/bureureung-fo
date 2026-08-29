package com.bureureung.fo.domain.auth.repository;

import com.bureureung.fo.domain.auth.entity.PasswordVerification;
import org.springframework.data.repository.CrudRepository;

public interface PasswordVerificationRepository extends CrudRepository<PasswordVerification, Long> {
}
