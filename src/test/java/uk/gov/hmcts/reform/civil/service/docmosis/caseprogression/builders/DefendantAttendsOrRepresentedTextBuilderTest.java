package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.finalorders.ClaimantAndDefendantHeard;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.model.finalorders.TrialNoticeProcedure;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.List;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList.DEFENDANT_NOT_ATTENDING;

@ExtendWith(SpringExtension.class)
public class DefendantAttendsOrRepresentedTextBuilderTest {

    @InjectMocks
    private DefendantAttendsOrRepresentedTextBuilder defendantAttendsOrRepresentedTextBuilder;

    @Test
    void testDefendantOneNotAttendingText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(
            FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedureComplex(TrialNoticeProcedure.builder().listDef(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String name = caseData.getRespondent1().getPartyName();
            FinalOrdersDefendantRepresentationList finalOrdersDefendantRepresentationList = DEFENDANT_NOT_ATTENDING;
            String response = defendantAttendsOrRepresentedTextBuilder.buildRespondentRepresentationText(caseData, name, finalOrdersDefendantRepresentationList, false);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the defendant, did not attend the trial. "
                        + "The Judge was not satisfied that they had received notice of the hearing "
                        + "and it was not reasonable to proceed in their absence.", name), response);
                case SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the defendant, did not attend the trial and, whilst the Judge was satisfied that they had "
                        + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.", name), response);
                case SATISFIED_REASONABLE_TO_PROCEED -> assertEquals(
                    format(
                        "%s, the defendant, did not attend the trial. The Judge was satisfied that they had "
                            + "received notice of the trial and determined that it was reasonable to proceed in their absence.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testDefendantTwoNotAttendingText() {
        for (FinalOrdersClaimantDefendantNotAttending finalOrdersClaimantDefendantNotAttending : List.of(
            FinalOrdersClaimantDefendantNotAttending.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().trialProcedureDefTwoComplex(TrialNoticeProcedure.builder().listDefTwo(
                        finalOrdersClaimantDefendantNotAttending).build()).build()).build())
                .build();
            String name = caseData.getRespondent2().getPartyName();
            FinalOrdersDefendantRepresentationList finalOrdersDefendantRepresentationList = DEFENDANT_NOT_ATTENDING;
            String response = defendantAttendsOrRepresentedTextBuilder.buildRespondentRepresentationText(caseData, name, finalOrdersDefendantRepresentationList, true);
            switch (finalOrdersClaimantDefendantNotAttending) {
                case NOT_SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the defendant, did not attend the trial. "
                        + "The Judge was not satisfied that they had received notice of the hearing "
                        + "and it was not reasonable to proceed in their absence.", name), response);
                case SATISFIED_NOTICE_OF_TRIAL -> assertEquals(format(
                    "%s, the defendant, did not attend the trial and, whilst the Judge was satisfied that they had "
                        + "received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.", name), response);
                case SATISFIED_REASONABLE_TO_PROCEED -> assertEquals(
                    format(
                        "%s, the defendant, did not attend the trial. The Judge was satisfied that they had "
                            + "received notice of the trial and determined that it was reasonable to proceed in their absence.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testDefendantOneAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersDefendantRepresentationList finalOrdersDefendantRepresentationList : List.of(
            FinalOrdersDefendantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationDefendantList(
                        finalOrdersDefendantRepresentationList).build()).build())
                .build();
            String name = caseData.getRespondent1().getPartyName();
            String response = defendantAttendsOrRepresentedTextBuilder.defendantBuilder(caseData, false);
            switch (finalOrdersDefendantRepresentationList) {
                case COUNSEL_FOR_DEFENDANT -> assertEquals(format("Counsel for %s, the defendant.", name), response);
                case SOLICITOR_FOR_DEFENDANT -> assertEquals(format("Solicitor for %s, the defendant.", name), response);
                case COST_DRAFTSMAN_FOR_THE_DEFENDANT -> assertEquals(format("Costs draftsman for %s, the defendant.", name), response);
                case THE_DEFENDANT_IN_PERSON -> assertEquals(format("%s, the defendant, in person.", name), response);
                case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT -> assertEquals(format("A lay representative for %s, the defendant.", name), response);
                default -> {
                }
            }
        }
    }

    @Test
    void testDefendantTwoAttendsOrRepresentedTextBuilder() {
        for (FinalOrdersDefendantRepresentationList finalOrdersDefendantRepresentationList : List.of(
            FinalOrdersDefendantRepresentationList.values())) {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .respondent2(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .finalOrderRecitals(null)
                .finalOrderRepresentation(FinalOrderRepresentation.builder().typeRepresentationComplex(
                    ClaimantAndDefendantHeard.builder().typeRepresentationDefendantTwoList(
                        finalOrdersDefendantRepresentationList).build()).build())
                .build();
            String name = caseData.getRespondent2().getPartyName();
            String response = defendantAttendsOrRepresentedTextBuilder.defendantBuilder(caseData, true);
            switch (finalOrdersDefendantRepresentationList) {
                case COUNSEL_FOR_DEFENDANT -> assertEquals(format("Counsel for %s, the defendant.", name), response);
                case SOLICITOR_FOR_DEFENDANT -> assertEquals(format("Solicitor for %s, the defendant.", name), response);
                case COST_DRAFTSMAN_FOR_THE_DEFENDANT -> assertEquals(format("Costs draftsman for %s, the defendant.", name), response);
                case THE_DEFENDANT_IN_PERSON -> assertEquals(format("%s, the defendant, in person.", name), response);
                case LAY_REPRESENTATIVE_FOR_THE_DEFENDANT -> assertEquals(format("A lay representative for %s, the defendant.", name), response);
                default -> {
                }
            }
        }
    }

}
