package uk.gov.hmcts.reform.civil.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackHandlerFactory;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;

@Tag(name = "Callback Controller")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/cases/callbacks",
    produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE
)
public class CallbackController {

    private final CallbackHandlerFactory callbackHandlerFactory;
    private final CaseDetailsConverter caseDetailsConverter;

    @PostMapping(path = {
        "/{callback-type}",
        "/{callback-type}/{page-id}",
        "/version/{version}/{callback-type}",
        "/version/{version}/{callback-type}/{page-id}"
    })
    @Operation(summary = "Handles all callbacks from CCD")
    public CallbackResponse callback(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @PathVariable("callback-type") String callbackType,
        @NotNull @RequestBody CallbackRequest callback,
        @PathVariable("version") Optional<CallbackVersion> version,
        @PathVariable("page-id") Optional<String> pageId
    ) {
        final CaseDetails caseDetails = callback.getCaseDetails();
        final CaseDetails caseDetailsBefore = callback.getCaseDetailsBefore();
        MDC.put("caseId", Objects.toString(caseDetails.getId(), ""));
        log.info("Received callback from CCD, eventId: {}, callback type: {}, page id: {}, version: {}",
            callback.getEventId(), callbackType, pageId, version
        );

        CallbackParams callbackParams = CallbackParams.builder()
            .request(callback)
            .type(CallbackType.fromValue(callbackType))
            .params(java.util.Map.of(CallbackParams.Params.BEARER_TOKEN, authorisation))
            .version(version.orElse(null))
            .pageId(pageId.orElse(null))
            .caseData(caseDetailsConverter.toCaseData(caseDetails))
            .caseDataBefore(caseDetailsBefore != null ? caseDetailsConverter.toCaseData(caseDetailsBefore) : null)
            .build();

        return callbackHandlerFactory.dispatch(callbackParams);
    }
}
