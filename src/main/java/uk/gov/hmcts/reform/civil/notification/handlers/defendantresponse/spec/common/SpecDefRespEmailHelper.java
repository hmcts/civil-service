package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
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
public class SpecDefRespEmailHelper {

    public static final String REFERENCE_TEMPLATE = "defendant-response-spec-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public String getAppSolTemplate(CaseData caseData) {
        RespondentResponsePartAdmissionPaymentTimeLRspec paymentRoute =
                        caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
        RespondentResponseTypeSpec respondent1Response = caseData.getRespondent1ClaimResponseTypeForSpec();
        RespondentResponseTypeSpec respondent2Response = caseData.getRespondent2ClaimResponseTypeForSpec();
        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);

        boolean isFullAdmitImmediatePayment = IMMEDIATELY.equals(paymentRoute)
                && (FULL_ADMISSION.equals(respondent1Response)
                || FULL_ADMISSION.equals(respondent2Response));

        if (isFullAdmitImmediatePayment) {
            return notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpec();
        }

        if (ONE_V_TWO_TWO_LEGAL_REP.equals(scenario)) {
            return notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec();
        }

        return notificationsProperties.getClaimantSolicitorDefendantResponseForSpec();
    }

    public String getLipTemplate(CaseData caseData) {
        if (caseData.isClaimantBilingual()) {
            return notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate();
        } else {
            return notificationsProperties.getClaimantLipClaimUpdatedTemplate();
        }
    }

    public String getRespondentTemplate(CaseData caseData) {
        if (ONE_V_TWO_TWO_LEGAL_REP.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            return notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
        }

        RespondentResponsePartAdmissionPaymentTimeLRspec paymentRoute =
            caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
        RespondentResponseTypeSpec responseType =
            caseData.getRespondent1ClaimResponseTypeForSpec();
        Boolean isApplicantRepresented = caseData.isApplicantRepresented();

        boolean isValidPaymentRoute = IMMEDIATELY.equals(paymentRoute)
                                        || BY_SET_DATE.equals(paymentRoute)
                                        || SUGGESTION_OF_REPAYMENT_PLAN.equals(paymentRoute);

        boolean isPartAdmitWithApplicantRepresented =
            RespondentResponseTypeSpec.PART_ADMISSION.equals(responseType)
                && Boolean.TRUE.equals(isApplicantRepresented);

        if (isValidPaymentRoute && isPartAdmitWithApplicantRepresented) {
            return notificationsProperties.getRespondentSolicitorDefResponseSpecWithClaimantAction();
        }

        return notificationsProperties.getRespondentSolicitorDefendantResponseForSpec();
    }
}
