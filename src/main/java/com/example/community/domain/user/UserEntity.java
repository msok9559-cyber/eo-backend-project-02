package com.example.community.domain.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 회원 엔티티
 * users 테이블과 매핑
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class UserEntity {

    /**
     * 회원 고유 번호 (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /**
     * 아이디 (로그인용, 중복 불가)
     */
    @NotBlank(message = "아이디는 필수입니다")
    @Size(min = 4, max = 50, message = "아이디는 4자 이상 50자 이하여야 합니다")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "아이디는 영문, 숫자, 언더스코어만 사용 가능합니다")
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    /**
     * 비밀번호 (암호화 저장)
     */
    @NotBlank(message = "비밀번호는 필수입니다")
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 이름
     */
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    /**
     * 닉네임 (중복 불가)
     */
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다")
    @Column(name = "nickname", length = 50, nullable = false, unique = true)
    private String nickname;

    /**
     * 이메일 (중복 불가)
     */
    @NotBlank(message = "이메일은 필수입니다")
    @Size(min = 6, max = 100, message = "이메일은 6자 이상 100자 이하여야 합니다")
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "올바른 이메일 형식이 아닙니다")
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    /**
     * 이메일 인증 여부
     */
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    /**
     * 회원 권한 (USER, ADMIN)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private UserRole role = UserRole.USER;

    /**
     * 계정 활성화 여부 (탈퇴 시 false)
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * 가입일 (자동 생성)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일 (자동 갱신)
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //  생성자 (Builder)

    @Builder
    public UserEntity(String username, String password, String name, String nickname,
                      String email, Boolean emailVerified, UserRole role, Boolean active) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.emailVerified = emailVerified != null ? emailVerified : false;
        this.role = role != null ? role : UserRole.USER;
        this.active = active != null ? active : true;
    }

    //  비즈니스 메서드(메서드 체이닝)

    /**
     * 닉네임 변경
     */
    public UserEntity updateNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    /**
     * 비밀번호 변경
     */
    public UserEntity updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        return this;
    }

    /**
     * 이메일 변경
     */
    public UserEntity updateEmail(String email) {
        this.email = email;
        this.emailVerified = false; // 이메일 변경 시 재인증 필요
        return this;
    }

    /**
     * 이메일 인증 완료 처리
     */
    public UserEntity verifyEmail() {
        this.emailVerified = true;
        return this;
    }

    /**
     * 권한 변경
     */
    public UserEntity updateRole(UserRole role) {
        this.role = role;
        return this;
    }

    /**
     * 회원 탈퇴 (soft delete)
     */
    public UserEntity deactivate() {
        this.active = false;
        return this;
    }

}
