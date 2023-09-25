package uk.gov.hmcts.reform.civil.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.addApplicantExpertAndWitnessFlagsStructure;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.addRespondentDQPartiesFlagStructure;

@Component
@AllArgsConstructor
public class CaseFlagsInitialiser {

    private final FeatureToggleService featureToggleService;

    public void initialiseCaseFlags(CaseEvent caseEvent, CaseData.CaseDataBuilder dataBuilder) {
        if (!featureToggleService.isCaseFlagsEnabled()) {
            return;
        }

        CaseData caseData = dataBuilder.build();
        switch (caseEvent) {
            case CREATE_CLAIM:
            case CREATE_CLAIM_SPEC: {
                initialiseApplicantAndRespondentFlags(dataBuilder, caseData);
                break;
            }
            case ADD_DEFENDANT_LITIGATION_FRIEND: {
                initialiseRespondentLitigationFriendFlags(dataBuilder, caseData);
                break;
            }
            case DEFENDANT_RESPONSE_SPEC:
            case DEFENDANT_RESPONSE: {
                addRespondentDQPartiesFlagStructure(dataBuilder, caseData);
                break;
            }
            case CLAIMANT_RESPONSE:
            case CLAIMANT_RESPONSE_SPEC: {
                addApplicantExpertAndWitnessFlagsStructure(dataBuilder, caseData);
                break;
            }
            default:
        }
    }

    public void initialiseMissingCaseFlags(CaseData.CaseDataBuilder dataBuilder) {
        CaseData caseData = dataBuilder.build();
        initialiseApplicantAndRespondentFlags(dataBuilder, caseData);
        initialiseRespondentLitigationFriendFlags(dataBuilder, caseData);
        if (shouldReinitialiseRespondentDQFlags(caseData)) {
            addRespondentDQPartiesFlagStructure(dataBuilder, caseData);
        }
        if (shouldReinitialiseApplicantDQFlags(caseData)) {
            addApplicantExpertAndWitnessFlagsStructure(dataBuilder, caseData);
        }
    }

    private void initialiseRespondentLitigationFriendFlags(CaseData.CaseDataBuilder dataBuilder, CaseData caseData) {
        dataBuilder
            .respondent1LitigationFriend(
                CaseFlagUtils.updateLitFriend(
                    "Respondent 1 Litigation Friend",
                    caseData.getRespondent1LitigationFriend()
                ))
            .respondent2LitigationFriend(
                CaseFlagUtils.updateLitFriend(
                    "Respondent 2 Litigation Friend",
                    caseData.getRespondent2LitigationFriend()
                ));
    }

    private void initialiseApplicantAndRespondentFlags(CaseData.CaseDataBuilder dataBuilder, CaseData caseData) {
        dataBuilder
            .applicant1(CaseFlagUtils.updateParty("Applicant 1", caseData.getApplicant1()))
            .applicant2(CaseFlagUtils.updateParty("Applicant 2", caseData.getApplicant2()))
            .respondent1(CaseFlagUtils.updateParty("Respondent 1", caseData.getRespondent1()))
            .respondent2(CaseFlagUtils.updateParty("Respondent 2", caseData.getRespondent2()))
            .applicant1LitigationFriend(CaseFlagUtils.updateLitFriend(
                "Applicant 1 Litigation Friend", caseData.getApplicant1LitigationFriend()))
            .applicant2LitigationFriend(CaseFlagUtils.updateLitFriend(
                "Applicant 2 Litigation Friend", caseData.getApplicant2LitigationFriend()));
    }

    private boolean shouldReinitialiseRespondentDQFlags(CaseData caseData) {
        return caseData.getRespondent1Witnesses() == null || caseData.getRespondent1Experts() == null
            || (YES.equals(caseData.getAddRespondent2())
            && (caseData.getRespondent2Experts() == null || caseData.getRespondent2Witnesses() == null));
    }

    private boolean shouldReinitialiseApplicantDQFlags(CaseData caseData) {
        return caseData.getApplicantExperts() == null || caseData.getApplicantWitnesses() == null;
    }
}
