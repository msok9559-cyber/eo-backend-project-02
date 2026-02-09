package com.example.community.persistence;

import com.example.community.domain.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    // Repository Bean 확인
    @Test
    public void testExists(){
        assertNotNull(userRepository);
        log.info("UserRepository Test = {}" , userRepository);
    }

    // 저장 테스트
    @Test
    public void testSave(){
        UserEntity user = UserEntity.builder()
                .username("park_test")
                .password("password123!")
                .name("Repository Test")
                .nickname("park_tester")
                .email("park@naver.com")
                .build();
        log.info("Before save : user = {}", user);

        UserEntity savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertThat(savedUser.getUsername()).isEqualTo("park_test");
        log.info("After save : user = {}", savedUser);
    }

    //Helper method
    private UserEntity createUser(String username, String name, String nickname, String email) {
        return UserEntity.builder()
                .username(username)
                .password("password123!")
                .name(name)
                .nickname(nickname)
                .email(email)
                .build();
    }




}