package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.fetchApplicantName;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.fetchSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.formatCcdCaseReference;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.toCaseName;

class DocmosisTemplateDataUtilsTest {

    @Test
    void shouldReturnNull_whenBothApplicant1NotFount() throws IOException {
        CaseData caseData = CaseData.builder()
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .partyName("Mr. Alex Richards")
                             .build())
            .ccdCaseReference(1L)
            .build();
        assertThatThrownBy(() -> fetchApplicantName(caseData)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnCaseName_whenBothPartiesAreIndividuals() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .partyName("Mr. Alex Richards")
                             .build())
            .build();
        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. Sam Clark vs Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenMultiApplicant() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant2(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("White")
                            .individualLastName("Clark")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("1 Mr. Sam Clark & 2 Mr. White Clark \nvs Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenMultiApplicantAndApplicant2HaveLF() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant2(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("White")
                            .individualLastName("Clark")
                            .build())
            .applicant2LitigationFriend(new LitigationFriend().setFirstName("Litigation")
                                            .setLastName("Friend"))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("1 Mr. Sam Clark & 2 Mr. White Clark (proceeding by L/F Litigation Friend) \nvs Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenMultiApplicantAndApplicant1HaveLF() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant1LitigationFriend(new LitigationFriend().setFirstName("Litigation")
                                            .setLastName("Friend"))
            .applicant2(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("White")
                            .individualLastName("Clark")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("1 Mr. Sam Clark (proceeding by L/F Litigation Friend) & 2 Mr. White Clark \nvs Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenMultiApplicantAndBothApplicantsHaveLF() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant1LitigationFriend(new LitigationFriend().setFirstName("Litigation")
                                            .setLastName("Friend"))
            .applicant2(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("White")
                            .individualLastName("Clark")
                            .build())
            .applicant2LitigationFriend(new LitigationFriend().setFirstName("Litigation")
                                            .setLastName("Friend"))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("1 Mr. Sam Clark (proceeding by L/F Litigation Friend) & 2 Mr. White Clark (proceeding by L/F Litigation Friend) \nvs Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenMultiApplicantAndBothApplicantsAndRespondent1HaveLF() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant1LitigationFriend(new LitigationFriend().setFirstName("Litigation")
                                            .setLastName("Friend"))
            .applicant2(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("White")
                            .individualLastName("Clark")
                            .build())
            .applicant2LitigationFriend(new LitigationFriend().setFirstName("Litigation")
                                            .setLastName("Friend"))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .build())
            .respondent1LitigationFriend(new LitigationFriend().setFirstName("Respondent Litigation")
                                             .setLastName("Friend"))
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("1 Mr. Sam Clark (proceeding by L/F Litigation Friend) & " +
                                           "2 Mr. White Clark (proceeding by L/F Litigation Friend) \nvs " +
                                           "Mr. Alex Richards (proceeding by L/F Respondent Litigation Friend)");
    }

