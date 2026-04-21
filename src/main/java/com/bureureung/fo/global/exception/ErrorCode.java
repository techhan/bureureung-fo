package com.bureureung.fo.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ------ 공통 (common) ------
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_E001", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_E002", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_E003", "서버 오류가 발생했습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_E004", "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "COMMON_E005", "이미 존재하는 데이터입니다."),

    // ------ 인증/인가 (auth) ------
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_E001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_E002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_E003", "만료된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_E004", "접근 권한이 없습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_E005" , "이메일과 비밀번호 정보를 찾을 수 없습니다." ),

    // ------ 사용자 (user) ------
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_E001", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_E002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_E003", "이미 사용 중인 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_E004", "비밀번호가 올바르지 않습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "USER_E005" , "필수 약관에 동의해야 합니다."),

    // ------ 이메일 (email) ------
    EMAIL_SEND_FAILED(HttpStatus.BAD_GATEWAY, "MAIL_E001", "메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "MAIL_E002", "이메일 인증이 필요합니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "MAIL_E003", "인증 코드가 일치하지 않습니다."),
    VERIFICATION_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "MAIL_E004", "인증 코드가 만료되었거나 존재하지 않습니다."),
    EMAIL_ALREADY_VERIFIED(HttpStatus.CONFLICT, "MAIL_E005", "이미 인증이 완료된 이메일입니다."),

    // ------ 가게 (RESTAURANT) ------
    RESTAURANT_NOT_FOUND(HttpStatus.NOT_FOUND, "RESTAURANT_E001", "존재하지 않는 가게입니다."),
    RESTAURANT_NOT_OPEN(HttpStatus.BAD_REQUEST, "RESTAURANT_E002", "현재 영업 중이 아닙니다."),

    // ------ 메뉴 (MENU) ------
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "MENU_E001", "존재하지 않는 메뉴입니다."),
    MENU_SOLD_OUT(HttpStatus.BAD_REQUEST, "MENU_E002", "품절된 메뉴입니다."),

    // ------ 주문 (ORDER) ------
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_E001", "존재하지 않는 주문입니다."),
    CANNOT_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "ORDER_E002", "취소할 수 없는 주문 상태입니다."),
    BELOW_MIN_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "ORDER_E003", "최소 주문 금액 미달입니다."),

    // ------ 결제 (PAYMENT) ------
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT_E001", "결제에 실패했습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_E002", "결제 금액이 일치하지 않습니다."),

    // ------ 리뷰 (REVIEW) ------
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_E001", "존재하지 않는 리뷰입니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW_E002", "이미 리뷰를 작성했습니다."),
    CANNOT_REVIEW_BEFORE_DELIVERY(HttpStatus.BAD_REQUEST, "REVIEW_E003", "배달 완료 후 리뷰 작성이 가능합니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
