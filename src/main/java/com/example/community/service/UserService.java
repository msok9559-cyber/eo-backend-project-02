package com.example.community.service;

import com.example.community.domain.user.UserDto;

import java.util.Optional;

public interface UserService {
    /*
    회원 가입
     */
    void create(UserDto userDto);

    /*
    id로 회원 번호 조회
     */
    Optional<UserDto> read(Long id);

    /*
    아이디로 회원 조회
     */
    Optional<UserDto> read(String username);

    /*
    회원 정보 수정
     */
    Optional<UserDto> update(UserDto userDto);

    /*
    회원 탈퇴
     */
    boolean delete(Long id);

    /*
    아이디 중복 체크
     */
    boolean existsByUsername(String username);

    /*
    닉네임 중복 체크
     */
    boolean existsByNickname(String nickname);

    /*
    마이페이지 닉네임 수정
    */
    void updateNickname(Long userId, String nickname);

    /*
    비밀번호 변경
     */
    void changePassword(Long userId, String currentPassword, String newPassword, String newPasswordConfirm);

    /*
     * 비밀번호 재설정 (이메일 인증 후)
     */
    void resetPassword(String email, String newPassword);

    /*
    * 현재 비밀번호 체크
     */
    boolean verifyCurrentPassword(Long userId, String currentPassword);
}
