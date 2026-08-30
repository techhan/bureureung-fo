package com.bureureung.fo.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawReason {
  RARELY_USE("잘 이용하지 않아요"),
  FEW_RESTAURANTS("주변에 주문할 가게가 없어요"),
  DELIVERY_FEE("배달비가 부담돼요"),
  SERVICE_DISSATISFIED("서비스 이용이 불편해요"),
  APP_ERROR("앱 오류가 잦아요"),
  PRIVACY_CONCERN("개인정보 유출이 걱정돼요"),
  RE_SIGNUP("다른 계정으로 재가입할 거예요"),
  OTHER("기타");

  private final String description;
}
