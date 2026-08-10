package com.likelion.team4.domain.user.repository;

import com.likelion.team4.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인 시 아이디로 회원 조회
    Optional<User> findByLoginId(String loginId);

    // 회원가입 시 아이디 중복 확인
    boolean existsByLoginId(String loginId);

    // 토큰 재발급 시 refreshToken으로 회원 조회
    Optional<User> findByRefreshToken(String refreshToken);
}
