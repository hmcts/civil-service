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
import uk.gov.hmcts.reform.civil.callback.SystemCallbackFailureResponder;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.http.HttpResponseHeadersService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;

import static uk.gov.hmcts.reform.civil.helpers.CaseTypeIdentifier.isGeneralApplication;

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
    private final HttpResponseHeadersService responseHeadersService;
    private final SystemCallbackFailureResponder systemCallbackFailureResponder;

    @PostMapping(path = {
        "/{callback-type}",
        "/{callback-type}/{page-id}",
        "/version/{version}/{callback-type}",
        "/version/{version}/{callback-type}/{page-id}"
    })
    @Operation(summary = "Handles all callbacks from CCD")
    public CallbackResponse callback(
        HttpServletResponse response,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(value = "user-roles", required = false) String userRoles,
        @PathVariable("callback-type") String callbackType,
        @NotNull @RequestBody CallbackRequest callback,
        @PathVariable("version") Optional<CallbackVersion> version,
        @PathVariable("page-id") Optional<String> pageId
    ) {
        final CaseDetails caseDetails = callback.getCaseDetails();
        final CaseDetails caseDetailsBefore = callback.getCaseDetailsBefore();
        final Long caseId = caseDetails.getId();
        final boolean generalApplicationCaseType = isGeneralApplication(caseDetails);

        MDC.put("caseId", Objects.toString(caseId, ""));
        log.info("Received callback from CCD, caseId: {}, caseIdBefore: {}, caseType: {}, eventId: {}, callback type: {}, page id: {}, version: {}",
                 caseId,
                 caseDetailsBefore == null ? null : caseDetailsBefore.getId(),
                 caseDetails.getCaseTypeId(),
                 callback.getEventId(),
                 callbackType,
                 pageId,
                 version
        );

        final CallbackType type = CallbackType.fromValue(callbackType);
        CallbackResponse callbackResponse;
        try {
            CallbackParams callbackParams = new CallbackParams()
                .request(callback)
                .type(type)
                .params(Map.of(CallbackParams.Params.BEARER_TOKEN, authorisation))
                .version(version.orElse(null))
                .pageId(pageId.orElse(null))
                .caseData(caseDetailsConverter.toBaseCaseData(caseDetails))
                .caseDataBefore(caseDetailsBefore != null ? caseDetailsConverter.toBaseCaseData(caseDetailsBefore) : null)
                .isGeneralApplicationCaseType(generalApplicationCaseType)
                .isCivilCaseType(!generalApplicationCaseType);

            callbackResponse = callbackHandlerFactory.dispatch(callbackParams);
        } catch (RuntimeException e) {
            // CCD reports any non-2xx callback response as a 502 and drops the body, so for system-user
            // events the real exception is returned as a labelled callback error instead (DTSCCI-7095).
            if (!systemCallbackFailureResponder.appliesTo(type, userRoles)) {
                throw e;
            }
            callbackResponse = systemCallbackFailureResponder.toCallbackError(e, caseId, callback.getEventId(), type);
        }
        responseHeadersService.addClientContextHeader(callbackResponse, response);
        return callbackResponse;
    }

}
