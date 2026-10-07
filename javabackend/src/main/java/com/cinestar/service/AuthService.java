package com.cinestar.service;

import com.cinestar.dto.request.LoginRequest;
import com.cinestar.dto.request.RegisterRequest;
import com.cinestar.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);
}
