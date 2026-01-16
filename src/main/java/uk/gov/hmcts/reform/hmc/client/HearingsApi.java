package uk.gov.hmcts.reform.hmc.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearings.HearingsResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotified;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.UnNotifiedHearingResponse;
import java.time.LocalDateTime;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "hmc-api", url = "${hmc.api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface HearingsApi {

    String HEARING_ENDPOINT = "/hearing";
    String PARTIES_NOTIFIED_ENDPOINT = "/partiesNotified";
    String UNNOTIFIED_HEARINGS_ENDPOINT = "/unNotifiedHearings";
    String HEARINGS_ENDPOINT = "/hearings";
    String ROLE_ASSIGNMENT_URL = "Role-Assignment-Url";
    String DATA_STORE_URL = "Data-Store-Url";
    String HMCTS_DEPLOYMENT_ID = "hmctsDeploymentId";

    @GetMapping(HEARING_ENDPOINT + "/{id}")
    HearingGetResponse getHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @PathVariable String id,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestParam(name = "isValid", required = false) Boolean isValid
    );

    @GetMapping(value = PARTIES_NOTIFIED_ENDPOINT + "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    PartiesNotifiedResponses getPartiesNotifiedRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @PathVariable("id") String id
    );

    @PutMapping(value = PARTIES_NOTIFIED_ENDPOINT + "/{id}", consumes = "application/json")
    ResponseEntity updatePartiesNotifiedRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @RequestBody PartiesNotified partiesNotified,
        @PathVariable("id") String hearingId,
        @RequestParam("version") int requestVersion,
        @RequestParam("received") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime receivedDateTime
    );

    @GetMapping(value = UNNOTIFIED_HEARINGS_ENDPOINT + "/{hmctsServiceCode}")
    UnNotifiedHearingResponse getUnNotifiedHearingRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @PathVariable String hmctsServiceCode,
        @RequestParam("hearing_start_date_from") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime hearingStartDateFrom,
        @RequestParam("hearing_start_date_to") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime hearingStartDateTo
    );

    @GetMapping(value = HEARINGS_ENDPOINT + "/{caseId}")
    HearingsResponse getHearings(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestHeader(value = DATA_STORE_URL, required = false) String dataStoreUrl,
        @RequestHeader(value = ROLE_ASSIGNMENT_URL, required = false) String roleAssignmentUrl,
        @RequestHeader(value = HMCTS_DEPLOYMENT_ID, required = false) String hmctsDeploymentId,
        @PathVariable Long caseId,
        @RequestParam("status") String status
    );
}
