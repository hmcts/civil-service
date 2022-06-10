package uk.gov.hmcts.reform.idam.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
public class UserIdamTokenGeneratorInfo {

    private final String userName;
    private final String userPassword;
    private final String idamRedirectUrl;
    private final String idamScope;
    private final String idamClientId;
    private final String idamClientSecret;

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getIdamRedirectUrl() {
        return idamRedirectUrl;
    }

    public String getIdamScope() {
        return idamScope;
    }

    public String getIdamClientId() {
        return idamClientId;
    }

    public String getIdamClientSecret() {
        return idamClientSecret;
    }

}
