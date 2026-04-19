package com.bureureung.fo.domain.user.service;

import com.bureureung.fo.domain.user.dto.RegisterRequest;
import com.bureureung.fo.domain.user.entity.FoUser;
import com.bureureung.fo.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void 회원가입을_한다() {
        // given
        String email = "test@test.com";
        String nickname = "테스트";
        var request = new RegisterRequest(email, "1234", nickname, "01012341234");

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(userRepository.existsByNickname(nickname)).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encoded-1234");

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
    }
}