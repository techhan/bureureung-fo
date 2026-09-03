package com.bureureung.fo.global.util;

/**
 * 문자열을 마스킹한다.
 */
public final class MaskingUtil {

  private MaskingUtil() {}

  /**
   * "****"를 리턴한다.
   */
  public static String mask() {
    return "****";
  }

  /**
   * 핸드폰 번호 가운데 4자리를 마스킹한다. (01012345678 → 010****5678)
   */
  public static String maskPhone(String value) {
    if(value == null || value.length() != 11) {
      return "****";
    }

    return value.substring(0, 3) + "****" + value.substring(7);
  }

  /**
   * 이메일을 마스킹한다.
   * 앞자리가 2자 이하일 경우 -> ****@domain.com
   * 앞자리가 3자 이상일 경우 -> hs****@domain.com
   */
  public static String maskEmail(String value) {
    if(value == null || !value.contains("@")) {
      return "****";
    }

    int at = value.indexOf("@");
    if (at <= 2) {
      return "****" + value.substring(at);
    }
    return value.substring(0,2) + "****" + value.substring(at);
  }

}
