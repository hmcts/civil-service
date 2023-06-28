package uk.gov.hmcts.reform.hmc.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.hmc.client.HearingsApi;
import uk.gov.hmcts.reform.hmc.exception.HmcException;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingsService {

    private final HearingsApi hearingNoticeApi;

    private final String serviceAuth = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjaXZpbF9zZXJ2aWNlIiwiZXhwIjoxNjg3OTU5M" +
        "zY2fQ.eYIqrUe-Ab2Ghw6sn-kOZWOckBsgx5WonZUVaM63FOyYAeuYjbY-wPsAaqYBeQaNSTG3a2_rzLEeOHPt18wgTw";
    private final String userAuth = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJaNEJjalZnZnZ1NVpleEt6QkVFbE1TbTQzTHM9Ii" +
        "wiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJjaXZpbC1zeXN0ZW11cGRhdGVAanVzdGljZS5nb3YudWsiLCJjdHMiOiJPQVVUSDJfU1RBV" +
        "EVMRVNTX0dSQU5UIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiNmY5NWRhNTAtNDA2NC00ZDZhLWEwMTQtMGI5ODB" +
        "kMGI1MTU5LTM4MDk1Mjc0Iiwic3VibmFtZSI6ImNpdmlsLXN5c3RlbXVwZGF0ZUBqdXN0aWNlLmdvdi51ayIsImlzcyI6Imh0dHBzO" +
        "i8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tZGVtby5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvcmV" +
        "hbG1zL3Jvb3QvcmVhbG1zL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1d" +
        "GhHcmFudElkIjoiV1FxVGpkZFVZRTJmVGlTTF9IOEpNOEhrNnlFIiwiYXVkIjoiaG1jdHMiLCJuYmYiOjE2ODc5NDQ5NzAsImdyYW5" +
        "0X3R5cGUiOiJwYXNzd29yZCIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE2ODc5NDQ5N" +
        "zAsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNjg3OTczNzcwLCJpYXQiOjE2ODc5NDQ5NzAsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGk" +
        "iOiJTbktVS2hrNGZITndkNTdZZ0RzcG9oOGlMTW8ifQ.HEoMl-j-8JuksDWt-AHVe_I8hu3F7UMQSonX74QrASuEOWXTUgmecil2FY" +
        "ewNY_ZFjqFMBHKhmjd3-FQ8h41zpCVbleiQxKbmWQO717pbZVOeXr-Qq_lFzx5iU3SQ1aBt0O7B_ib7XrRMeVI7Yi3iVHSTWYUBH0n" +
        "KfmHUnDG5Fxe6Ba1WAgTFjcYsM5EOsMifWOtkWa17H2p3t24c5Gb85PbUK4Jw3uDCbBNgheARvpGYzXhAZP52AIx6drWcz5XQdIpMi" +
        "HLPJEJ1gdsTvN0OGB5NuBGf_oaFXEk5homWSudov2tEjrtLVQRtqXvsInv_qgqyUq-ne4nrtU53IRi1w";

    public HearingGetResponse getHearingResponse(String authToken, String hearingId) throws HmcException {
        log.debug("Sending Get Hearings with Hearing ID {}", hearingId);
        try {
            return hearingNoticeApi.getHearingRequest(
                userAuth,
                serviceAuth,
                hearingId,
                null);
        } catch (FeignException ex)  {
            log.error("Failed to retrieve hearing with Id: {} from HMC", hearingId);
            throw new HmcException(ex);
        }
    }

    public PartiesNotifiedResponses getPartiesNotifiedResponses(String authToken, String hearingId) {
        log.debug("Requesting Get Parties Notified with Hearing ID {}", hearingId);
        try {
            return hearingNoticeApi.getPartiesNotifiedRequest(
                userAuth,
                serviceAuth,
                hearingId);
        } catch (FeignException e) {
            log.error("Failed to retrieve patries notified with Id: %s from HMC", hearingId);
            throw new HmcException(e);
        }
    }

    public ResponseEntity updatePartiesNotifiedResponse(String authToken, String hearingId,
                                                        int requestVersion, LocalDateTime receivedDateTime,
                                                        PartiesNotified payload) {
        try {
            return hearingNoticeApi.updatePartiesNotifiedRequest(
                userAuth,
                serviceAuth,
                payload,
                hearingId,
                requestVersion,
                receivedDateTime
            );
        } catch (FeignException ex)  {
            log.error("Failed to update partiesNotified with Id: {} from HMC", hearingId);
            throw new HmcException(ex);
        }
    }

    public UnNotifiedHearingResponse getUnNotifiedHearingResponses(String authToken, String hmctsServiceCode,
                                                                   LocalDateTime hearingStartDateFrom,
                                                                   LocalDateTime hearingStartDateTo) {
        log.debug("Requesting UnNotified Hearings");
        return hearingNoticeApi.getUnNotifiedHearingRequest(
            userAuth,
            serviceAuth,
            hmctsServiceCode,
            hearingStartDateFrom,
            hearingStartDateTo
        );
    }
}
