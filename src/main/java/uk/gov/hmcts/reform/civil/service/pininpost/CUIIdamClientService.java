package uk.gov.hmcts.reform.civil.service.pininpost;

import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;

import java.io.UnsupportedEncodingException;


@Service
public class CUIIdamClientService {

    private IdamApi idamApi;
    private String clientId = "cmc_citizen";
    public static final String CODE = "code";

    @Autowired
    public CUIIdamClientService(IdamApi idamApi, OAuth2Configuration oauth2Configuration) {
        this.idamApi = idamApi;
    }

    public int authenticatePinUser(String pin, String state)
        throws UnsupportedEncodingException {
        AuthenticateUserResponse pinUserCode;
        final String redirectUri = "https://moneyclaims.aat.platform.hmcts.net/receiver";
        final Response response = idamApi.authenticatePinUser(pin, clientId, redirectUri, state);
        return response.status();
    }
}
