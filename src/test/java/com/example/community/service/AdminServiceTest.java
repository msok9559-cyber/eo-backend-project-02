package com.example.community.service;

import com.example.community.domain.user.UserDto;
import com.example.community.domain.user.UserEntity;
import com.example.community.domain.user.UserRole;
import com.example.community.persistence.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminServiceTest {
    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity testUser;
    private UserEntity testAdmin;

    @BeforeEach
    void setUp() {
        //테스트 사용자 생성
        testUser = UserEntity.builder()
                .username("testuser")
                .password(passwordEncoder.encode("1234"))
                .name("테스트 유저")
                .nickname("유저")
                .email("user@test.com")
                .role(UserRole.USER)
                .active(true)
                .emailVerified(true)
                .build();
        userRepository.save(testUser);

        //테스트 관리자 생성
        testAdmin = UserEntity.builder()
                .username("testamin")
                .password(passwordEncoder.encode("1234"))
                .name("테스트관리자")
                .nickname("관리자")
                .email("admin@test.com")
                .role(UserRole.ADMIN)
                .active(true)
                .emailVerified(true)
                .build();
        userRepository.save(testAdmin);
    }

    @Test
    @DisplayName("전체 사용자 조회 - 페이징")
    void testGetAllUsers() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<UserDto> result = adminService.getAllUsers(pageable,"ALL",null);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("역할별 사용자 조회 - USER")
    void testGetUsersByRole() {

        Pageable pageable = PageRequest.of(0, 10);


        Page<UserDto> result = adminService.getAllUsers(pageable, "USER", null);


        assertThat(result).isNotNull();
        assertThat(result.getContent()).allMatch(user -> user.getRole() == UserRole.USER);
    }

    @Test
    @DisplayName("키워드로 사용자 검색")
    void testSearchUsers() {

        Pageable pageable = PageRequest.of(0, 10);


        Page<UserDto> result = adminService.getAllUsers(pageable, "ALL", "testuser");


        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getUsername()).contains("testuser");
    }

    @Test
    @DisplayName("사용자 상세 조회 성공")
    void testGetUserById() {

        UserDto result = adminService.getUserById(testUser.getId());


        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getName()).isEqualTo("테스트 유저");
    }

    @Test
    @DisplayName("사용자 상세 조회 실패 - 존재하지 않는 ID")
    void testGetUserByIdNotFound() {

        assertThatThrownBy(() -> adminService.getUserById(99999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 권한 변경 성공")
    void testChangeUserRole() {

        adminService.changeUserRole(testUser.getId(), "ADMIN", "테스트 승급", testAdmin.getId());


        UserEntity updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("자기 자신 권한 변경 실패")
    void testChangeOwnRoleFails() {

        assertThatThrownBy(() ->
                adminService.changeUserRole(testAdmin.getId(), "USER", "테스트", testAdmin.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("자신의 권한은 변경할 수 없습니다");
    }

    @Test
    @DisplayName("최소 1명 관리자 유지 검증")
    void testMinimumAdminCheck() {
        // given - 관리자가 1명만 있는 상태 (testAdmin)

        // when & then
        assertThatThrownBy(() ->
                adminService.changeUserRole(testAdmin.getId(), "USER", "테스트", testUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("최소 1명의 관리자는 유지되어야 합니다");
    }

    @Test
    @DisplayName("사용자 정지 성공")
    void testBanUser() {
        // when
        adminService.banUser(testUser.getId(), "테스트 정지", 7, "테스트", testAdmin.getId());

        // then
        UserEntity banned = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(banned.getActive()).isFalse();
    }

    @Test
    @DisplayName("사용자 활성화")
    void testActivateUser() {
        // given
        testUser.setActive(false);
        userRepository.save(testUser);

        // when
        adminService.activateUser(testUser.getId());

        // then
        UserEntity activated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(activated.getActive()).isTrue();
    }

    @Test
    @DisplayName("사용자 비활성화")
    void testDeactivateUser() {
        // when
        adminService.deactivateUser(testUser.getId());

        // then
        UserEntity deactivated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(deactivated.getActive()).isFalse();
    }



}