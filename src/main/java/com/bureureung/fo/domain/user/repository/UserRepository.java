package com.bureureung.fo.domain.user.repository;

import com.bureureung.fo.domain.user.entity.FoUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<FoUser, Long> {

    Optional<FoUser> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}
