package com.example.community.security;

import com.example.community.domain.user.UserEntity;
import com.example.community.domain.user.UserRole;
import com.example.community.persistence.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Slf4j
@Transactional
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 이름으로 CustomUserDetails 정보를 정확히 불러오는지 테스트")
    void loadUserByUsername_Success() {
        // 1. 테스트용 유저 저장
        String username = "service_test_user";
        UserEntity user = UserEntity.builder()
                .username(username)
                .password("password123!")
                .name("테스터")
                .nickname("서비스테스터")
                .email("service@test.com")
                .role(UserRole.USER)
                .active(true)
                .build();
        userRepository.save(user);
        log.info("테스트 유저 저장 완료: {}", username);

        // 2. 서비스 호출
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 3. 결과 검증
        // 반환된 객체가 CustomUserDetails 클래스의 인스턴스인지 확인
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

        // 데이터 일치 확인
        assertThat(customUserDetails.getUsername()).isEqualTo(username);
        // CustomUserDetails에 추가한 getNickname() 메서드가 정상 작동하는지 확인

        assertThat(customUserDetails.getNickname()).isEqualTo("서비스테스터");
        assertThat(customUserDetails.isEnabled()).isTrue();

        log.info("반환된 닉네임: {}", customUserDetails.getNickname());
        log.info("CustomUserDetailsService 테스트 완료");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생 테스트")
    void loadUserByUsername_Fail_NotFound() {
        // 존재하지 않는 임의의 아이디
        String invalidUsername = "non_existent_user_1234";

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(invalidUsername);
        });

        log.info("존재하지 않는 사용자 예외 처리 확인 완료");
    }

    @Test
    @DisplayName("비활성화(탈퇴)된 사용자 조회 시 예외 발생 테스트")
    void loadUserByUsername_Fail_InactiveUser() {
        // 1.비활성 유저 저장
        String username = "inactive_user";
        UserEntity user = UserEntity.builder()
                .username(username)
                .password("password123!")
                .name("탈퇴자")
                .nickname("탈퇴유저")
                .email("out@test.com")
                .active(false)
                .build();
        userRepository.save(user);

        // 2. findByUsernameAndActiveTrue 로직에 의해 조회가 안 되어야 함
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(username);
        });

        log.info("비활성 사용자 로그인 차단 확인 완료");
    }
}