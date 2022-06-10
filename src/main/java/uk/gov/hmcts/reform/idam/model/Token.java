package uk.gov.hmcts.reform.idam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Token {

    private String accessToken;
    private String scope;

    private Token() {
        //No-op constructor for deserialization
    }

    public Token(String accessToken, String scope) {
        this.accessToken = accessToken;
        this.scope = scope;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getScope() {
        return scope;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Token token = (Token) object;
        return Objects.equals(accessToken, token.accessToken)
            && Objects.equals(scope, token.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, scope);
    }

    @Override
    public String toString() {
        return "Token{"
            + "accessToken='" + accessToken + '\''
            + ", scope='" + scope + '\''
            + '}';
    }
}
