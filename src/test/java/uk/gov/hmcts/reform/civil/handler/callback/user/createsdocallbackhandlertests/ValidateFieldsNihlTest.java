package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.ValidateFieldsNihl;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictNoOfWitnessDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2RestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ValidateFieldsNihlTest {

    @InjectMocks
    private ValidateFieldsNihl validateFieldsNihl;

    @Test
    void shouldReturnErrorForPastStandardDisclosureDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                                            .standardDisclosureDate(LocalDate.now().minusDays(1))
                                            .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastAddendumReportDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2AddendumReport(SdoR2AddendumReport.builder()
                                     .sdoAddendumReportDate(LocalDate.now().minusDays(2))
                                     .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForNegativeWitnessClaimantCount() {
        CaseData caseData = CaseData.builder()
            .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                                      .sdoWitnessDeadlineDate(LocalDate.now().plusDays(10))
                                      .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                                                .restrictNoOfWitnessDetails(SdoR2RestrictNoOfWitnessDetails.builder()
                                                                                                .noOfWitnessClaimant(-1)
                                                                                                .noOfWitnessDefendant(2)
                                                                                                .build())
                                                                .build())
                                      .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("The number entered cannot be less than zero", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastClaimantShallUndergoDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2FurtherAudiogram(SdoR2FurtherAudiogram.builder()
                                       .sdoClaimantShallUndergoDate(LocalDate.now().minusDays(3))
                                       .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnNoErrorsForValidDatesAndCounts() {
        CaseData caseData = CaseData.builder()
            .sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                                            .standardDisclosureDate(LocalDate.now().plusDays(1))
                                            .build())
            .sdoR2FurtherAudiogram(SdoR2FurtherAudiogram.builder()
                                       .sdoClaimantShallUndergoDate(LocalDate.now().plusDays(1))
                                       .build())
            .sdoR2WitnessesOfFact(SdoR2WitnessOfFact.builder()
                                      .sdoWitnessDeadlineDate(LocalDate.now().plusDays(20))
                                      .sdoR2RestrictWitness(SdoR2RestrictWitness.builder()
                                                                .restrictNoOfWitnessDetails(SdoR2RestrictNoOfWitnessDetails.builder()
                                                                                                .noOfWitnessClaimant(1)
                                                                                                .noOfWitnessDefendant(1)
                                                                                                .build())
                                                                .build())
                                      .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(0, errors.size());
    }

    @Test
    void shouldReturnErrorForPastInspectionDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2DisclosureOfDocuments(SdoR2DisclosureOfDocuments.builder()
                                            .inspectionDate(LocalDate.now().minusDays(1))
                                            .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastServiceReportDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2FurtherAudiogram(SdoR2FurtherAudiogram.builder()
                                       .sdoServiceReportDate(LocalDate.now().minusDays(1))
                                       .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastDefendantMayAskDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                                              .sdoDefendantMayAskDate(LocalDate.now().minusDays(1))
                                              .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastQuestionsShallBeAnsweredDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2QuestionsClaimantExpert(SdoR2QuestionsClaimantExpert.builder()
                                              .sdoQuestionsShallBeAnsweredDate(LocalDate.now().minusDays(1))
                                              .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastJointMeetingOfExpertsDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2PermissionToRelyOnExpert(SdoR2PermissionToRelyOnExpert.builder()
                                               .sdoJointMeetingOfExpertsDate(LocalDate.now().minusDays(1))
                                               .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastInstructionOfTheExpertDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder()
                                               .sdoInstructionOfTheExpertDate(LocalDate.now().minusDays(1))
                                               .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastExpertReportDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder()
                                               .sdoExpertReportDate(LocalDate.now().minusDays(1))
                                               .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastWrittenQuestionsDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder()
                                               .sdoWrittenQuestionsDate(LocalDate.now().minusDays(1))
                                               .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastRepliesDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2EvidenceAcousticEngineer(SdoR2EvidenceAcousticEngineer.builder()
                                               .sdoRepliesDate(LocalDate.now().minusDays(1))
                                               .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastWrittenQuestionsDateToEntExpert() {
        CaseData caseData = CaseData.builder()
            .sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                                           .sdoWrittenQuestionsDate(LocalDate.now().minusDays(1))
                                           .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastQuestionsShallBeAnsweredDateToEntExpert() {
        CaseData caseData = CaseData.builder()
            .sdoR2QuestionsToEntExpert(SdoR2QuestionsToEntExpert.builder()
                                           .sdoQuestionsShallBeAnsweredDate(LocalDate.now().minusDays(1))
                                           .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastScheduleOfLossClaimantDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder()
                                     .sdoR2ScheduleOfLossClaimantDate(LocalDate.now().minusDays(1))
                                     .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastScheduleOfLossDefendantDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2ScheduleOfLoss(SdoR2ScheduleOfLoss.builder()
                                     .sdoR2ScheduleOfLossDefendantDate(LocalDate.now().minusDays(1))
                                     .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastTrialFirstOpenDateAfter() {
        CaseData caseData = CaseData.builder()
            .sdoR2Trial(SdoR2Trial.builder()
                            .sdoR2TrialWindow(SdoR2TrialWindow.builder().build())
                            .sdoR2TrialFirstOpenDateAfter(SdoR2TrialFirstOpenDateAfter.builder()
                                                              .listFrom(LocalDate.now().minusDays(1))
                                                              .build())
                            .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        // Assert
        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastTrialWindowListFrom() {
        CaseData caseData = CaseData.builder()
            .sdoR2Trial(SdoR2Trial.builder()
                            .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                                                  .listFrom(LocalDate.now().minusDays(1))
                                                  .build())
                            .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastTrialWindowDateTo() {
        CaseData caseData = CaseData.builder()
            .sdoR2Trial(SdoR2Trial.builder()
                            .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                                                  .dateTo(LocalDate.now().minusDays(1))
                                                  .build())
                            .build())
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }

    @Test
    void shouldReturnErrorForPastImportantNotesDate() {
        CaseData caseData = CaseData.builder()
            .sdoR2ImportantNotesDate(LocalDate.now().minusDays(1))
            .build();

        ArrayList<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        assertEquals(1, errors.size());
        assertEquals("Date must be in the future", errors.get(0));
    }
}
