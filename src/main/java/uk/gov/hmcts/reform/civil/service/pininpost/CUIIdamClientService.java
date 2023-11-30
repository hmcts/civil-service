package uk.gov.hmcts.reform.civil.service.pininpost;

import feign.Response;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class CUIIdamClientService {

    private final IdamApi idamApi;
    private static String clientId = "cmc_citizen";

    public int authenticatePinUser(String pin, String state)
        throws UnsupportedEncodingException {
        final String encodedRedirectUrl = URLEncoder.encode("https://moneyclaims.aat.platform.hmcts.net/receiver", StandardCharsets.UTF_8);

        final Response response = idamApi.authenticatePinUser(pin, clientId, encodedRedirectUrl, state);
        return response.status();
    }
}
