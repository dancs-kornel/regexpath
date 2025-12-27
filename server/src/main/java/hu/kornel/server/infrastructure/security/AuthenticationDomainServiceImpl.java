package hu.kornel.server.infrastructure.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import hu.kornel.server.domain.entities.User;
import hu.kornel.server.domain.exception.InvalidTokenException;
import hu.kornel.server.domain.service.AuthenticationDomainServiceInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationDomainServiceImpl implements AuthenticationDomainServiceInterface {
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public String hashPassword(String plainPassword) { return passwordEncoder.encode(plainPassword); }

    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) { return passwordEncoder.matches(plainPassword, hashedPassword); }

    @Override
    public String generateToken(User user) { return jwtTokenProvider.generateToken(user); }

    @Override
    public Long validateTokenAndGetUserId(String token) {
        if (!jwtTokenProvider.validateToken(token)) throw InvalidTokenException.invalid();
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    @Override
    public boolean isTokenExpired(String token) { return jwtTokenProvider.isTokenExpired(token); }
}
