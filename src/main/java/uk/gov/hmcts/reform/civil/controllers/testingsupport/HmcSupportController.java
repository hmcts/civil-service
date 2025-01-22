package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

@Tag(name = "Hmc Support Controller")
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnExpression("${hmc.support.enabled:false}")
public class HmcSupportController {

    private final HearingsService hearingsService;

    @RequestMapping(
        method = RequestMethod.GET,
        value = "/hmc-support/hearing-details",
        produces = "application/json")
    public ResponseEntity<HearingGetResponse> getHearingDetails(
        @RequestHeader(value = "authorization") String authorization,
        @RequestParam(value = "hearingId") String hearingId
    ) {
        HearingGetResponse response = hearingsService.getHearingResponse(hearingId, authorization);
        return ResponseEntity.ok(response);
    }

}
