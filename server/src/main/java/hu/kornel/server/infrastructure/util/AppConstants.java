package hu.kornel.server.infrastructure.util;


public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;

    
    public static final boolean ACCOUNT_NON_EXPIRED = true;
    public static final boolean ACCOUNT_NON_LOCKED = true;
    public static final boolean CREDENTIALS_NON_EXPIRED = true;
}
