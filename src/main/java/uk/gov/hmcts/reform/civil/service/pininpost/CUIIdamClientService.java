package uk.gov.hmcts.reform.civil.service.pininpost;

import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.CMCPinVerifyConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class CUIIdamClientService {

    private final IdamApi idamApi;
    private final CMCPinVerifyConfiguration cmcPinVerifyConfiguration;

    @Autowired
    public CUIIdamClientService(IdamApi idamApi, CMCPinVerifyConfiguration cmcPinVerifyConfiguration) {
        this.idamApi = idamApi;
        this.cmcPinVerifyConfiguration = cmcPinVerifyConfiguration;
    }

    public int authenticatePinUser(String pin, String state) {

        final String encodedRedirectUrl = URLEncoder.encode(cmcPinVerifyConfiguration.getRedirectUrl() + "/receiver", StandardCharsets.UTF_8);
        try (Response response = idamApi.authenticatePinUser(
            pin,
            cmcPinVerifyConfiguration.getClientId(),
            encodedRedirectUrl,
            state
        )) {
            return response.status();
        }
    }
}
