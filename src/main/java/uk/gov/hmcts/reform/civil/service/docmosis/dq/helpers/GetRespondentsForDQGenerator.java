package uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@RequiredArgsConstructor
public class GetRespondentsForDQGenerator {

    private static final String organisationName = "Organisation name";
    private final RepresentativeService representativeService;

    public List<Party> getRespondents(CaseData caseData, String defendantIdentifier) {

        var legalRepHeading = caseData.getCaseAccessCategory().equals(SPEC_CLAIM) ? "Name" : organisationName;

        if (isClaimantResponse(caseData)) {

            List<Party> respondents = new ArrayList<>();
            if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && !ONE_V_ONE.equals(getMultiPartyScenario(caseData))) {
                if (isOneVTwoWithOneorBothLegalRep(caseData)) {
                    addPartyForRespondent1and2(caseData, respondents, legalRepHeading);
                } else if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
                    addPartForRespondent1(caseData, respondents, legalRepHeading);
                }
                return respondents;
            }

            proceedAgainstRespondent1(caseData, legalRepHeading, respondents);
            proceedAgainstRespondent2(caseData, legalRepHeading, respondents);

            return respondents;
        }

        if (respondent2HasSameLegalRep(caseData)) {
            return handleRespondentWithSameLegalRep(caseData, defendantIdentifier, legalRepHeading);
        }

        var respondent = isRespondent2(caseData) ? caseData.getRespondent2() : caseData.getRespondent1();
        var respondentRepresentative = isRespondent2(caseData)
            ? representativeService.getRespondent2Representative(caseData)
            : representativeService.getRespondent1Representative(caseData);
        var litigationFriend = isRespondent2(caseData)
            ? caseData.getRespondent2LitigationFriend()
            : caseData.getRespondent1LitigationFriend();

        return buildParty(respondent, respondentRepresentative, litigationFriend, legalRepHeading);
    }

    private List<Party> handleRespondentWithSameLegalRep(CaseData caseData, String defendantIdentifier, String legalRepHeading) {
        if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
            return getPartiesWhenSameLegalRepAndResponse(caseData, legalRepHeading);
        } else if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
            if ("ONE".equals(defendantIdentifier)) {
                return getRespondent1PartyWhenResponseNotSame(caseData, legalRepHeading);
            }
            if ("TWO".equals(defendantIdentifier)) {
                return getRespondent2PartyWhenResponseNotSame(caseData, legalRepHeading);
            }
        }
        return Collections.emptyList();
    }

    private static boolean isOneVTwoWithOneorBothLegalRep(CaseData caseData) {
        return (ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && YES.equals(caseData.getRespondentResponseIsSame()))
            || (ONE_V_TWO_TWO_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && RespondentResponseTypeSpec.FULL_DEFENCE
            .equals(caseData.getRespondent1ClaimResponseTypeForSpec())
            && RespondentResponseTypeSpec.FULL_DEFENCE
            .equals(caseData.getRespondent2ClaimResponseTypeForSpec()));
    }

    private void addPartForRespondent1(CaseData caseData, List<Party> respondents, String legalRepHeading) {
        respondents.add(new Party()
                            .setName(caseData.getRespondent1().getPartyName())
                            .setEmailAddress(caseData.getRespondent1().getPartyEmail())
                            .setPhoneNumber(caseData.getRespondent1().getPartyPhone())
                            .setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress())
                            .setRepresentative(representativeService
                                                .getRespondent1Representative(caseData))
                            .setLitigationFriendName(
                                ofNullable(caseData.getRespondent1LitigationFriend())
                                    .map(LitigationFriend::getFullName)
                                    .orElse(""))
                            .setLegalRepHeading(legalRepHeading));
    }

    private void addPartyForRespondent1and2(CaseData caseData, List<Party> respondents, String legalRepHeading) {
        respondents.add(new Party()
                            .setName(caseData.getRespondent1().getPartyName())
                            .setEmailAddress(caseData.getRespondent1().getPartyEmail())
                            .setPhoneNumber(caseData.getRespondent1().getPartyPhone())
                            .setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress())
                            .setRepresentative(representativeService
                                                .getRespondent1Representative(caseData))
                            .setLitigationFriendName(
                                ofNullable(caseData.getRespondent1LitigationFriend())
                                    .map(LitigationFriend::getFullName)
                                    .orElse(""))
                            .setLegalRepHeading(legalRepHeading));
        respondents.add(new Party()
                            .setName(caseData.getRespondent2().getPartyName())
                            .setEmailAddress(caseData.getRespondent2().getPartyEmail())
                            .setPhoneNumber(caseData.getRespondent2().getPartyPhone())
                            .setPrimaryAddress(caseData.getRespondent2().getPrimaryAddress())
                            .setRepresentative(representativeService
                                                .getRespondent2Representative(caseData))
                            .setLitigationFriendName(
                                ofNullable(caseData.getRespondent2LitigationFriend())
                                    .map(LitigationFriend::getFullName)
                                    .orElse(""))
                            .setLegalRepHeading(legalRepHeading));
    }

    private static List<Party> buildParty(uk.gov.hmcts.reform.civil.model.Party respondent,
                                          Representative respondentRepresentative,
                                          LitigationFriend litigationFriend,
                                          String legalRepHeading
    ) {
        var respondentParty = new Party()
            .setName(respondent.getPartyName())
            .setPrimaryAddress(respondent.getPrimaryAddress())
            .setEmailAddress(respondent.getPartyEmail())
            .setPhoneNumber(respondent.getPartyPhone())
            .setRepresentative(respondentRepresentative)
            .setLitigationFriendName(
                ofNullable(litigationFriend)
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .setLitigationFriendFirstName(
                ofNullable(litigationFriend)
                    .map(LitigationFriend::getFirstName)
                    .orElse(""))
            .setLitigationFriendLastName(
                ofNullable(litigationFriend)
                    .map(LitigationFriend::getLastName)
                    .orElse(""))
            .setLitigationFriendPhoneNumber(ofNullable(litigationFriend)
                                             .map(LitigationFriend::getPhoneNumber)
                                             .orElse(""))
            .setLitigationFriendEmailAddress(ofNullable(litigationFriend)
                                              .map(LitigationFriend::getEmailAddress)
                                              .orElse(""))
            .setLegalRepHeading(legalRepHeading);
        return List.of(respondentParty);
    }

    private List<Party> getRespondent2PartyWhenResponseNotSame(CaseData caseData, String legalRepHeading) {
        var respondent2Party = new Party()
            .setName(caseData.getRespondent2().getPartyName())
            .setPrimaryAddress(caseData.getRespondent2().getPrimaryAddress())
            .setEmailAddress(caseData.getRespondent2().getPartyEmail())
            .setPhoneNumber(caseData.getRespondent2().getPartyPhone())
            .setRepresentative(representativeService.getRespondent1Representative(caseData))
            .setLitigationFriendName(
                ofNullable(caseData.getRespondent2LitigationFriend())
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .setLitigationFriendFirstName(
                ofNullable(caseData.getRespondent2LitigationFriend())
                    .map(LitigationFriend::getFirstName)
                    .orElse(""))
            .setLitigationFriendLastName(
                ofNullable(caseData.getRespondent2LitigationFriend())
                    .map(LitigationFriend::getLastName)
                    .orElse(""))
            .setLitigationFriendPhoneNumber(ofNullable(caseData.getRespondent2LitigationFriend())
                                             .map(LitigationFriend::getPhoneNumber)
                                             .orElse(""))
            .setLitigationFriendEmailAddress(ofNullable(caseData.getRespondent2LitigationFriend())
                                              .map(LitigationFriend::getEmailAddress)
                                              .orElse(""))
            .setLegalRepHeading(legalRepHeading);
        return List.of(respondent2Party);
    }

    private List<Party> getRespondent1PartyWhenResponseNotSame(CaseData caseData, String legalRepHeading) {
        var respondent1Party = getRespondent1Party(caseData, legalRepHeading);
        return List.of(respondent1Party);
    }

    private List<Party> getPartiesWhenSameLegalRepAndResponse(CaseData caseData, String legalRepHeading) {
        var respondent1Party = getRespondent1Party(caseData, legalRepHeading);

        var respondent2Party = new Party()
            .setName(caseData.getRespondent2().getPartyName())
            .setPrimaryAddress(caseData.getRespondent2().getPrimaryAddress())
            .setEmailAddress(caseData.getRespondent2().getPartyEmail())
            .setPhoneNumber(caseData.getRespondent2().getPartyPhone())
            .setRepresentative(representativeService.getRespondent2Representative(caseData))
            .setLitigationFriendName(
                ofNullable(caseData.getRespondent2LitigationFriend())
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .setLitigationFriendFirstName(
                ofNullable(caseData.getRespondent2LitigationFriend())
                    .map(LitigationFriend::getFirstName)
                    .orElse(""))
            .setLitigationFriendLastName(
                ofNullable(caseData.getRespondent2LitigationFriend())
                    .map(LitigationFriend::getLastName)
                    .orElse(""))
            .setLitigationFriendPhoneNumber(ofNullable(caseData.getRespondent2LitigationFriend())
                                             .map(LitigationFriend::getPhoneNumber)
                                             .orElse(""))
            .setLitigationFriendEmailAddress(ofNullable(caseData.getRespondent2LitigationFriend())
                                              .map(LitigationFriend::getEmailAddress)
                                              .orElse(""))
            .setLegalRepHeading(legalRepHeading);

        return List.of(respondent1Party, respondent2Party);
    }

    private void proceedAgainstRespondent2(CaseData caseData, String legalRepHeading, List<Party> respondents) {
        if (isProceedingAgainstRespondent2(caseData)) {
            var respondent = caseData.getRespondent2();
            var litigationFriend = caseData.getRespondent2LitigationFriend();
            var respondent2Party = new Party()
                .setName(respondent.getPartyName())
                .setPrimaryAddress(caseData.getRespondent2().getPrimaryAddress())
                .setEmailAddress(respondent.getPartyEmail())
                .setPhoneNumber(respondent.getPartyPhone())
                .setRepresentative(representativeService
                                    .getRespondent2Representative(caseData))
                .setLitigationFriendName(
                    ofNullable(litigationFriend)
                        .map(LitigationFriend::getFullName)
                        .orElse(""))
                .setLitigationFriendFirstName(
                    ofNullable(litigationFriend)
                        .map(LitigationFriend::getFirstName)
                        .orElse(""))
                .setLitigationFriendLastName(
                    ofNullable(litigationFriend)
                        .map(LitigationFriend::getLastName)
                        .orElse(""))
                .setLitigationFriendPhoneNumber(ofNullable(litigationFriend)
                                                 .map(LitigationFriend::getPhoneNumber)
                                                 .orElse(""))
                .setLitigationFriendEmailAddress(ofNullable(litigationFriend)
                                                  .map(LitigationFriend::getEmailAddress)
                                                  .orElse(""))
                .setLegalRepHeading(legalRepHeading);
            respondents.add(respondent2Party);
        }
    }

    private void proceedAgainstRespondent1(CaseData caseData, String legalRepHeading, List<Party> respondents) {
        if (isProceedingAgainstRespondent1(caseData)) {
            var respondent = caseData.getRespondent1();
            var litigationFriend = caseData.getRespondent1LitigationFriend();
            var respondent1Party = new Party()
                .setName(respondent.getPartyName())
                .setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress())
                .setEmailAddress(respondent.getPartyEmail())
                .setPhoneNumber(respondent.getPartyPhone())
                .setRepresentative(representativeService
                                    .getRespondent1Representative(caseData))
                .setLitigationFriendName(
                    ofNullable(litigationFriend)
                        .map(LitigationFriend::getFullName)
                        .orElse(""))
                .setLitigationFriendFirstName(
                    ofNullable(litigationFriend)
                        .map(LitigationFriend::getFirstName)
                        .orElse(""))
                .setLitigationFriendLastName(
                    ofNullable(litigationFriend)
                        .map(LitigationFriend::getLastName)
                        .orElse(""))
                .setLitigationFriendPhoneNumber(ofNullable(litigationFriend)
                                                 .map(LitigationFriend::getPhoneNumber)
                                                 .orElse(""))
                .setLitigationFriendEmailAddress(ofNullable(litigationFriend)
                                                  .map(LitigationFriend::getEmailAddress)
                                                  .orElse(""))
                .setLegalRepHeading(legalRepHeading);
            respondents.add(respondent1Party);
        }
    }

    public static boolean isClaimantResponse(CaseData caseData) {
        var businessProcess = ofNullable(caseData.getBusinessProcess())
            .map(BusinessProcess::getCamundaEvent)
            .orElse(null);
        return "CLAIMANT_RESPONSE".equals(businessProcess)
            || "CLAIMANT_RESPONSE_SPEC".equals(businessProcess)
            || "CLAIMANT_RESPONSE_CUI".equals(businessProcess);
    }

    private boolean isProceedingAgainstRespondent1(CaseData caseData) {
        return YES.equals(caseData.getApplicant1ProceedWithClaim())
            || YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
            || YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
            || YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent1MultiParty1v2());
    }

    private boolean isRespondent2(CaseData caseData) {
        if (caseData.getRespondent2ResponseDate() != null) {
            return caseData.getRespondent1ResponseDate() == null
                || caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate());
        }

        return false;
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private boolean isProceedingAgainstRespondent2(CaseData caseData) {
        return YES.equals(caseData.getApplicant1ProceedWithClaimAgainstRespondent2MultiParty1v2());
    }

    private Party getRespondent1Party(CaseData caseData, String legalRepHeading) {
        return new Party()
            .setName(caseData.getRespondent1().getPartyName())
            .setPrimaryAddress(caseData.getRespondent1().getPrimaryAddress())
            .setEmailAddress(caseData.getRespondent1().getPartyEmail())
            .setPhoneNumber(caseData.getRespondent1().getPartyPhone())
            .setRepresentative(representativeService.getRespondent1Representative(caseData))
            .setLitigationFriendName(
                ofNullable(caseData.getRespondent1LitigationFriend())
                    .map(LitigationFriend::getFullName)
                    .orElse(""))
            .setLitigationFriendFirstName(
                ofNullable(caseData.getRespondent1LitigationFriend())
                    .map(LitigationFriend::getFirstName)
                    .orElse(""))
            .setLitigationFriendLastName(
                ofNullable(caseData.getRespondent1LitigationFriend())
                    .map(LitigationFriend::getLastName)
                    .orElse(""))
            .setLitigationFriendPhoneNumber(ofNullable(caseData.getRespondent1LitigationFriend())
                                             .map(LitigationFriend::getPhoneNumber)
                                             .orElse(""))
            .setLitigationFriendEmailAddress(ofNullable(caseData.getRespondent1LitigationFriend())
                                              .map(LitigationFriend::getEmailAddress)
                                              .orElse(""))
            .setLegalRepHeading(legalRepHeading);
    }
}
