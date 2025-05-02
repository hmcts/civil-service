package uk.gov.hmcts.reform.civil.notification.handlers.defresponse.spec.online.fulldefencefulladmitpartadmit;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;

@Component
@AllArgsConstructor
public class SpecRespEmailHelper {

    protected static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public String getAppSolTemplate(CaseData caseData) {
        String emailTemplate;
        if (caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && (FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            || FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec()))
        ) {
            emailTemplate = notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpec();
        } else {
            if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
                emailTemplate = notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec();
            } else {
                emailTemplate = notificationsProperties.getClaimantSolicitorDefendantResponseForSpec();
            }
        }
        return emailTemplate;
    }

    public String getLipTemplate(CaseData caseData) {
        if (caseData.isClaimantBilingual()) {
            return notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate();
        } else {
            return notificationsProperties.getClaimantLipClaimUpdatedTemplate();
        }
    }

    public String getRespondentTemplate(CaseData caseData) {
        String emailTemplate;
        if (MultiPartyScenario.getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)) {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        } else if ((caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == BY_SET_DATE
            || caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == SUGGESTION_OF_REPAYMENT_PLAN)
            &&
            (RespondentResponseTypeSpec.PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && caseData.isApplicantRepresented())
        ) {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefResponseSpecWithClaimantAction();
        } else {
            emailTemplate = notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        }
        return emailTemplate;
    }
}
