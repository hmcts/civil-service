package uk.gov.hmcts.reform.civil.controllers.cases;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BundleCreateResponse;
import uk.gov.hmcts.reform.civil.model.BundlingInformation;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.stitching.BundlingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/bundle")

public class BundlingController {

    @Autowired
    private BundlingService bundlingService;

    @PostMapping(path = "/createBundle", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Creating bundle. ")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bundle Created Successfully ."),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse createBundle(@RequestHeader("Authorization")
                                                                 @Parameter(hidden = true)
                                                                 String authorization,
                                                             @RequestHeader("ServiceAuthorization")
                                                             @Parameter(hidden = true)
                                                             String serviceAuthorization,
                                                             @RequestBody CallbackRequest callbackRequest,
                                                             @RequestBody CallbackParams callbackParams)
        throws Exception {

        //log.info("*** callRecieved to createBundle api in civil-service : {}", callbackRequest.toString());
        CaseData caseData = callbackParams.getCaseData();
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        moveExistingCaseBundlesToHistoricalBundles(caseData);
        log.info("*** Creating Bundle for the case reference : {}", caseData.getCcdCaseReference());
        BundleCreateResponse bundleCreateResponse = bundlingService.createBundleServiceRequest(caseData,
            callbackRequest.getEventId(), authorization);
        log.info("*** Bundle response from api : {}", new ObjectMapper().writeValueAsString(bundleCreateResponse));
        if (null != bundleCreateResponse && null != bundleCreateResponse.getData()
            && null != bundleCreateResponse.getData().getCaseBundles()) {
            caseDataUpdated.put("bundleInformation",
                BundlingInformation.builder().caseBundles(bundleCreateResponse.getData().getCaseBundles())
                    .historicalBundles(caseData.getBundleInformation().getHistoricalBundles())
                    .bundleConfiguration(bundleCreateResponse.data.getBundleConfiguration()).build());
            log.info(
                "*** Bundle created successfully.. Updating bundle Information in case data for the case reference: {}",
                      caseData.getCcdCaseReference());
        }
        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
    }

    private void moveExistingCaseBundlesToHistoricalBundles(CaseData caseData) {
        List<Bundle> historicalBundles = new ArrayList<>();
        BundlingInformation existingBundleInformation = caseData.getBundleInformation();
        if (nonNull(existingBundleInformation)) {
            if (nonNull(existingBundleInformation.getHistoricalBundles())) {
                historicalBundles.addAll(existingBundleInformation.getHistoricalBundles());
            }
            if (nonNull(existingBundleInformation.getCaseBundles())) {
                historicalBundles.addAll(existingBundleInformation.getCaseBundles());
            }
            existingBundleInformation.setHistoricalBundles(historicalBundles);
            existingBundleInformation.setCaseBundles(null);
        } else {
            caseData.setBundleInformation(BundlingInformation.builder().build());
        }
    }
}
