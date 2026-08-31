package com.bureureung.fo.domain.user.repository;

import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<FoUser, Long> {
    Optional<FoUser> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    default FoUser getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
