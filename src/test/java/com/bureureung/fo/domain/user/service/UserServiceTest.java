package com.bureureung.fo.domain.user.service;

import com.bureureung.fo.domain.auth.dto.FoUserTermsResponse;
import com.bureureung.fo.domain.auth.entity.EmailVerification;
import com.bureureung.fo.domain.auth.repository.EmailVerificationRepository;
import com.bureureung.fo.domain.user.dto.UserResponse;
import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.entity.FoUserTerms;
import com.bureureung.fo.domain.user.entity.TermsType;
import com.bureureung.fo.domain.user.repository.UserRepository;
import com.bureureung.fo.domain.user.repository.UserTermsRepository;
import com.bureureung.fo.fixture.RegisterRequestFixture;
import com.bureureung.fo.global.exception.CustomException;
import com.bureureung.fo.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private UserTermsRepository userTermsRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void 회원가입을_한다() {
        // given
        var request = RegisterRequestFixture.create();
        String email = request.email();
        String nickname = request.nickname();

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(userRepository.existsByNickname(nickname)).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encoded-1234");

        // 이메일 인증
        EmailVerification verification = EmailVerification.issue(email);
        verification.verify(); // 인증 완료 상태로 만들기
        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        given(userRepository.save(any(FoUser.class)))
                .willAnswer(invocation -> invocation.getArgument(0)); //받은 그대로 반환

        //when
        userService.register(request);

        // then
        ArgumentCaptor<FoUser> captor = ArgumentCaptor.forClass(FoUser.class);
        verify(userRepository).save(captor.capture());

        FoUser saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getNickname()).isEqualTo(nickname);
        assertThat(saved.getPassword()).isEqualTo("encoded-1234");
        assertThat(saved.getPassword()).isNotEqualTo(request.password());

        // 약관 동의 검증
        ArgumentCaptor<List<FoUserTerms>> termsCaptor = ArgumentCaptor.forClass(List.class);
        verify(userTermsRepository).saveAll(termsCaptor.capture());

        var savedTerms = termsCaptor.getValue();

        assertThat(savedTerms).hasSize(4);

        FoUserTerms terms = savedTerms.stream()
                .filter(t -> t.getTermsType() == TermsType.TERMS)
                .findFirst().orElseThrow();
        assertThat(terms.isAgreed()).isTrue();

        FoUserTerms privacy = savedTerms.stream()
                .filter(t -> t.getTermsType() == TermsType.PRIVACY)
                .findFirst().orElseThrow();
        assertThat(privacy.isAgreed()).isTrue();

        FoUserTerms marketing = savedTerms.stream()
                .filter(t -> t.getTermsType() == TermsType.MARKETING)
                .findFirst().orElseThrow();
        assertThat(marketing.isAgreed()).isFalse();

        FoUserTerms nightMarketing = savedTerms.stream()
                .filter(t -> t.getTermsType() == TermsType.NIGHT_MARKETING)
                .findFirst().orElseThrow();
        assertThat(nightMarketing.isAgreed()).isFalse();
    }

    @Test
    void 이메일이_중복이면_회원가입에_실패한다() {
        // given
        var request = RegisterRequestFixture.create();
        String email = request.email();

        EmailVerification verification = EmailVerification.issue(email);
        verification.verify();

        given(userRepository.existsByEmail(email)).willReturn(true);
        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        // when & then
        assertThatThrownBy(() -> {
            userService.register(request);
        }).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

        verify(userRepository, never()).save(any(FoUser.class));
    }

    @Test
    void 닉네임이_중복이면_회원가입에_실패한다() {
        // given
        var request = RegisterRequestFixture.create();

        String nickname = request.nickname();
        String email = "test@test.com";

        EmailVerification verification = EmailVerification.issue(email);
        verification.verify();

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(userRepository.existsByNickname(nickname)).willReturn(true);
        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        // when & then
        assertThatThrownBy(() -> {
            userService.register(request);
        }).isInstanceOf(CustomException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);

        verify(userRepository, never()).save(any(FoUser.class));
    }

    @Test
    void 이메일_인증이_안되면_회원가입을_실패한다() {
        // given
        var request = RegisterRequestFixture.create();
        String email = request.email();

        EmailVerification verification = EmailVerification.issue(email);
        // verify() 호출 안 함 -> 미인증 상태

        given(emailVerificationRepository.findById(email)).willReturn(Optional.of(verification));

        // when & then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_NOT_VERIFIED);

        verify(userRepository, never()).save(any(FoUser.class));
    }

    @Test
    void 회원가입_시_약관_필수_동의를_하지_않으면_예외가_발생한다() {
        // given
        Map<TermsType, Boolean> termsMap = Map.of(
                TermsType.TERMS, false,
                TermsType.PRIVACY, true
        );

        var request = RegisterRequestFixture.createWithTerms(termsMap);

        // when
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUIRED_TERMS_NOT_AGREED);

        // then
        verify(userRepository, never()).save(any(FoUser.class));
        verify(userTermsRepository, never()).save(any(FoUserTerms.class));
    }

    @Test
    void 회원가입_시_필수_약관이_누락된_경우_예외가_발생한다() {
        // given
        Map<TermsType, Boolean> termsMap = Map.of(
                TermsType.MARKETING, true
        );

        var request = RegisterRequestFixture.createWithTerms(termsMap);

        // when
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REQUIRED_TERMS_NOT_AGREED);

        // then
        verify(userRepository, never()).save(any(FoUser.class));
        verify(userTermsRepository, never()).save(any(FoUserTerms.class));
    }

    @Test
    void 본인의_회원_정보를_조회한다() {
        // given
        Long userId = 1L;
        FoUser user = FoUser.of("test@test.com", "abc12345!!", "테스트", "01012341234");
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        List<FoUserTerms> terms = List.of(
                FoUserTerms.of(userId, TermsType.TERMS, true),
                FoUserTerms.of(userId, TermsType.PRIVACY, true),
                FoUserTerms.of(userId, TermsType.MARKETING, false),
                FoUserTerms.of(userId, TermsType.NIGHT_MARKETING, false)
        );

        given(userTermsRepository.findByFoUserId(userId)).willReturn(terms);

        // when
        var userResponse = userService.getProfile(userId);

        // then
        assertThat(userResponse.id()).isEqualTo(user.getId());
        assertThat(userResponse.nickname()).isEqualTo(user.getNickname());
        assertThat(userResponse.email()).isEqualTo(user.getEmail());
        assertThat(userResponse.phone()).isEqualTo(user.getPhone());

        assertThat(userResponse.terms()).hasSize(4);
        Map<TermsType, Boolean> termsMap = userResponse.terms().stream()
                .collect(Collectors.toMap(
                        FoUserTermsResponse::termsType,
                        FoUserTermsResponse::isAgreed
                ));

        assertThat(termsMap.get(TermsType.TERMS)).isTrue();
        assertThat(termsMap.get(TermsType.PRIVACY)).isTrue();
        assertThat(termsMap.get(TermsType.MARKETING)).isFalse();
        assertThat(termsMap.get(TermsType.NIGHT_MARKETING)).isFalse();
    }

    @Test
    void 존재하지_않는_회원_번호로_조회한다() {
        //given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> userService.getProfile(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }


}