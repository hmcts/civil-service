package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Evidence;
import uk.gov.hmcts.reform.civil.model.EvidenceDetails;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.TimelineOfEvents;
import uk.gov.hmcts.reform.civil.model.docmosis.ClaimResponseForm;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim.Representative;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class ClaimResponseFormGenerator implements TemplateDataGenerator<ClaimResponseForm> {

    private final RepresentativeService representativeService;

    @Override
    public ClaimResponseForm getTemplateData(CaseData caseData) throws IOException {
        ClaimResponseForm.ClaimResponseFormBuilder builder = ClaimResponseForm.builder();

        boolean isRespondent2 = isRespondent2(caseData);

        Party formRespondent = getParty(caseData, isRespondent2);

        setResponseInfo(builder, caseData, isRespondent2);

        builder.referenceNumber(caseData.getLegacyCaseReference())
            .issueDate(caseData.getIssueDate())
            .submittedOn(isRespondent2
                             ? caseData.getRespondent2ResponseDate()
                             : caseData.getRespondent1ResponseDate())
            .respondent(formRespondent)
        ;

        return builder.build();
    }

    /**
     * TODO for MP we'll have to work through this.
     *
     * @param builder       building the form.
     * @param caseData      data of the claim.
     * @param isRespondent2 true if we have to generate the data for the respondent2, false for respondent 1.
     */
    private void setResponseInfo(ClaimResponseForm.ClaimResponseFormBuilder builder,
                                 CaseData caseData,
                                 boolean isRespondent2) {
        builder.defendantResponse(Optional.ofNullable(caseData.getRespondent1ClaimResponseTypeForSpec())
                                      .map(RespondentResponseTypeSpec::getDisplayedValue)
                                      .orElse(""))
            .whyDisputeTheClaim(caseData.getDetailsOfWhyDoesYouDisputeTheClaim())
            .defendantTimeline(getTimeLine(caseData.getSpecResponseTimelineOfEvents()))
            .defendantEvidence(getEvidence(caseData.getSpecResponselistYourEvidenceList()))
            .willingToTryMediation(Optional.ofNullable(caseData.getResponseClaimMediationSpecRequired())
                                       .orElse(NO))
            .statementOfTruth(caseData.getUiStatementOfTruth());
    }

    private List<TimelineOfEventDetails> getTimeLine(List<TimelineOfEvents> timelineOfEvents) {
        if (timelineOfEvents != null) {
            return timelineOfEvents.stream()
                .map(event -> new TimelineOfEventDetails(
                    event.getValue().getTimelineDate(),
                    event.getValue().getTimelineDescription()
                ))
                .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private List<EvidenceDetails> getEvidence(List<Evidence> evidenceList) {
        if (evidenceList == null) {
            return Collections.emptyList();
        } else {
            return evidenceList.stream()
                .map(evidence -> new EvidenceDetails(
                    evidence.getValue().getEvidenceType(),
                    evidence.getValue().getPhotoEvidence(),
                    evidence.getValue().getContractAndAgreementsEvidence(),
                    evidence.getValue().getExpertWitnessEvidence(),
                    evidence.getValue().getLettersEmailsAndOtherCorrespondenceEvidence(),
                    evidence.getValue().getReceiptsEvidence(),
                    evidence.getValue().getStatementOfTruthEvidence(),
                    evidence.getValue().getOtherEvidence()
                )).collect(Collectors.toList());
        }
    }

    private Party getParty(CaseData caseData, boolean isRespondent2) {
        uk.gov.hmcts.reform.civil.model.Party caseDataRespondent;
        Representative representative;
        Optional<LitigationFriend> litigationFriend;
        if (isRespondent2) {
            caseDataRespondent = caseData.getRespondent2();
            litigationFriend = Optional.ofNullable(caseData.getRespondent2LitigationFriend());
            if (respondent2HasSameLegalRep(caseData)) {
                representative = representativeService
                    .getRespondent1Representative(caseData);
            } else {
                representative = representativeService
                    .getRespondent2Representative(caseData);
            }
        } else {
            caseDataRespondent = caseData.getRespondent1();
            litigationFriend = Optional.ofNullable(caseData.getRespondent1LitigationFriend());
            representative = representativeService
                .getRespondent1Representative(caseData);
        }

        return Party.builder()
            .name(caseDataRespondent.getPartyName())
            .primaryAddress(caseDataRespondent.getPrimaryAddress())
            .representative(representative)
            .litigationFriendName(litigationFriend
                                      .map(LitigationFriend::getFullName)
                                      .orElse(""))
            .build();
    }

    private boolean isRespondent2(CaseData caseData) {
        // TODO duplicated from DirectionsQuestionnaireGenerator, waiting to merge changes on that to replace this
        if (caseData.getRespondent2ResponseDate() != null && caseData.getRespondent1ResponseDate() == null) {
            return true;
        } else if ((caseData.getRespondent1ResponseDate() != null
            && caseData.getRespondent2ResponseDate() != null)) {
            if (caseData.getRespondent2ResponseDate().isAfter(caseData.getRespondent1ResponseDate())) {
                return true;
            }
        }
        return false;
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        // TODO also copied from DirectionsQuestionnaireGenerator, might move to RepresentativeService
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }
}
