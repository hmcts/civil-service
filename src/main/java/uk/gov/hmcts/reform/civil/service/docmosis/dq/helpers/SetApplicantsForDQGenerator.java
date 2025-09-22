package uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@RequiredArgsConstructor
public class SetApplicantsForDQGenerator {

    private final RepresentativeService representativeService;
    static final String organisationName = "Organisation name";

    public void setApplicants(DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder,
                               CaseData caseData) {
        if (TWO_V_ONE.equals(MultiPartyScenario
                                 .getMultiPartyScenario(caseData))) {
            if (onlyApplicant2IsProceeding(caseData)) {
                builder.applicant(getApplicant2DQParty(caseData));
            } else {
                builder.applicant(getApplicant1DQParty(caseData));
                builder.applicant2(getApplicant2DQParty(caseData));
            }
        } else {
            builder.applicant(getApplicant1DQParty(caseData));
        }
    }

    public Party getApplicant2DQParty(CaseData caseData) {
        var legalRepHeading = caseData.getCaseAccessCategory().equals(SPEC_CLAIM) ? "Name" : organisationName;
        var applicant = caseData.getApplicant2();
        var litigationFriend = caseData.getApplicant2LitigationFriend();
        var applicant2PartyBuilder = Party.builder()
            .name(applicant.getPartyName())
            .primaryAddress(applicant.getPrimaryAddress())
            .emailAddress(applicant.getPartyEmail())
            .phoneNumber(applicant.getPartyPhone())
            .representative(representativeService
                                .getApplicantRepresentative(caseData))
            // remove litigationFriendName when HNL toggle is enabled
            .litigationFriendName(
                ofNullable(litigationFriend)
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .litigationFriendFirstName(
                ofNullable(litigationFriend)
                    .map(LitigationFriend::getFirstName)
                    .orElse(""))
            .litigationFriendLastName(
                ofNullable(litigationFriend)
                    .map(LitigationFriend::getLastName)
                    .orElse(""))
            .litigationFriendPhoneNumber(ofNullable(litigationFriend)
                                             .map(LitigationFriend::getPhoneNumber)
                                             .orElse(""))
            .litigationFriendEmailAddress(ofNullable(litigationFriend)
                                              .map(LitigationFriend::getEmailAddress)
                                              .orElse(""))
            .legalRepHeading(legalRepHeading);
        return applicant2PartyBuilder.build();
    }

    public Party getApplicant1DQParty(CaseData caseData) {
        var legalRepHeading = caseData.getCaseAccessCategory().equals(SPEC_CLAIM) ? "Name" : organisationName;
        var applicant = caseData.getApplicant1();
        var litigationFriend = caseData.getApplicant1LitigationFriend();
        var applicant1PartyBuilder = Party.builder()
            .name(applicant.getPartyName())
            .primaryAddress(caseData.getApplicant1().getPrimaryAddress())
            .emailAddress(applicant.getPartyEmail())
            .phoneNumber(applicant.getPartyPhone())
            .representative(representativeService
                                .getApplicantRepresentative(caseData))
            // remove litigationFriendName when HNL toggle is enabled
            .litigationFriendName(
                ofNullable(litigationFriend)
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .litigationFriendFirstName(
                ofNullable(litigationFriend)
                    .map(LitigationFriend::getFirstName)
                    .orElse(""))
            .litigationFriendLastName(
                ofNullable(litigationFriend)
                    .map(LitigationFriend::getLastName)
                    .orElse(""))
            .litigationFriendPhoneNumber(ofNullable(litigationFriend)
                                             .map(LitigationFriend::getPhoneNumber)
                                             .orElse(""))
            .litigationFriendEmailAddress(ofNullable(litigationFriend)
                                              .map(LitigationFriend::getEmailAddress)
                                              .orElse(""))
            .legalRepHeading(legalRepHeading);
        return applicant1PartyBuilder.build();
    }

    private boolean onlyApplicant2IsProceeding(CaseData caseData) {
        return !YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
            && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1());
    }
}
