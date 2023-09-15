package uk.gov.hmcts.reform.civil.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.APPLICANT_ONE;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.APPLICANT_ONE_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.APPLICANT_TWO;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.APPLICANT_TWO_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_ONE;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_ONE_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_TWO;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.RESPONDENT_TWO_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.addApplicantExpertAndWitnessFlagsStructure;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.addRespondentDQPartiesFlagStructure;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.createOrUpdateFlags;

@Component
@AllArgsConstructor
public class CaseFlagsInitialiser {

    private final FeatureToggleService featureToggleService;
    private final OrganisationService organisationService;

    public void initialiseCaseFlags(CaseEvent caseEvent, CaseData.CaseDataBuilder dataBuilder) {
        if (!featureToggleService.isCaseFlagsEnabled()) {
            return;
        }

        CaseData caseData = dataBuilder.build();
        switch (caseEvent) {
            case CREATE_CLAIM:
            case CREATE_CLAIM_SPEC: {
                dataBuilder
                    .applicant1(CaseFlagUtils.updateParty(APPLICANT_ONE, caseData.getApplicant1()))
                    .applicant2(CaseFlagUtils.updateParty(APPLICANT_TWO, caseData.getApplicant2()))
                    .respondent1(CaseFlagUtils.updateParty(RESPONDENT_ONE, caseData.getRespondent1()))
                    .respondent2(CaseFlagUtils.updateParty(RESPONDENT_TWO, caseData.getRespondent2()))
                    .applicant1LitigationFriend(CaseFlagUtils.updateLitFriend(
                        APPLICANT_ONE_LITIGATION_FRIEND, caseData.getApplicant1LitigationFriend()))
                    .applicant2LitigationFriend(CaseFlagUtils.updateLitFriend(
                        APPLICANT_TWO_LITIGATION_FRIEND, caseData.getApplicant2LitigationFriend()));
                break;
            }
            case ADD_DEFENDANT_LITIGATION_FRIEND: {
                dataBuilder
                    .respondent1LitigationFriend(
                        CaseFlagUtils.updateLitFriend(
                            RESPONDENT_ONE_LITIGATION_FRIEND,
                            caseData.getRespondent1LitigationFriend()
                        ))
                    .respondent2LitigationFriend(
                        CaseFlagUtils.updateLitFriend(
                            RESPONDENT_TWO_LITIGATION_FRIEND,
                            caseData.getRespondent2LitigationFriend()
                        ));
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
            case MANAGE_CONTACT_INFORMATION: {
                createOrUpdateFlags(dataBuilder, caseData, organisationService);
                break;
            }
            default:
        }
    }
}
