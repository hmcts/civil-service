package uk.gov.hmcts.reform.civil.handler.callback.camunda.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.GaCallbackDataUtil;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.service.PaymentsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SERVICE_REQUEST_CUI_GENERAL_APP;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceRequestCUICallbackHandler extends CallbackHandler {

    private static final String ERROR_MESSAGE = "Technical error occurred";
    private static final String TASK_ID = "CreateServiceRequestCUI";

    private final PaymentsService paymentsService;
    private final ObjectMapper objectMapper;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::makePaymentServiceReq
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return List.of(
            CREATE_SERVICE_REQUEST_CUI_CLAIM_ISSUE,
            CREATE_SERVICE_REQUEST_CUI_GENERAL_APP
        );
    }

    private CallbackResponse makePaymentServiceReq(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = GaCallbackDataUtil.resolveGaCaseData(callbackParams, objectMapper);
        CaseData caseData = GaCallbackDataUtil.mergeToCaseData(gaCaseData, callbackParams.getCaseData(), objectMapper);
        if (caseData == null) {
            throw new IllegalArgumentException("Case data missing from callback params");
        }
        var authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        List<String> errors = new ArrayList<>();

        if (isEvent(callbackParams, CREATE_SERVICE_REQUEST_CUI_GENERAL_APP)) {
            try {
                if (isGaServiceRequestNotRequested(gaCaseData, caseData) && !isGaHelpWithFees(gaCaseData, caseData)) {
                    log.info("Calling GA payment service request (claim fee) for case {}", caseData.getCcdCaseReference());
                    String serviceRequestReference = getServiceRequestReference(caseData, authToken);
                    GAPbaDetails updatedDetails = buildUpdatedGaPbaDetails(serviceRequestReference, gaCaseData, caseData);
                    caseData = caseData.toBuilder()
                        .generalAppPBADetails(updatedDetails)
                        .build();
                    if (gaCaseData != null) {
                        gaCaseData = gaCaseData.toBuilder()
                            .generalAppPBADetails(updatedDetails)
                            .build();
                        caseData = GaCallbackDataUtil.mergeToCaseData(gaCaseData, caseData, objectMapper);
                    }
                }
            } catch (FeignException e) {
                log.error("Failed creating GA payment service request for case {}. Http status: {}. Exception: {}",
                    caseData.getCcdCaseReference(), e.status(), e
                );
                errors.add(ERROR_MESSAGE);
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .errors(errors)
                .build();
        }

        try {
            if (isServiceRequestNotRequested(caseData) && !caseData.isHelpWithFees()) {
                log.info("Calling payment service request (claim fee) for case {}", caseData.getCcdCaseReference());
                String serviceRequestReference = getServiceRequestReference(caseData, authToken);
                caseData = caseData.toBuilder().serviceRequestReference(serviceRequestReference)
                    .claimIssuedPBADetails(getClaimIssuePbaDetails(serviceRequestReference, caseData.getClaimFee()))
                    .build();
            }
        } catch (FeignException e) {
            log.error("Failed creating a payment service request for case {}. Http status: {}. Exception: {}",
                      caseData.getCcdCaseReference(), e.status(), e
            );
            errors.add(ERROR_MESSAGE);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private String getServiceRequestReference(CaseData caseData, String authToken) {
        return paymentsService.createServiceRequest(caseData, authToken)
            .getServiceRequestReference();
    }

    private boolean isServiceRequestNotRequested(CaseData caseData) {
        return isNull(caseData.getServiceRequestReference());
    }

    private SRPbaDetails getClaimIssuePbaDetails(String serviceReference, Fee claimFee) {
        return SRPbaDetails.builder()
            .serviceReqReference(serviceReference)
            .fee(claimFee)
            .build();
    }

    private boolean isGaServiceRequestNotRequested(GeneralApplicationCaseData gaCaseData, CaseData caseData) {
        if (gaCaseData != null && gaCaseData.getGeneralAppPBADetails() != null) {
            return isNull(gaCaseData.getGeneralAppPBADetails().getServiceReqReference());
        }
        return caseData.getGeneralAppPBADetails() == null
            || caseData.getGeneralAppPBADetails().getServiceReqReference() == null;
    }

    private boolean isGaHelpWithFees(GeneralApplicationCaseData gaCaseData, CaseData caseData) {
        if (gaCaseData != null && gaCaseData.getGeneralAppHelpWithFees() != null) {
            return gaCaseData.getGeneralAppHelpWithFees().getHelpWithFee() == YesOrNo.YES;
        }
        return Optional.ofNullable(caseData.getGeneralAppHelpWithFees())
            .map(HelpWithFees -> HelpWithFees.getHelpWithFee() == YesOrNo.YES)
            .orElse(false);
    }

    private GAPbaDetails buildUpdatedGaPbaDetails(String serviceReference,
                                                  GeneralApplicationCaseData gaCaseData,
                                                  CaseData caseData) {
        GAPbaDetails existingDetails = Optional.ofNullable(gaCaseData)
            .map(GeneralApplicationCaseData::getGeneralAppPBADetails)
            .orElse(caseData.getGeneralAppPBADetails());

        GAPbaDetails.GAPbaDetailsBuilder builder = Optional.ofNullable(existingDetails)
            .map(GAPbaDetails::toBuilder)
            .orElse(GAPbaDetails.builder());

        if (existingDetails == null || existingDetails.getFee() == null) {
            Optional.ofNullable(caseData.getGeneralAppPBADetails())
                .map(GAPbaDetails::getFee)
                .ifPresent(builder::fee);
        }

        return builder
            .serviceReqReference(serviceReference)
            .build();
    }
}
