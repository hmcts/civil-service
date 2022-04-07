package uk.gov.hmcts.reform.civil.model.dq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class Applicant1DQTest extends DQTest {

    @Test
    void shouldGetVariables_usingInterfaceMethods() {
        Applicant1DQ dq = buildApplicant1Dq();

        assertEquals(disclosureOfElectronicDocuments(), dq.getDisclosureOfElectronicDocuments());
        assertEquals(disclosureOfNonElectronicDocuments(), dq.getDisclosureOfNonElectronicDocuments());
        assertEquals(disclosureReport(), dq.getDisclosureReport());
        assertEquals(draftDirections(), dq.getDraftDirections());
        assertEquals(experts(), dq.getExperts());
        assertEquals(fileDirectionsQuestionnaire(), dq.getFileDirectionQuestionnaire());
        assertEquals(furtherInformation(), dq.getFurtherInformation());
        assertEquals(hearing(), dq.getHearing());
        assertEquals(hearingSupport(), dq.getHearingSupport());
        assertNull(dq.getRequestedCourt());
        assertEquals(statementOfTruth(), dq.getStatementOfTruth());
        assertEquals(witnesses(), dq.getWitnesses());
        assertEquals(welshLanguageRequirements(), dq.getWelshLanguageRequirements());
        assertEquals(vulnerabilityQuestions(), dq.getVulnerabilityQuestions());
    }

    private Applicant1DQ buildApplicant1Dq() {
        return Applicant1DQ.builder()
            .applicant1DQDisclosureOfElectronicDocuments(disclosureOfElectronicDocuments())
            .applicant1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronicDocuments())
            .applicant1DQDisclosureReport(disclosureReport())
            .applicant1DQDraftDirections(draftDirections())
            .applicant1DQExperts(experts())
            .applicant1DQFileDirectionsQuestionnaire(fileDirectionsQuestionnaire())
            .applicant1DQFurtherInformation(furtherInformation())
            .applicant1DQHearing(hearing())
            .applicant1DQHearingSupport(hearingSupport())
            .applicant1DQRequestedCourt(requestedCourt())
            .applicant1DQStatementOfTruth(statementOfTruth())
            .applicant1DQWitnesses(witnesses())
            .applicant1DQLanguage(welshLanguageRequirements())
            .applicant1DQVulnerabilityQuestions(vulnerabilityQuestions())
            .build();
    }

    @Nested
    class GetExperts {

        @Test
        void shouldRemoveDetails_whenNoExpertsRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQExperts(dq.getExperts().toBuilder()
                                          .expertRequired(NO)
                                          .build())
                .build();

            assertThat(dq.getExperts().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenExpertsRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQExperts(dq.getExperts().toBuilder()
                                          .expertRequired(YES)
                                          .build())
                .build();

            assertThat(dq.getExperts()).isEqualTo(experts());
        }

        @Test
        void shouldReturnNull_whenExpertsIsNull() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQExperts(null)
                .build();

            assertThat(dq.getExperts()).isNull();
        }
    }

    @Nested
    class GetWitnesses {

        @Test
        void shouldRemoveDetails_whenNoWitnessesToAppear() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQWitnesses(dq.getWitnesses().toBuilder()
                                            .witnessesToAppear(NO)
                                            .build())
                .build();

            assertThat(dq.getWitnesses().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenWitnessesToAppear() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQWitnesses(dq.getWitnesses().toBuilder()
                                            .witnessesToAppear(YES)
                                            .build())
                .build();

            assertThat(dq.getWitnesses()).isEqualTo(witnesses());
        }

        @Test
        void shouldReturnNull_whenWitnessesIsNull() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQWitnesses(null)
                .build();

            assertThat(dq.getWitnesses()).isNull();
        }
    }

    @Nested
    class GetHearing {

        @Test
        void shouldRemoveUnavailableDates_whenNoUnavailableDatesRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQHearing(dq.getHearing().toBuilder()
                                          .unavailableDatesRequired(NO)
                                          .build())
                .build();

            assertThat(dq.getHearing().getUnavailableDates()).isNull();
        }

        @Test
        void shouldNotRemoveUnavailableDates_whenUnavailableDatesRequired() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQHearing(dq.getHearing().toBuilder()
                                          .unavailableDatesRequired(YES)
                                          .build())
                .build();

            assertThat(dq.getHearing().getUnavailableDates()).isEqualTo(hearing().getUnavailableDates());
        }

        @Test
        void shouldReturnNull_whenUnavailableDatesIsNull() {
            Applicant1DQ dq = buildApplicant1Dq();
            dq = dq.toBuilder()
                .applicant1DQHearing(null)
                .build();

            assertThat(dq.getHearing()).isNull();
        }
    }
}
