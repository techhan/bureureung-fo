package com.bureureung.fo.domain.user.entity;

import com.bureureung.fo.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalHistory extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long foUserId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private WithdrawReason reason;

  @Column(length = 500)
  private String detail;

  @Column(nullable = false, updatable = false)
  private LocalDateTime withdrawnAt;

  public static WithdrawalHistory of(Long foUserId, WithdrawReason reason, String detail) {
    WithdrawalHistory history = new WithdrawalHistory();
    history.foUserId = foUserId;
    history.reason = reason;
    history.detail = detail;
    history.withdrawnAt = LocalDateTime.now();
    return history;
  }
}
