package uk.gov.hmcts.reform.civil.service.pininpost;

import feign.Response;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.CMCPinVerifyConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class CUIIdamClientService {

    private final IdamApi idamApi;
    private final CMCPinVerifyConfiguration cmcPinVerifyConfiguration;

    public int authenticatePinUser(String pin, String state) {

        final String encodedRedirectUrl = URLEncoder.encode(cmcPinVerifyConfiguration.getRedirectUrl() + "/receiver", StandardCharsets.UTF_8);
        final Response response = idamApi.authenticatePinUser(pin, cmcPinVerifyConfiguration.getClientId(), encodedRedirectUrl, state);
        return response.status();
    }
}
