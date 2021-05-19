package uk.gov.hmcts.reform.civil.model.dq;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class Respondent1DQTest extends DQTest {

    @Test
    void shouldGetVariables_usingInterfaceMethods() {
        Respondent1DQ dq = buildRespondent1Dq();

        assertEquals(disclosureOfElectronicDocuments(), dq.getDisclosureOfElectronicDocuments());
        assertEquals(disclosureOfNonElectronicDocuments(), dq.getDisclosureOfNonElectronicDocuments());
        assertEquals(disclosureReport(), dq.getDisclosureReport());
        assertEquals(draftDirections(), dq.getDraftDirections());
        assertEquals(experts(), dq.getExperts());
        assertEquals(fileDirectionsQuestionnaire(), dq.getFileDirectionQuestionnaire());
        assertEquals(furtherInformation(), dq.getFurtherInformation());
        assertEquals(hearing(), dq.getHearing());
        assertEquals(hearingSupport(), dq.getHearingSupport());
        assertEquals(requestedCourt(), dq.getRequestedCourt());
        assertEquals(statementOfTruth(), dq.getStatementOfTruth());
        assertEquals(witnesses(), dq.getWitnesses());
        assertEquals(welshLanguageRequirements(), dq.getWelshLanguageRequirements());
    }

    private Respondent1DQ buildRespondent1Dq() {
        return Respondent1DQ.builder()
            .respondent1DQDisclosureOfElectronicDocuments(disclosureOfElectronicDocuments())
            .respondent1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronicDocuments())
            .respondent1DQDisclosureReport(disclosureReport())
            .respondent1DQDraftDirections(draftDirections())
            .respondent1DQExperts(experts())
            .respondent1DQFileDirectionsQuestionnaire(fileDirectionsQuestionnaire())
            .respondent1DQFurtherInformation(furtherInformation())
            .respondent1DQHearing(hearing())
            .respondent1DQHearingSupport(hearingSupport())
            .respondent1DQRequestedCourt(requestedCourt())
            .respondent1DQStatementOfTruth(statementOfTruth())
            .respondent1DQWitnesses(witnesses())
            .respondent1DQLanguage(welshLanguageRequirements())
            .build();
    }

    @Nested
    class GetExperts {

        @Test
        void shouldRemoveDetails_whenNoExpertsRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQExperts(dq.getExperts().toBuilder()
                                          .expertRequired(NO)
                                          .build())
                .build();

            assertThat(dq.getExperts().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenExpertsRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQExperts(dq.getExperts().toBuilder()
                                          .expertRequired(YES)
                                          .build())
                .build();

            assertThat(dq.getExperts()).isEqualTo(experts());
        }

        @Test
        void shouldReturnNull_whenExpertsIsNull() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQExperts(null)
                .build();

            assertThat(dq.getExperts()).isNull();
        }
    }

    @Nested
    class GetWitnesses {

        @Test
        void shouldRemoveDetails_whenNoWitnessesToAppear() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQWitnesses(dq.getWitnesses().toBuilder()
                                          .witnessesToAppear(NO)
                                          .build())
                .build();

            assertThat(dq.getWitnesses().getDetails()).isNull();
        }

        @Test
        void shouldNotRemoveDetails_whenWitnessesToAppear() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQWitnesses(dq.getWitnesses().toBuilder()
                                          .witnessesToAppear(YES)
                                          .build())
                .build();

            assertThat(dq.getWitnesses()).isEqualTo(witnesses());
        }

        @Test
        void shouldReturnNull_whenWitnessesIsNull() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQWitnesses(null)
                .build();

            assertThat(dq.getWitnesses()).isNull();
        }
    }

    @Nested
    class GetHearing {

        @Test
        void shouldRemoveUnavailableDates_whenNoUnavailableDatesRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQHearing(dq.getHearing().toBuilder()
                                            .unavailableDatesRequired(NO)
                                            .build())
                .build();

            assertThat(dq.getHearing().getUnavailableDates()).isNull();
        }

        @Test
        void shouldNotRemoveUnavailableDates_whenUnavailableDatesRequired() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQHearing(dq.getHearing().toBuilder()
                                            .unavailableDatesRequired(YES)
                                            .build())
                .build();

            assertThat(dq.getHearing().getUnavailableDates()).isEqualTo(hearing().getUnavailableDates());
        }

        @Test
        void shouldReturnNull_whenUnavailableDatesIsNull() {
            Respondent1DQ dq = buildRespondent1Dq();
            dq = dq.toBuilder()
                .respondent1DQHearing(null)
                .build();

            assertThat(dq.getHearing()).isNull();
        }
    }
}
