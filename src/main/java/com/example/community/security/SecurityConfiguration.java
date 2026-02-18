package com.example.community.security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    private final com.example.community.security.CustomOAuth2UserService customOAuth2UserService;


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
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                );

        // 소셜 로그인 인가 설정
        httpSecurity
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                );

        httpSecurity
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        // 인가(authorization)에 대한 설정
        httpSecurity
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/", "/login", "/signup", "/check-username", "/check-nickname","/findpassword").permitAll()
                        .requestMatchers("/send-verification-email", "/verify-email-code").permitAll()
                        .requestMatchers("/css/**", "/images/**", "/js/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/mypage/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/board/**").permitAll()
                        .requestMatchers("/board/**").hasAnyRole("USER", "ADMIN")

                        // 댓글 조회 API (GET) - 로그인 불필요 (누구나 댓글을 볼 수 있음)
                        .requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()

                        // 댓글 작성/수정/삭제 API (POST, PUT, DELETE) - 로그인 필수
                        .requestMatchers(HttpMethod.POST, "/api/posts/*/comments").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/posts/*/comments/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/*/comments/*").hasAnyRole("USER", "ADMIN")

                        // 그 외 모든 API - 로그인 필수
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/error/**").permitAll()
                        .anyRequest().authenticated()
                );

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

}