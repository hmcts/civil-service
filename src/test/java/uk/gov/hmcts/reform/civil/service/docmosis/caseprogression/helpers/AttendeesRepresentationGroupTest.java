package uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderRepresentationList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.casepogression.JudgeFinalOrderForm;
import uk.gov.hmcts.reform.civil.model.finalorders.ClaimantAndDefendantHeard;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders.ClaimantAttendsOrRepresentedTextBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.caseprogression.builders.DefendantAttendsOrRepresentedTextBuilder;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class AttendeesRepresentationGroupTest {

    @InjectMocks
    private AttendeesRepresentationPopulator attendeesRepresentationPopulator;

    @Mock
    private ClaimantAttendsOrRepresentedTextBuilder claimantAttendsOrRepresentedTextBuilder;

    @Mock
    private DefendantAttendsOrRepresentedTextBuilder defendantAttendsOrRepresentedTextBuilder;

    @Test
    void shouldPopulateAttendeesDetails_WhenAllAttendeesPresent() {
        Party applicant2 = PartyBuilder.builder().company().build();
        Party respondent2 = PartyBuilder.builder().company().build();
        CaseData caseData = CaseDataBuilder.builder().applicant2(applicant2).respondent2(respondent2).build();

        when(claimantAttendsOrRepresentedTextBuilder.claimantBuilder(caseData, false)).thenReturn("Claimant 1 attends");
        when(claimantAttendsOrRepresentedTextBuilder.claimantBuilder(caseData, true)).thenReturn("Claimant 2 attends");
        when(defendantAttendsOrRepresentedTextBuilder.defendantBuilder(caseData, false)).thenReturn("Defendant 1 attends");
        when(defendantAttendsOrRepresentedTextBuilder.defendantBuilder(caseData, true)).thenReturn("Defendant 2 attends");

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();
        builder = attendeesRepresentationPopulator.populateAttendeesDetails(builder, caseData);

        JudgeFinalOrderForm form = builder.build();
        Assertions.assertEquals("Claimant 1 attends", form.getClaimantAttendsOrRepresented());
        Assertions.assertEquals("Claimant 2 attends", form.getClaimantTwoAttendsOrRepresented());
        Assertions.assertEquals("Defendant 1 attends", form.getDefendantAttendsOrRepresented());
        Assertions.assertEquals("Defendant 2 attends", form.getDefendantTwoAttendsOrRepresented());

    }

    @Test
    void shouldPopulateAttendeesDetails_WhenOnlyOneClaimantPresent() {
        Party applicant1 = PartyBuilder.builder().company().build();
        CaseData caseData = CaseDataBuilder.builder().applicant1(applicant1).build();

        JudgeFinalOrderForm.JudgeFinalOrderFormBuilder builder = JudgeFinalOrderForm.builder();
        builder = attendeesRepresentationPopulator.populateAttendeesDetails(builder, caseData);

        JudgeFinalOrderForm form = builder.build();
        Assertions.assertNull(form.getClaimantTwoAttendsOrRepresented());
    }

    @Test
    void shouldReturnCorrectClaimantText_ForFirstClaimant() {
        CaseData caseData = CaseDataBuilder.builder().applicant1(PartyBuilder.builder().company().build()).build();

        when(claimantAttendsOrRepresentedTextBuilder.claimantBuilder(caseData, false)).thenReturn("Claimant 1 attends");

        String result = attendeesRepresentationPopulator.generateClaimantAttendsOrRepresentedText(caseData, false);

        Assertions.assertEquals("Claimant 1 attends", result);
    }

    @Test
    void shouldReturnCorrectOtherRepresentedText_WhenDetailsArePresent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged().build().toBuilder()
            .finalOrderRepresentation(FinalOrderRepresentation.builder()
                                          .typeRepresentationList(FinalOrderRepresentationList.CLAIMANT_AND_DEFENDANT)
                                          .typeRepresentationOtherComplex(ClaimantAndDefendantHeard
                                                                              .builder().detailsRepresentationText("Test").build()).build())
            .build();

        String result = attendeesRepresentationPopulator.getOtherRepresentedText(caseData);

        Assertions.assertEquals("Test", result);
    }

    @Test
    void shouldReturnEmptyString_WhenOtherRepresentationDetailsAreAbsent() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build();

        String result = attendeesRepresentationPopulator.getOtherRepresentedText(caseData);

        Assertions.assertEquals("", result);
    }

}
