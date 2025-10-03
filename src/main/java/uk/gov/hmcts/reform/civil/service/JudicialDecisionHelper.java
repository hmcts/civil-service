package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudicialDecisionHelper {

    public YesOrNo isApplicationCreatedWithoutNoticeByApplicant(CaseData caseData) {
        return (caseData.getGeneralAppRespondentAgreement() != null
            && YES.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed()))
            ? YES : (caseData.getGeneralAppInformOtherParty() != null
            && NO.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice()))
            ? YES : NO;
    }

    public YesOrNo isLipApplicationCreatedWithoutNoticeByApplicant(CaseData caseData) {
        return (caseData.getGeneralAppInformOtherParty() != null
            && YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice())
            || Objects.nonNull(caseData.getGeneralAppConsentOrder())) ? NO : YES;
    }

    public boolean isApplicantAndRespondentLocationPrefSame(CaseData caseData) {
        if (caseData.getGeneralAppHearingDetails() == null
            || caseData.getGeneralAppHearingDetails().getHearingPreferredLocation() == null
            || caseData.getRespondentsResponses() == null
            || caseData.getRespondentsResponses().stream().filter(
                e -> e.getValue().getGaHearingDetails().getHearingPreferredLocation() == null).count() > 0) {
            return false;
        }
        String applicantLocation = caseData.getGeneralAppHearingDetails().getHearingPreferredLocation()
            .getValue().getLabel();
        long count = caseData.getRespondentsResponses().stream()
            .filter(e -> !applicantLocation.equals(
                e.getValue().getGaHearingDetails().getHearingPreferredLocation().getValue().getLabel())).count();
        return count == 0;
    }

    public boolean isOrderMakeDecisionMadeVisibleToDefendant(CaseData caseData) {
        return (isApplicationCreatedWithoutNoticeByApplicant(caseData).equals(YES)
            && Objects.nonNull(caseData.getApplicationIsCloaked())
            && caseData.getApplicationIsCloaked().equals(NO)
            && caseData.getJudicialDecision().getDecision().equals(GAJudgeDecisionOption.MAKE_AN_ORDER));

    }

    public boolean isApplicationUncloakedWithAdditionalFee(CaseData caseData) {

        var judicialDecisionRequestMoreInfo = caseData.getJudicialDecisionRequestMoreInfo();

        return isApplicationCreatedWithoutNoticeByApplicant(caseData).equals(YES)
            && Objects.nonNull(caseData.getApplicationIsCloaked())
            && caseData.getApplicationIsCloaked().equals(NO)
            && Objects.nonNull(judicialDecisionRequestMoreInfo)
            && Objects.nonNull(judicialDecisionRequestMoreInfo.getRequestMoreInfoOption())
            && judicialDecisionRequestMoreInfo.getRequestMoreInfoOption().equals(SEND_APP_TO_OTHER_PARTY);
    }
}
