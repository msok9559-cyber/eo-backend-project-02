package com.example.community.service;


import com.example.community.domain.user.UserDto;
import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void create(@NotNull UserDto userDto){
        log.info("signup start : username = {}", userDto.getUsername());

        //중복 체크
        checkUsernameAvailability(userDto.getUsername());
        checkNicknameAvailability(userDto.getNickname());
        checkEmailAvailability(userDto.getEmail());

        //비밀 번호 암호화
        setEncodedPassword(userDto);

        // 엔티티 변환 및 저장
        UserEntity userEntity = UserEntity.builder()
                .username(userDto.getUsername())
                .password(userDto.getPassword())
                .name(userDto.getName())
                .nickname(userDto.getNickname())
                .email(userDto.getEmail())
                .emailVerified(userDto.getEmailVerified())
                .role(userDto.getRole())
                .active(userDto.getActive())
                .build();

        UserEntity savedEntity = userRepository.save(userEntity);
        userDto.setId(userEntity.getId());

        log.info("signup end : username = {}", savedEntity.getId());
    }

    @Override
    public Optional<UserDto> read(@NotNull Long id) {
        return userRepository.findById(id).map(UserDto::from);
    }

    @Override
    public Optional<UserDto> read(@NotNull String username) {
        return userRepository.findByUsername(username).map(UserDto::from);
    }

    // 회원 수정
    @Override
    public Optional<UserDto> update(@NotNull UserDto userDto) {
        log.info("update: id={}", userDto.getId());

        return userRepository.findById(userDto.getId()).map(userEntity -> {
            // 닉네임 변경
            if (userDto.getNickname() != null && !userDto.getNickname().equals(userEntity.getNickname())) {
                checkNicknameAvailability(userDto.getNickname());
                userEntity.updateNickname(userDto.getNickname());
            }

            // 이메일 변경
            if (userDto.getEmail() != null && !userDto.getEmail().equals(userEntity.getEmail())) {
                checkEmailAvailability(userDto.getEmail());
                userEntity.updateEmail(userDto.getEmail());
            }

            // 비밀번호 변경 (있는 경우만)
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                String encodedPassword = passwordEncoder.encode(userDto.getPassword());
                userEntity.updatePassword(encodedPassword);
            }

            log.info("회원 수정 완료: id={}", userEntity.getId());

            return UserDto.from(userEntity);
        });
    }
    // 회원 탈퇴
    @Override
    public boolean delete(@NotNull Long id) {
        log.info("delete: id={}", id);

        return userRepository.findById(id).map(userEntity -> {

            userRepository.delete(userEntity);


            log.info("회원 탈퇴 완료: id={}", id);
            return true;
        }).orElse(false);
    }


     // 아이디 사용 가능 여부 확인

    private void checkUsernameAvailability(@NotNull String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already in use");
        }
    }

    // 닉네임 사용 가능 여부 확인
    private void checkNicknameAvailability(@NotNull String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("Nickname is already in use");
        }
    }

    // 이메일 사용 가능 여부 확인
    private void checkEmailAvailability(@NotNull String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already in use");
        }
    }

    // 비밀번호 암화화
    private UserDto setEncodedPassword(@NotNull UserDto userDto) {
        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);
        return userDto;
    }

    // 아아디 중복체크
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // 닉네임 중복 체크
    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

}
