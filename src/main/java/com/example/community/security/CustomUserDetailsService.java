package com.example.community.security;

import com.example.community.domain.user.UserEntity;
import com.example.community.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * Spring Security 인증을 위한 UserDetailsService 구현체
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * username으로 사용자를 조회하여 UserDetails 객체 반환
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 활성화된 사용자만 조회
        UserEntity userEntity = userRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // UserDetails 객체를 엔티티 로 받아옴
        return new CustomUserDetails(userEntity);
    }
}
