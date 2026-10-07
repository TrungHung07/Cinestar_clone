package com.cinestar.service.impl;

import com.cinestar.dto.request.LoginRequest;
import com.cinestar.dto.request.RegisterRequest;
import com.cinestar.dto.response.AuthResponse;
import com.cinestar.dto.response.UserResponse;
import com.cinestar.entity.Role;
import com.cinestar.entity.User;
import com.cinestar.exception.BadRequestException;
import com.cinestar.exception.ResourceNotFoundException;
import com.cinestar.exception.UnauthorizedException;
import com.cinestar.repository.RoleRepository;
import com.cinestar.repository.UserRepository;
import com.cinestar.security.JwtService;
import com.cinestar.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.jwt.access-token-expiration-ms}")
    private long accessExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng");
        }
        Role userRole = roleRepository.findByName(ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Thiếu role mặc định trong hệ thống"));

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone())
                .enabled(true)
                .roles(java.util.Set.of(userRole))
                .build();
        user = userRepository.save(user);
        log.info("Dang ky tai khoan moi: {}", user.getUsername());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // findByUsernameIgnoreCase da fetch roles trong 1 query (EntityGraph)
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername().trim())
                .orElseThrow(() -> new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu");
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UnauthorizedException("Tài khoản đã bị khóa");
        }
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        Claims claims = jwtService.parse(refreshToken);
        if (claims == null || !jwtService.isRefreshToken(claims)) {
            throw new UnauthorizedException("Refresh token không hợp lệ hoặc đã hết hạn");
        }
        User user = userRepository.findByUsernameIgnoreCase(claims.getSubject())
                .orElseThrow(() -> new UnauthorizedException("Tài khoản không tồn tại"));
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String access = jwtService.generateAccessToken(user.getId(), user.getUsername(), roles);
        String refresh = jwtService.generateRefreshToken(user.getId(), user.getUsername(), roles);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(roles)
                .build();

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn(accessExpirationMs / 1000)
                .user(userResponse)
                .build();
    }
}
