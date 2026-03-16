package uk.gov.hmcts.reform.civil.client.idam.models;

import feign.form.FormProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Builder
@Getter
public class ExchangeCodeRequest {
    private String code;
    @FormProperty("grant_type")
    private String grantType;
    @FormProperty("redirect_uri")
    private String redirectUri;
    @FormProperty("client_id")
    private String clientId;
    @FormProperty("client_secret")
    private String clientSecret;

    public ExchangeCodeRequest(
            String code, String grantType, String redirectUri, String clientId, String clientSecret
    ) {
        this.code = code;
        this.grantType = grantType;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
