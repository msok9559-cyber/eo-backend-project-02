package com.example.community.security;

import com.example.community.domain.user.UserEntity;
import com.example.community.domain.user.UserRole;
import com.example.community.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        log.info("OAuth2 Login - Provider: {}", userRequest.getClientRegistration().getRegistrationId());
        log.info("OAuth2 User Attributes: {}", oauth2User.getAttributes());

        // Google 사용자 정보 추출
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String googleId = (String) attributes.get("sub");

        // DB에서 사용자 찾기 또는 생성
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 새 사용자 생성
                    UserEntity newUser = UserEntity.builder()
                            .username("google_" + googleId)
                            .email(email)
                            .name(name)
                            .nickname(name)
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .role(UserRole.USER)
                            .active(true)
                            .build();

                    log.info("New OAuth2 user created: {}", email);
                    return userRepository.save(newUser);
                });

        // CustomUserDetails로 변환하여 반환
        return new CustomUserDetails(user, attributes);
    }
}