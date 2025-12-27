package hu.kornel.server.domain.service;

import hu.kornel.server.domain.entities.User;

public interface AuthenticationDomainServiceInterface {
    String hashPassword(String plainPassword);
    boolean verifyPassword(String plainPassword, String hashedPassword);
    String generateToken(User user);
    Long validateTokenAndGetUserId(String token);
    boolean isTokenExpired(String token);
}
