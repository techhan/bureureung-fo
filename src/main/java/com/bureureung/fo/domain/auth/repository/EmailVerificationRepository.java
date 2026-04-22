package com.bureureung.fo.domain.auth.repository;

import com.bureureung.fo.domain.auth.entity.EmailVerification;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, String> {
}
