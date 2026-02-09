package com.example.community.security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Spring Security 프레임워크의 SecurityFilterChain을 적용하지 않을 요청을 전역적으로 지정
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return webSecurity -> webSecurity.ignoring()
                .requestMatchers("/h2-console/**")
                .requestMatchers("/static/**")
                .requestMatchers("/css/**")
                .requestMatchers("/images/**")
                .requestMatchers("/js/**");
                //.requestMatchers("/check-username", "/check-nickname");
    }

    /**
     * SecurityFilterChain의 구성을 설정
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // Credential 기반 인증(username과 password로 인증)에 대한 설정
        httpSecurity
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                );

        // 인가(authorization)에 대한 설정
        httpSecurity
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/", "/login", "/signup", "/check-username", "/check-nickname").permitAll()
                        .requestMatchers("/send-verification-email", "/verify-email-code").permitAll()
                        .requestMatchers("/board/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/error/**").permitAll()
                        .anyRequest().authenticated()
                );

        // CSRF 비활성화 (테스트용)
        httpSecurity
                .csrf(csrf -> csrf.disable());

        // AccessDeniedException 예외를 처리할 핸들러 등록
        httpSecurity
                .exceptionHandling(handler -> handler
                        .accessDeniedHandler(accessDeniedHandler())
                );

        // CustomUserDetailsService를 UserDetailsService 등록
        httpSecurity
                .userDetailsService(customUserDetailsService);

        return httpSecurity.build();
    }

    /**
     * AccessDeniedException 예외가 발생했을 때 처리하기 위한 핸들러
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                request.getRequestDispatcher("/error/403").forward(request, response);
    }

    /**
     * 사용자가 입력한 비밀번호를 암호화할 PasswordEncoder를 빈(bean)으로 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}