package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.finalorders.ClaimantAndDefendantHeard;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.model.finalorders.TrialNoticeProcedure;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList.CLAIMANT_NOT_ATTENDING;

@ExtendWith(SpringExtension.class)
public class ClaimantAttendsOrRepresentedTextBuilderTest {

    @InjectMocks
    private ClaimantAttendsOrRepresentedTextBuilder claimantAttendsOrRepresentedTextBuilder;

    @Test
    void testGetClaimantOneNotAttendedText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(
            FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(new FinalOrderRepresentation().setTypeRepresentationComplex(
                    new ClaimantAndDefendantHeard().setTrialProcedureClaimantComplex(new TrialNoticeProcedure().setList(
                        finalOrdersClaimantDefendantNotAttending))))
                .build();
            String name = caseData.getApplicant1().getPartyName();
            FinalOrdersClaimantRepresentationList finalOrdersClaimantRepresentationList = CLAIMANT_NOT_ATTENDING;
            String response = claimantAttendsOrRepresentedTextBuilder.buildClaimantRepresentationText(caseData, name, finalOrdersClaimantRepresentationList, false);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL -> Assertions.assertEquals(format(
                    "%s, the claimant, did not attend the trial. "
                        + "The Judge was not satisfied that they had received notice of the hearing "
                        + "and it was not reasonable to proceed in their absence.", name), response);
                case SATISFIED_NOTICE_OF_TRIAL -> Assertions.assertEquals(format(
                    "%s, the claimant, did not attend the trial and, whilst the Judge was satisfied that they had "
                        + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.", name), response);
                case SATISFIED_REASONABLE_TO_PROCEED -> Assertions.assertEquals(format(
                    "%s, the claimant, did not attend the trial. The Judge was satisfied that they had "
                        + "received notice of the trial and determined that it was reasonable to proceed in their absence.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testGetClaimantTwoNotAttendedText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(
            FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YesOrNo.YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .finalOrderRecitals(null)
                .finalOrderRepresentation(new FinalOrderRepresentation().setTypeRepresentationComplex(
                    new ClaimantAndDefendantHeard().setTrialProcedClaimTwoComplex(new TrialNoticeProcedure().setListClaimTwo(
                        finalOrdersClaimantDefendantNotAttending))))
                .build();
            String name = caseData.getApplicant2().getPartyName();
            FinalOrdersClaimantRepresentationList finalOrdersClaimantRepresentationList = CLAIMANT_NOT_ATTENDING;
            String response = claimantAttendsOrRepresentedTextBuilder.buildClaimantRepresentationText(caseData, name,
                                                                                                      finalOrdersClaimantRepresentationList, true);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL -> Assertions.assertEquals(format(
                    "%s, the claimant, did not attend the trial. "
                        + "The Judge was not satisfied that they had received notice of the hearing "
                        + "and it was not reasonable to proceed in their absence.", name), response);
                case SATISFIED_NOTICE_OF_TRIAL -> Assertions.assertEquals(format(
                    "%s, the claimant, did not attend the trial and, whilst the Judge was satisfied that they had "
                        + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.", name), response);
                case SATISFIED_REASONABLE_TO_PROCEED -> Assertions.assertEquals(format(
                    "%s, the claimant, did not attend the trial. The Judge was satisfied that they had "
                        + "received notice of the trial and determined that it was reasonable to proceed in their absence.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testClaimantOneAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersClaimantRepresentationList finalOrdersClaimantRepresentationList : List.of(
            FinalOrdersClaimantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(new FinalOrderRepresentation().setTypeRepresentationComplex(
                    new ClaimantAndDefendantHeard().setTypeRepresentationClaimantList(
                        finalOrdersClaimantRepresentationList)))
                .build();
            String name = caseData.getApplicant1().getPartyName();
            String response = claimantAttendsOrRepresentedTextBuilder.claimantBuilder(caseData, false);
            switch (finalOrdersClaimantRepresentationList) {
                case COUNSEL_FOR_CLAIMANT -> assertEquals(format("Counsel for %s, the claimant.", name), response);
                case SOLICITOR_FOR_CLAIMANT -> assertEquals(format("Solicitor for %s, the claimant.", name), response);
                case COST_DRAFTSMAN_FOR_THE_CLAIMANT -> assertEquals(format("Costs draftsman for %s, the claimant.", name), response);
                case THE_CLAIMANT_IN_PERSON -> assertEquals(format("%s, the claimant, in person.", name), response);
                case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT -> assertEquals(format("A lay representative for %s, the claimant.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testClaimantTwoAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersClaimantRepresentationList finalOrdersClaimantRepresentationList : List.of(
            FinalOrdersClaimantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YesOrNo.YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .finalOrderRecitals(null)
                .finalOrderRepresentation(new FinalOrderRepresentation().setTypeRepresentationComplex(
                    new ClaimantAndDefendantHeard().setTypeRepresentationClaimantListTwo(
                        finalOrdersClaimantRepresentationList)))
                .build();
            String name = caseData.getApplicant2().getPartyName();
            String response = claimantAttendsOrRepresentedTextBuilder.claimantBuilder(caseData, true);
            switch (finalOrdersClaimantRepresentationList) {
                case COUNSEL_FOR_CLAIMANT -> assertEquals(format("Counsel for %s, the claimant.", name), response);
                case SOLICITOR_FOR_CLAIMANT -> assertEquals(format("Solicitor for %s, the claimant.", name), response);
                case COST_DRAFTSMAN_FOR_THE_CLAIMANT -> assertEquals(format("Costs draftsman for %s, the claimant.", name), response);
                case THE_CLAIMANT_IN_PERSON -> assertEquals(format("%s, the claimant, in person.", name), response);
                case LAY_REPRESENTATIVE_FOR_THE_CLAIMANT -> assertEquals(format("A lay representative for %s, the claimant.", name), response);
                default -> {
                }
            }
        }
    }
}
