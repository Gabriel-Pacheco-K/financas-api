package com.gabrielpacheco.financas.service;

import com.gabrielpacheco.financas.domain.User;
import com.gabrielpacheco.financas.dto.AuthDtos.LoginRequest;
import com.gabrielpacheco.financas.dto.AuthDtos.RegisterRequest;
import com.gabrielpacheco.financas.dto.AuthDtos.TokenResponse;
import com.gabrielpacheco.financas.exception.BusinessException;
import com.gabrielpacheco.financas.repository.UserRepository;
import com.gabrielpacheco.financas.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Já existe uma conta com este e-mail.");
        }

        User user = userRepository.save(new User(request.name().trim(), email, passwordEncoder.encode(request.password())));
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalize(request.email()))
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        return tokenFor(user);
    }

    private TokenResponse tokenFor(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new TokenResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
