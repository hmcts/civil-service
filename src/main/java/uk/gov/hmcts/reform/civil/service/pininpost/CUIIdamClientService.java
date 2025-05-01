package uk.gov.hmcts.reform.civil.service.pininpost;

import uk.gov.hmcts.reform.civil.config.CMCPinVerifyConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CUIIdamClientService {

    private final IdamApi idamApi;
    private final CMCPinVerifyConfiguration cmcPinVerifyConfiguration;

    @Autowired
    public CUIIdamClientService(IdamApi idamApi, CMCPinVerifyConfiguration cmcPinVerifyConfiguration) {
        this.idamApi = idamApi;
        this.cmcPinVerifyConfiguration = cmcPinVerifyConfiguration;
    }

    public Response authenticatePinUser(String pin, String state) {
        final String encodedRedirectUrl = URLEncoder.encode(cmcPinVerifyConfiguration.getRedirectUrl() + "/receiver", StandardCharsets.UTF_8);
        log.info("Redirect URL: " + encodedRedirectUrl);
        return idamApi.authenticatePinUser(
            pin,
            cmcPinVerifyConfiguration.getClientId(),
            encodedRedirectUrl,
            state
        );
    }
}
