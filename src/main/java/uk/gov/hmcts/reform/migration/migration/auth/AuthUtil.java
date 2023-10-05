package uk.gov.hmcts.reform.migration.migration.auth;

@SuppressWarnings("squid:S1118")
public final class AuthUtil {

    private static final String BEARER = "Bearer ";

    private AuthUtil() {

    }

    public static String getBearerToken(String token) {
        if (token == null || token.isBlank()) {
            return token;
        }

        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }
}

