package uk.gov.hmcts.reform.civil.controllers;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.civil.callback.CallbackHandlerFactory;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;

import java.util.Optional;
import javax.validation.constraints.NotNull;

@Api
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
    @ApiOperation("Handles all callbacks from CCD")
    public CallbackResponse callback(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @PathVariable("callback-type") String callbackType,
        @NotNull @RequestBody CallbackRequest callback,
        @PathVariable("version") Optional<CallbackVersion> version,
        @PathVariable("page-id") Optional<String> pageId
    ) {
        log.info("Received callback from CCD, eventId: {}, callback type: {}, page id: {}, version: {}",
                 callback.getEventId(), callbackType, pageId, version
        );
        CallbackParams callbackParams = CallbackParams.builder()
            .request(callback)
            .type(CallbackType.fromValue(callbackType))
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, authorisation))
            .version(version.orElse(null))
            .pageId(pageId.orElse(null))
            .caseData(caseDetailsConverter.toCaseData(callback.getCaseDetails()))
            .build();

        return callbackHandlerFactory.dispatch(callbackParams);
    }
}
