package uk.gov.hmcts.reform.civil.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;

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
                dataBuilder
                    .applicant1(CaseFlagUtils.updateParty("Applicant 1", caseData.getApplicant1()))
                    .applicant2(CaseFlagUtils.updateParty("Applicant 2", caseData.getApplicant2()))
                    .respondent1(CaseFlagUtils.updateParty("Respondent 1", caseData.getRespondent1()))
                    .respondent2(CaseFlagUtils.updateParty("Respondent 2", caseData.getRespondent2()))
                    .applicant1LitigationFriend(CaseFlagUtils.updateLitFriend(
                        "Applicant 1 Litigation Friend", caseData.getApplicant1LitigationFriend()))
                    .applicant2LitigationFriend(CaseFlagUtils.updateLitFriend(
                        "Applicant 2 Litigation Friend", caseData.getApplicant2LitigationFriend()));
                break;
            }
            case ADD_DEFENDANT_LITIGATION_FRIEND: {
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
                break;
            }
            default:
        }
    }
}
