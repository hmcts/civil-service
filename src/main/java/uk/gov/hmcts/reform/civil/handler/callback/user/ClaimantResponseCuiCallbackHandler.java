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
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimantResponseCuiCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE_CUI);

    private final ObjectMapper objectMapper;

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
        updatedCaseData.showResponseOneVOneFlag(setUpOneVOneFlow(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData updatedData = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE_CUI))
            .build();

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.toMap(objectMapper));

        updateClaimEndState(response, updatedData);

        return response.build();
    }

    private void updateClaimEndState(AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response, CaseData updatedData) {
        if (updatedData.hasClaimantAgreedToFreeMediation()) {
            response.state(CaseState.IN_MEDIATION.name());
        } else if (updatedData.hasApplicantRejectedRepaymentPlan() && (updatedData.getRespondent1().isCompany() || updatedData.getRespondent1().isOrganisation())) {
            response.state(CaseState.All_FINAL_ORDERS_ISSUED.name());
        } else {
            response.state(CaseState.JUDICIAL_REFERRAL.name());
        }
    }

    private ResponseOneVOneShowTag setUpOneVOneFlow(CaseData caseData) {
        if (ONE_V_ONE.equals(getMultiPartyScenario(caseData))) {
            if (caseData.getRespondent1ClaimResponseTypeForSpec() == null) {
                return null;
            }
            return switch (caseData.getRespondent1ClaimResponseTypeForSpec()) {
                case FULL_DEFENCE -> ResponseOneVOneShowTag.ONE_V_ONE_FULL_DEFENCE;
                case FULL_ADMISSION -> setUpOneVOneFlowForFullAdmit(caseData);
                case PART_ADMISSION -> setUpOneVOneFlowForPartAdmit(caseData);
                case COUNTER_CLAIM -> ResponseOneVOneShowTag.ONE_V_ONE_COUNTER_CLAIM;
                default -> null;
            };
        }
        return null;
    }

    private ResponseOneVOneShowTag setUpOneVOneFlowForPartAdmit(CaseData caseData) {
        if (YES.equals(caseData.getSpecDefenceAdmittedRequired())) {
            return ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_HAS_PAID;
        }
        return switch (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()) {
            case IMMEDIATELY -> ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY;
            case BY_SET_DATE -> ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_BY_SET_DATE;
            case SUGGESTION_OF_REPAYMENT_PLAN -> ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_INSTALMENT;
            default -> ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT;
        };
    }

    private ResponseOneVOneShowTag setUpOneVOneFlowForFullAdmit(CaseData caseData) {
        if (YES.equals(caseData.getSpecDefenceFullAdmittedRequired())) {
            return ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_HAS_PAID;
        }
        return switch (caseData.getDefenceAdmitPartPaymentTimeRouteRequired()) {
            case IMMEDIATELY -> ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_IMMEDIATELY;
            case BY_SET_DATE -> ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_BY_SET_DATE;
            case SUGGESTION_OF_REPAYMENT_PLAN -> ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT_PAY_INSTALMENT;
            default -> ResponseOneVOneShowTag.ONE_V_ONE_FULL_ADMIT;
        };
    }
}
