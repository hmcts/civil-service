package uk.gov.hmcts.reform.hmc.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
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
    private final AuthTokenGenerator authTokenGenerator;

    private final String DEMO_USER_AUTH = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJaNEJjalZnZnZ1NVpleEt6QkVFbE1TbTQzTHM9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJjaXZpbC1zeXN0ZW11cGRhdGVAanVzdGljZS5nb3YudWsiLCJjdHMiOiJPQVVUSDJfU1RBVEVMRVNTX0dSQU5UIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiNmY5NWRhNTAtNDA2NC00ZDZhLWEwMTQtMGI5ODBkMGI1MTU5LTMyOTE3MzAxIiwic3VibmFtZSI6ImNpdmlsLXN5c3RlbXVwZGF0ZUBqdXN0aWNlLmdvdi51ayIsImlzcyI6Imh0dHBzOi8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tZGVtby5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvcmVhbG1zL3Jvb3QvcmVhbG1zL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoiSzdfU3VQZzR5d1l0UU42X3M5SXJUblhEeHh3IiwiYXVkIjoiaG1jdHMiLCJuYmYiOjE2ODc1MTY1NDksImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE2ODc1MTY1NDksInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNjg3NTQ1MzQ5LCJpYXQiOjE2ODc1MTY1NDksImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJNZVhhSGJrWUNkdWs5Sk1xRlFKUFVlOENGdzQifQ.fKaGC2bdL-qrudGfTpqMjHRJrAZVZjPUjEWpwrXV9siHKTmRczQ7q7YNYFieyfL-6c_8vmDjkT8o9Ece2jPdqSJ-frg8m3DcJc8R4tvaYwCLSDLkz5VGcIZ_xngHZYRgDDWzeoS_nfGZghEgOQNluGgIoFkIuprc0f8xIIJyaAvvFsC99n74J5MZZfgJqiPxi0dab6kzUHgwxoKi1cOlb1Nlznjj0lZgoIsrepvjvc7V6DvhSWShVReHQ-2pPpuMW9Y4EhG-8qCqSRYRvwpt-wkJsdKCh_mwuB9OzSVEN6UwyGBI8eOjGiD_CYKgCQdm0OUdnJq5r3H2Ug1te1XvbQ";
    private final String DEMO_SERVICE_AUTH = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjaXZpbF9zZXJ2aWNlIiwiZXhwIjoxNjg3NTI4MDAzfQ.aJESFEXyU7gLnxcmx4fIDkXk2PgCGjr84uPsVdlqGgG9AavSiCW4XQc_Zr6hLNpPzPj-0Rivf1zKawPa5pUqTg";

    public HearingGetResponse getHearingResponse(String authToken, String hearingId) throws HmcException {
        log.debug("Sending Get Hearings with Hearing ID {}", hearingId);
        try {
            return hearingNoticeApi.getHearingRequest(
                DEMO_USER_AUTH,
                DEMO_SERVICE_AUTH,
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
                DEMO_USER_AUTH,
                DEMO_SERVICE_AUTH,
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
                DEMO_USER_AUTH,
                DEMO_SERVICE_AUTH,
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
        try {
            return hearingNoticeApi.getUnNotifiedHearingRequest(
                DEMO_USER_AUTH,
                DEMO_SERVICE_AUTH,
                hmctsServiceCode,
                hearingStartDateFrom,
                hearingStartDateTo);
        } catch (FeignException e) {
            log.error("Failed to retrieve unnotified hearings");
            throw new HmcException(e);
        }
    }
}
