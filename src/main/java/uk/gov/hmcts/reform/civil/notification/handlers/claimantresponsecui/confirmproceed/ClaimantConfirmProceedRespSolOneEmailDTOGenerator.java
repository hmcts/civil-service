package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantConfirmProceedRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claimant-confirms-to-proceed-respondent-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final FeatureToggleService featureToggleService;

    public ClaimantConfirmProceedRespSolOneEmailDTOGenerator(OrganisationService organisationService,
                                                             NotificationsProperties notificationsProperties,
                                                             FeatureToggleService featureToggleService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (isClaimProceedForLipVsLR(caseData)) {
            return caseData.isSmallClaim()
                ? notificationsProperties.getRespondentSolicitorNotifyToProceedInMediation()
                : notificationsProperties.getRespondentSolicitorNotifyToProceedSpecWithAction();
        }
        if (isClaimantNotProceedLipVsLRWithNoc(caseData)) {
            return notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec();
        }

        return notificationsProperties.getNotifyDefendantLRForMediation();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        return properties;
    }

    private boolean isClaimProceedForLipVsLR(CaseData caseData) {
        return caseData.isLipvLROneVOne()
            && ((isFullDefenceStatesPaid(caseData) && NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim()))
            || YES.equals(caseData.getApplicant1ProceedWithClaim()))
            && featureToggleService.isDefendantNoCOnlineForCase(caseData);
    }

    private boolean isClaimantNotProceedLipVsLRWithNoc(CaseData caseData) {
        return caseData.isLipvLROneVOne()
            && ((isFullDefenceStatesPaid(caseData) && YES.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim()))
            || YesOrNo.NO.equals(caseData.getApplicant1ProceedWithClaim()))
            && featureToggleService.isDefendantNoCOnlineForCase(caseData);
    }

    private boolean isFullDefenceStatesPaid(CaseData caseData) {
        return HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired())
            && FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeForSpec());
    }
}
