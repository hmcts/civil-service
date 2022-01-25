package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.math.BigDecimal;

@Api
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/cases",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class CasesController {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @GetMapping(path = {
        "/{cid}",
    })
    @ApiOperation("get case by id from CCD")

    public ResponseEntity<CaseData> getClaimById(
        @PathVariable("cid") Long claimId
    ) {
        log.info(
            "Received CaseId: {}",
            claimId
        );

        var caseDataResponse = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(claimId).getData());
        log.info(
            "CaseDataResponse : {}",
            caseDataResponse
        );
        return new ResponseEntity<>(caseDataResponse, HttpStatus.OK);
    }
}
