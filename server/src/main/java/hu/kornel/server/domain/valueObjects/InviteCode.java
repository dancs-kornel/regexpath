package hu.kornel.server.domain.valueObjects;

import java.security.SecureRandom;
import java.util.Objects;

import lombok.Getter;

@Getter
public class InviteCode {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String value;

    private InviteCode(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Invite code cannot be empty or null");
        if (!value.matches("^[A-Z0-9]{6}$")) throw new IllegalArgumentException("Invite code must be 6 alphanumeric characters");
        this.value = value.toUpperCase();
    }

    public static InviteCode generate() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return new InviteCode(code.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InviteCode that = (InviteCode) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }

    public static InviteCode of(String value) {
        return new InviteCode(value);
    }
}
