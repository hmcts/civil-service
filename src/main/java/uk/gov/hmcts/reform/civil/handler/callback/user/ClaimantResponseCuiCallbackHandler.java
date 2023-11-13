package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimantResponseCuiCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE_CUI);

    private final ResponseOneVOneShowTagService responseOneVOneService;

    private final ObjectMapper objectMapper;
    private final UpdateCaseManagementDetailsService updateCaseManagementLocationDetailsService;

    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateCaseData,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateCaseData(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
        updatedCaseData.showResponseOneVOneFlag(responseOneVOneService.setUpOneVOneFlow(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime applicant1ResponseDate = LocalDateTime.now();
        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                .applicant1ResponseDate(applicant1ResponseDate)
                .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE_CUI))
                .respondent1RespondToSettlementAgreementDeadline(getRespondToSettlementAgreementDeadline(caseData, applicant1ResponseDate));

        updateCaseManagementLocationDetailsService.updateCaseManagementDetails(builder, callbackParams);

        CaseData updatedData = builder.build();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
                AboutToStartOrSubmitCallbackResponse.builder()
                        .data(updatedData.toMap(objectMapper));

        updateClaimEndState(response, updatedData);

        return response.build();
    }

    private LocalDateTime getRespondToSettlementAgreementDeadline(CaseData caseData, LocalDateTime responseDate) {
        return caseData.hasApplicant1SignedSettlementAgreement()
                ? deadlinesCalculator.getRespondToSettlementAgreementDeadline(responseDate) : null;
    }

    private void updateClaimEndState(AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response, CaseData updatedData) {
        if (updatedData.hasClaimantAgreedToFreeMediation()) {
            response.state(CaseState.IN_MEDIATION.name());
        } else if (updatedData.hasApplicant1SignedSettlementAgreement() && updatedData.hasApplicantAcceptedRepaymentPlan()) {
            response.state(CaseState.All_FINAL_ORDERS_ISSUED.name());
        } else if (updatedData.hasApplicantRejectedRepaymentPlan() && updatedData.getRespondent1().isCompanyOROrganisation()) {
            response.state(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM.name());
        } else {
            response.state(CaseState.JUDICIAL_REFERRAL.name());
        }
    }

}