    @Test
    void shouldReturnCaseName_whenMultiRespondentAndBothApplicantAndRespondentsHaveLF() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant1LitigationFriend(new LitigationFriend().setFirstName("Litigation")
                                            .setLastName("Friend"))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .build())
            .respondent1LitigationFriend(new LitigationFriend().setFirstName("Respondent Litigation")
                                             .setLastName("Friend"))
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("White")
                             .individualLastName("Richards")
                             .build())
            .respondent2LitigationFriend(new LitigationFriend().setFirstName("Respondent2 Litigation")
                                        .setLastName("Friend"))
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. Sam Clark (proceeding by L/F Litigation Friend) \n" +
                                           "vs 1 Mr. Alex Richards (proceeding by L/F Respondent Litigation Friend) &" +
                                           " 2 Mr. White Richards (proceeding by L/F Respondent2 Litigation Friend)");
    }

    @Test
    void shouldReturnCaseName_whenMultiRespondent() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .partyName("Ms. Irrelevant name")
                            .build())
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("White")
                             .individualLastName("Richards")
                             .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("King")
                             .partyName("Mr. Alex King")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. Sam Clark \nvs 1 Mr. Alex King & 2 Mr. White Richards");
    }

    @Test
    void shouldReturnCaseName_whenApplicantIsSoleTrader() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.SOLE_TRADER)
                            .soleTraderTitle("Mrs.")
                            .soleTraderFirstName("Georgina")
                            .soleTraderLastName("Hammersmith")
                            .soleTraderTradingAs("EuroStar")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .partyName("Mr. Alex Richards")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mrs. Georgina Hammersmith T/A EuroStar \nvs Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenRespondentIsSoleTrader() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("White")
                            .individualLastName("Richards")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.SOLE_TRADER)
                             .soleTraderFirstName("Boris")
                             .soleTraderLastName("Johnson")
                             .soleTraderTradingAs("UberFlip")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. White Richards \nvs Boris Johnson T/A UberFlip");
    }

    @Test
    void shouldReturnCaseName_whenBothAreSoleTrader() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.SOLE_TRADER)
                            .soleTraderFirstName("Georgina")
                            .soleTraderLastName("Hammersmith")
                            .soleTraderTradingAs("EuroStar")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.SOLE_TRADER)
                             .soleTraderTitle("Mr.")
                             .soleTraderFirstName("Sean")
                             .soleTraderLastName("White")
                             .partyName("Mr. Boris Johnson")
                             .soleTraderTradingAs("UberFlip")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Georgina Hammersmith T/A EuroStar \nvs Mr. Sean White T/A UberFlip");
    }

    @Test
    void shouldReturnCaseName_whenRespondentHasLitigationFriend() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .partyName("Mr. Sam Clark")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .partyName("Mr. Other Party")
                             .build())
            .respondent1LitigationFriend(new LitigationFriend().setFirstName("Litigation")
                                             .setLastName("Friend"))
            .build();
        String caseName = toCaseName.apply(caseData);
        assertThat(caseName)
            .isEqualTo("Mr. Sam Clark \nvs Mr. Alex Richards (proceeding by L/F Litigation Friend)");
    }

    @Test
    void shouldReturnCaseName_whenApplicantHasLitigationFriend() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .build())
            .applicant1LitigationFriend(new LitigationFriend().setFirstName("Litigation")
                                            .setLastName("Friend"))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .partyName("Mr. Alex Richards")
                             .build())
            .build();
        String caseName = toCaseName.apply(caseData);
        assertThat(caseName)
            .isEqualTo("Mr. Sam Clark (proceeding by L/F Litigation Friend) \nvs Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenBothHasLitigationFriend() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualTitle("Mr.")
                            .individualFirstName("Sam")
                            .individualLastName("Clark")
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant1LitigationFriend(new LitigationFriend().setFirstName("Applicant")
                                            .setLastName("Friend"))
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .individualTitle("Mr.")
                             .individualFirstName("Alex")
                             .individualLastName("Richards")
                             .build())
            .respondent1LitigationFriend(new LitigationFriend().setFirstName("Respondent")
                                             .setLastName("Friend"))
            .build();
        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. Sam Clark (proceeding by L/F Applicant Friend) \nvs Mr. Alex Richards "
                                           + "(proceeding by L/F Respondent Friend)");
    }

    @Nested
    class FetchSolicitorReferences {

        @Test
        void shouldPopulateNotProvided_whenSolicitorReferencesIsNull() {
            CaseData caseData = CaseData.builder()
                .solicitorReferences(null)
                .respondentSolicitor2Reference(null)
                .build();
            SolicitorReferences result = fetchSolicitorReferences(caseData);
            assertAll(
                "SolicitorReferences not provided",
                () -> assertEquals("Not Provided", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor2Reference())
            );
        }

        @Test
        void shouldPopulateNotProvided_whenSolicitorReferencesMissing() {
            SolicitorReferences solicitorReferences = new SolicitorReferences();
            CaseData caseData = CaseData.builder()
                .solicitorReferences(solicitorReferences)
                .respondentSolicitor2Reference(null)
                .build();
            SolicitorReferences result = fetchSolicitorReferences(caseData);
            assertAll(
                "SolicitorReferences not provided",
                () -> assertEquals("Not Provided", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor2Reference())
            );
        }

        @Test
        void shouldPopulateProvidedValues_whenSolicitorReferencesAvailable() {
            SolicitorReferences solicitorReferences = new SolicitorReferences()
                .setApplicantSolicitor1Reference("Applicant ref")
                .setRespondentSolicitor1Reference("Respondent ref");
            CaseData caseData = CaseData.builder()
                .solicitorReferences(solicitorReferences)
                .respondentSolicitor2Reference("Respondent 2 ref")
                .build();

            SolicitorReferences result = fetchSolicitorReferences(caseData);
            assertAll(
                "SolicitorReferences provided",
                () -> assertEquals("Applicant ref", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Respondent ref", result.getRespondentSolicitor1Reference()),
                () -> assertEquals("Respondent 2 ref", result.getRespondentSolicitor2Reference())
            );
        }

        @Test
        void shouldPopulateNotProvided_whenOneReferencesNotAvailable() {
            SolicitorReferences solicitorReferences = new SolicitorReferences()
                .setApplicantSolicitor1Reference("Applicant ref");
            CaseData caseData = CaseData.builder()
                .solicitorReferences(solicitorReferences)
                .build();

            SolicitorReferences result = fetchSolicitorReferences(caseData);

            assertAll(
                "SolicitorReferences one is provided",
                () -> assertEquals("Applicant ref", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor2Reference())
            );
        }
    }

    @Test
    void shouldReturnEmptyForMissingCaseReference() {
        CaseData caseData = CaseData.builder()
            .build();
        String formattedCaseReference = formatCcdCaseReference(caseData);
        assertThat(formattedCaseReference).isEmpty();
    }

    private static Stream<Arguments> testCaseReferenceData() {
        return Stream.of(
            Arguments.of(1111111111111111L, "1111-1111-1111-1111"),
            Arguments.of(11111111111111L, "1111-1111-1111-11"),
            Arguments.of(1111L, "1111"),
            Arguments.of(1L, "1")

        );
    }

    @ParameterizedTest
    @MethodSource("testCaseReferenceData")
    void shouldReturnFormattedReference(Long inputCaseReference, String expectedCaseReference) {
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(inputCaseReference)
            .build();
        String formattedCaseReference = formatCcdCaseReference(caseData);
        assertThat(formattedCaseReference).isNotEmpty();
        assertThat(formattedCaseReference).isEqualTo(expectedCaseReference);
    }
}
