package uk.gov.hmcts.reform.civil.client.idam.models;

import feign.form.FormProperty;

public class AuthenticateUserRequest {
    @FormProperty("response_type")
    private String responseType;
    @FormProperty("client_id")
    private String clientId;
    @FormProperty("redirect_uri")
    private String redirectUri;

    public AuthenticateUserRequest(String responseType, String clientId, String redirectUri) {
        this.responseType = responseType;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
