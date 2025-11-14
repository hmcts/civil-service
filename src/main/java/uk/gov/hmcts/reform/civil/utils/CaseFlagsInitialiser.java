package uk.gov.hmcts.reform.civil.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
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

    private final OrganisationService organisationService;

    public void initialiseCaseFlags(CaseEvent caseEvent, CaseData caseData) {

        switch (caseEvent) {
            case CREATE_CLAIM, CREATE_CLAIM_SPEC, CREATE_LIP_CLAIM: {
                initialiseApplicantAndRespondentFlags(caseData);
                break;
            }
            case ADD_DEFENDANT_LITIGATION_FRIEND: {
                initialiseRespondentLitigationFriendFlags(caseData);
                break;
            }
            case DEFENDANT_RESPONSE_SPEC, DEFENDANT_RESPONSE, DEFENDANT_RESPONSE_CUI: {
                addRespondentDQPartiesFlagStructure(caseData);
                break;
            }
            case CLAIMANT_RESPONSE, CLAIMANT_RESPONSE_SPEC, CLAIMANT_RESPONSE_CUI: {
                addApplicantExpertAndWitnessFlagsStructure(caseData);
                break;
            }
            case MANAGE_CONTACT_INFORMATION: {
                createOrUpdateFlags(caseData, organisationService);
                break;
            }
            default:
        }
    }

    public void initialiseMissingCaseFlags(CaseData caseData) {
        initialiseApplicantAndRespondentFlags(caseData);
        initialiseRespondentLitigationFriendFlags(caseData);
        if (shouldReinitialiseRespondentDQFlags(caseData)) {
            addRespondentDQPartiesFlagStructure(caseData);
        }
        if (shouldReinitialiseApplicantDQFlags(caseData)) {
            addApplicantExpertAndWitnessFlagsStructure(caseData);
        }
    }

    private void initialiseRespondentLitigationFriendFlags(CaseData caseData) {
        caseData
            .setRespondent1LitigationFriend(
                CaseFlagUtils.updateLitFriend(
                    RESPONDENT_ONE_LITIGATION_FRIEND,
                    caseData.getRespondent1LitigationFriend()
                ));
        caseData.setRespondent2LitigationFriend(
            CaseFlagUtils.updateLitFriend(
                RESPONDENT_TWO_LITIGATION_FRIEND,
                caseData.getRespondent2LitigationFriend()
            ));
    }

    private void initialiseApplicantAndRespondentFlags(CaseData caseData) {
        caseData.setApplicant1(CaseFlagUtils.updateParty(APPLICANT_ONE, caseData.getApplicant1()));
        caseData.setApplicant2(CaseFlagUtils.updateParty(APPLICANT_TWO, caseData.getApplicant2()));
        caseData.setRespondent1(CaseFlagUtils.updateParty(RESPONDENT_ONE, caseData.getRespondent1()));
        caseData.setRespondent2(CaseFlagUtils.updateParty(RESPONDENT_TWO, caseData.getRespondent2()));
        caseData.setApplicant1LitigationFriend(CaseFlagUtils.updateLitFriend(
            APPLICANT_ONE_LITIGATION_FRIEND, caseData.getApplicant1LitigationFriend()));
        caseData.setApplicant2LitigationFriend(CaseFlagUtils.updateLitFriend(
            APPLICANT_TWO_LITIGATION_FRIEND, caseData.getApplicant2LitigationFriend()));
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
