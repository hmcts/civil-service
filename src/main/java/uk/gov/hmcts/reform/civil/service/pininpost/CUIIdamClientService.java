package uk.gov.hmcts.reform.civil.service.pininpost;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.CMCPinVerifyConfiguration;
import uk.gov.hmcts.reform.civil.exceptions.RetryablePinException;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    @Retryable(value = RetryablePinException.class, backoff = @Backoff(delay = 500))
    public Response authenticatePinUser(String pin, String state) {
        final String encodedRedirectUrl = URLEncoder.encode(cmcPinVerifyConfiguration.getRedirectUrl() + "/receiver", StandardCharsets.UTF_8);
        log.info("Redirect URL: " + encodedRedirectUrl);

        Response response = idamApi.authenticatePinUser(
            pin,
            cmcPinVerifyConfiguration.getClientId(),
            encodedRedirectUrl,
            state
        );
        if (response != null && response.status() == HttpStatus.UNAUTHORIZED.value()) {
            throw new RetryablePinException();
        }
        return response;
    }
}
