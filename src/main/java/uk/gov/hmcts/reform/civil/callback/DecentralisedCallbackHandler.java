package uk.gov.hmcts.reform.civil.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import uk.gov.hmcts.ccd.sdk.json.JsonCallbackHandler;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.http.HttpResponseHeadersService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.helpers.CaseTypeIdentifier.isGeneralApplication;

/**
 * Runs the about-to-submit and submitted callbacks of decentralised CIVIL and GENERALAPPLICATION events.
 *
 * <p>The decentralised runtime calls this in place of CCD calling {@code CallbackController}. It builds the same
 * {@link CallbackParams} the controller builds from the callback URL in the definition, and dispatches them through
 * {@link CallbackHandlerFactory}, so the factory's aspects and duplicate-event check apply as they do over HTTP.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DecentralisedCallbackHandler implements JsonCallbackHandler {

    // The paths CallbackController maps. Versioned paths come first: Spring MVC prefers them as more specific,
    // and /version/V_1/about-to-submit would otherwise match {callback-type}/{page-id}.
    private static final List<PathPattern> CALLBACK_PATHS = List.of(
        PathPatternParser.defaultInstance.parse("/cases/callbacks/version/{version}/{callback-type}"),
        PathPatternParser.defaultInstance.parse("/cases/callbacks/version/{version}/{callback-type}/{page-id}"),
        PathPatternParser.defaultInstance.parse("/cases/callbacks/{callback-type}"),
        PathPatternParser.defaultInstance.parse("/cases/callbacks/{callback-type}/{page-id}")
    );

    private final CallbackHandlerFactory callbackHandlerFactory;
    private final CaseDetailsConverter caseDetailsConverter;
    private final HttpResponseHeadersService responseHeadersService;

    @Override
    public boolean handles(String callbackPath) {
        return match(callbackPath).isPresent();
    }

    @Override
    public Object handle(String callbackPath, CallbackRequest request, HttpHeaders headers) {
        Map<String, String> pathVariables = match(callbackPath)
            .orElseThrow(() -> new CallbackException("Not a civil callback path: " + callbackPath))
            .getUriVariables();
        String authorisation = headers.getFirst(HttpHeaders.AUTHORIZATION);
        CaseDetails caseDetails = request.getCaseDetails();
        CaseDetails caseDetailsBefore = request.getCaseDetailsBefore();
        boolean generalApplicationCaseType = isGeneralApplication(caseDetails);

        log.info("Running decentralised callback, caseId: {}, caseType: {}, eventId: {}, path: {}",
                 caseDetails.getId(), caseDetails.getCaseTypeId(), request.getEventId(), callbackPath);

        CallbackParams callbackParams = new CallbackParams()
            .request(request)
            .type(CallbackType.fromValue(pathVariables.get("callback-type")))
            .params(Map.of(CallbackParams.Params.BEARER_TOKEN, authorisation))
            .version(Optional.ofNullable(pathVariables.get("version")).map(CallbackVersion::valueOf).orElse(null))
            .pageId(pathVariables.get("page-id"))
            .caseData(caseDetailsConverter.toBaseCaseData(caseDetails))
            .caseDataBefore(caseDetailsBefore != null ? caseDetailsConverter.toBaseCaseData(caseDetailsBefore) : null)
            .isGeneralApplicationCaseType(generalApplicationCaseType)
            .isCivilCaseType(!generalApplicationCaseType);

        CallbackResponse response = callbackHandlerFactory.dispatch(callbackParams);
        // As CallbackController does, ask ExUI to complete the WA task a submitted callback names.
        return responseHeadersService.clientContextHeader(response)
            .<Object>map(clientContext -> ResponseEntity.ok()
                .header(HttpResponseHeadersService.CLIENT_CONTEXT, clientContext)
                .body(response))
            .orElse(response);
    }

    private Optional<PathPattern.PathMatchInfo> match(String callbackPath) {
        PathContainer path = PathContainer.parsePath(callbackPath);
        return CALLBACK_PATHS.stream()
            .map(pattern -> pattern.matchAndExtract(path))
            .filter(Objects::nonNull)
            .findFirst();
    }
}
