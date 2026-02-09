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
}
