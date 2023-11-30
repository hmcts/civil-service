package uk.gov.hmcts.reform.civil.service.pininpost;

import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
public class CUIIdamClientService {

    private final IdamApi idamApi;
    private static String clientId = "cmc_citizen";
    String redirectUrl = "https://moneyclaims.aat.platform.hmcts.net/receiver";

    @Autowired
    public CUIIdamClientService(IdamApi idamApi) {
        this.idamApi = idamApi;
    }

    public int authenticatePinUser(String pin, String state)
        throws UnsupportedEncodingException {
        final String encodedRedirectUrl = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);

        final Response response = idamApi.authenticatePinUser(pin, clientId, encodedRedirectUrl, state);
        return response.status();
    }
}
