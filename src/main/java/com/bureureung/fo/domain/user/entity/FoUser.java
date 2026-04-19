package com.bureureung.fo.domain.user.entity;

import com.bureureung.fo.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "fo_user")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class FoUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false, unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 20)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserGrade grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column
    private LocalDateTime deletedAt;

    public static FoUser of(String email, String password, String nickname, String phone) {
        FoUser user = new FoUser();
        user.email = email;
        user.password = password;
        user.nickname = nickname;
        user.phone = phone;
        user.grade = UserGrade.BRONZE;
        user.status = UserStatus.ACTIVE;
        return user;
    }
}
