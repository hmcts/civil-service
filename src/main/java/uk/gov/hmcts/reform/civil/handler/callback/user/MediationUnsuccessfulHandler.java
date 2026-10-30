package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationNotConductedReason;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulOutcome;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_UNSUCCESSFUL;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class MediationUnsuccessfulHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(MEDIATION_UNSUCCESSFUL);
    private static final String UNSUCCESSFUL_MEDIATION_OUTCOME = "Unsuccessful";
    private static final String CONFIRMATION_HEADER = "# Unsuccessful mediation outcome is recorded";

    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(CallbackType.ABOUT_TO_START), this::populateShowConditionFlags,
            callbackKey(CallbackType.ABOUT_TO_SUBMIT), this::submitUnsuccessfulMediation,
            callbackKey(CallbackType.SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateShowConditionFlags(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            caseData.setShowCarmFields(YES);
        } else {
            caseData.setShowCarmFields(NO);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitUnsuccessfulMediation(CallbackParams callbackParams) {
        CaseData caseDataUpdated = callbackParams.getCaseData();
        if (featureToggleService.isCaseworkerMediationActivitiesEnabled()) {
            recordUnsuccessfulMediationOutcome(caseDataUpdated);
        }
        caseDataUpdated.setBusinessProcess(BusinessProcess.ready(MEDIATION_UNSUCCESSFUL));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .state(CaseState.JUDICIAL_REFERRAL.name())
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        if (featureToggleService.isCaseworkerMediationActivitiesEnabled()) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(CONFIRMATION_HEADER)
                .build();
        }
        return emptySubmittedCallbackResponse(callbackParams);
    }

    private void recordUnsuccessfulMediationOutcome(CaseData caseData) {
        Mediation mediation = caseData.getMediation();
        if (mediation == null) {
            mediation = new Mediation();
            caseData.setMediation(mediation);
        }
        mediation.setMediationOutcome(UNSUCCESSFUL_MEDIATION_OUTCOME);
        clearUnselectedMediationReasons(mediation);
    }

    private void clearUnselectedMediationReasons(Mediation mediation) {
        MediationUnsuccessfulOutcome outcome = mediation.getMediationUnsuccessfulOutcome();
        if (outcome != MediationUnsuccessfulOutcome.NOT_CONDUCTED) {
            mediation.setMediationNotConductedReason(null);
            mediation.setMediationNotAttendedReason(null);
            mediation.setMediationCancelledReason(null);
            mediation.setMediationAbandonedReason(null);
        } else {
            MediationNotConductedReason reason = mediation.getMediationNotConductedReason();
            if (reason != MediationNotConductedReason.NOT_ATTENDED) {
                mediation.setMediationNotAttendedReason(null);
            }
            if (reason != MediationNotConductedReason.CANCELLED) {
                mediation.setMediationCancelledReason(null);
            }
            if (reason != MediationNotConductedReason.ABANDONED) {
                mediation.setMediationAbandonedReason(null);
            }
        }
        if (outcome != MediationUnsuccessfulOutcome.NOT_SETTLED) {
            mediation.setMediationNotSettledReason(null);
        }
    }
}
