package com.bureureung.fo.domain.user.auth.repository;

import com.bureureung.fo.domain.user.auth.entity.EmailVerification;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, String> {
}
