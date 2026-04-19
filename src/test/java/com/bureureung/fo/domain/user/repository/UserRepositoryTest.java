package com.bureureung.fo.domain.user.repository;

import com.bureureung.fo.domain.user.entity.FoUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 이메일로_유저를_조회한다() {
        var newUser = createUser();
        userRepository.save(newUser);

        Optional<FoUser> result = userRepository.findByEmail(newUser.getEmail());
        Assertions.assertThat(result).isPresent();
    }

    @Test
    void 존재하지_않는_이메일로_조회하면_빈값을_반환한다() {
        Optional<FoUser> result = userRepository.findByEmail("test@test.com");
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    void 이메일이_존재하는지_확인한다() {
        var newUser = createUser();
        userRepository.save(newUser);

        assertThat(userRepository.existsByEmail(newUser.getEmail())).isTrue();
    }

    @Test
    void 이메일이_존재하지_않을때_false를_반환한다() {
        assertThat(userRepository.existsByEmail("test@test.com")).isFalse();
    }

    @Test
    void 이메일이_중복되면_예외가_발생한다() {
        var newUser = createUser();
        userRepository.save(newUser);

        var newUser2 = createUser();

        assertThatThrownBy(() -> {
            userRepository.save(newUser2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 닉네임이_중복되면_예외가_발생한다() {
        var newUser = createUser();
        userRepository.save(newUser);

        var newUser2 = createUser("test@test2.com");

        assertThatThrownBy(() -> {
            userRepository.save(newUser2);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 닉네임이_존재하는지_확인한다() {
        var newUser = createUser();
        userRepository.save(newUser);

        assertThat(userRepository.existsByNickname(newUser.getNickname())).isTrue();
    }

    @Test
    void 닉네임이_존재하지_않을때_false를_반환한다() {
        assertThat(userRepository.existsByNickname("테스트")).isFalse();
    }


    public FoUser createUser() {
        return FoUser.of("test@test.com", "1234", "테스트", "01012341234");
    }

    public FoUser createUser(String email) {
        return FoUser.of(email, "1234", "테스트", "01012341234");
    }

}