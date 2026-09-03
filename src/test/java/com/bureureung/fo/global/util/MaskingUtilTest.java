package com.bureureung.fo.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class MaskingUtilTest {

  @Test
  void 핸드폰_번호_가운데_자리를_마스킹한다() {
    assertThat(MaskingUtil.maskPhone("01012341234")).isEqualTo("010****1234");
  }

  @Test
  void 핸드폰_번호가_null이거나_형식이_다르면_전체를_마스킹한다() {
    assertThat(MaskingUtil.maskPhone(null)).isEqualTo("****");
    assertThat(MaskingUtil.maskPhone("0212341234")).isEqualTo("****");
  }

  @Test
  void 이메일_아이디를_마스킹한다() {
    assertThat(MaskingUtil.maskEmail("hs@email.com")).isEqualTo("****@email.com");
    assertThat(MaskingUtil.maskEmail("hs4441@email.com")).isEqualTo("hs****@email.com");
  }

  @Test
  void 이메일이_null이거나_형식이_다르면_전체를_마스킹한다() {
    assertThat(MaskingUtil.maskEmail(null)).isEqualTo("****");
    assertThat(MaskingUtil.maskEmail("hansol")).isEqualTo("****");
  }
}
