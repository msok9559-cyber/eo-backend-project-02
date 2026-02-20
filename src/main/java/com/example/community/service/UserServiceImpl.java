package com.example.community.service;


import com.example.community.domain.user.UserDto;
import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
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

    @Transactional(readOnly = true)
    @Override
    public Optional<UserDto> read(@NotNull Long id) {
        return userRepository.findById(id).map(UserDto::from);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<UserDto> read(@NotNull String username) {
        return userRepository.findByUsername(username).map(UserDto::from);
    }

    // 회원 수정
    @Transactional
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

    // 마이페이지 닉네임 수정
    @Override
    public void updateNickname(Long userId, String nickname) {
        log.info("update nickname: userId={}, nickname={}", userId, nickname);

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateNickname(nickname);

        log.info("닉네임 수정 완료: userId={}", userId);
    }

    // 마이페이지 비밀번호 변경
    @Override
    public void changePassword(Long userId,
                               String currentPassword,
                               String newPassword,
                               String newPasswordConfirm) {

        log.info("비밀번호 변경 시도: userId={}", userId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 암호화 후 저장
        String encoded = passwordEncoder.encode(newPassword);
        user.updatePassword(encoded);

        log.info("비밀번호 변경 완료: userId={}", userId);
    }


    /**
     * 비밀번호 재설정 (이메일 인증 후)
     */
    @Transactional
    @Override
    public void resetPassword(String email, String newPassword) {
        log.info("비밀번호 재설정 시작: email={}", email);

        // 이메일로 사용자 찾기
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 등록된 사용자가 없습니다."));

        // 비밀번호 유효성 검사
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 20) {
            throw new IllegalArgumentException("비밀번호는 8~20자로 입력해주세요.");
        }

        // 비밀번호 암호화 및 저장
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);

        log.info("비밀번호 재설정 완료: email={}", email);
    }

    /**
     * 현재 비밀번호 체크
     */
    @Override
    public boolean verifyCurrentPassword(Long userId, String currentPassword) {
        return userRepository.findById(userId)
                .map(user -> passwordEncoder.matches(currentPassword, user.getPassword()))
                .orElse(false);
    }
}
