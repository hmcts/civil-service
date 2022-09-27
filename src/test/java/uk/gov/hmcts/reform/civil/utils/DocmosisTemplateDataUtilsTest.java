package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.fetchSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils.toCaseName;

class DocmosisTemplateDataUtilsTest {

    @Test
    void shouldReturnCaseName_whenBothPartiesAreIndividuals() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. Sam Clark")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
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
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant2(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. White Clark")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. Alex Richards")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("1 Mr. Sam Clark & 2 Mr. White Clark \nvs Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenMultiRespondent() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. Sam Clark")
                            .build())
            .respondent2(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. White Richards")
                             .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. Alex Richards")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. Sam Clark \nvs 1 Mr. Alex Richards & 2 Mr. White Richards");
    }

    @Test
    void shouldReturnCaseName_whenApplicantIsSoleTrader() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.SOLE_TRADER)
                            .partyName("Mrs. Georgina Hammersmith")
                            .soleTraderTradingAs("EuroStar")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
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
                            .partyName("Mr. White Richards")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.SOLE_TRADER)
                             .partyName("Mr. Boris Johnson")
                             .soleTraderTradingAs("UberFlip")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. White Richards \nvs Mr. Boris Johnson T/A UberFlip");
    }

    @Test
    void shouldReturnCaseName_whenBothAreSoleTrader() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.SOLE_TRADER)
                            .partyName("Mrs. Georgina Hammersmith")
                            .soleTraderTradingAs("EuroStar")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.SOLE_TRADER)
                             .partyName("Mr. Boris Johnson")
                             .soleTraderTradingAs("UberFlip")
                             .build())
            .build();

        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mrs. Georgina Hammersmith T/A EuroStar \nvs Mr. Boris Johnson T/A UberFlip");
    }

    @Test
    void shouldReturnCaseName_whenRespondentHasLitigationFriend() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. Sam Clark")
                            .build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. Alex Richards")
                             .build())
            .respondent1LitigationFriend(LitigationFriend.builder().fullName("Mr. Litigation Friend").build())
            .build();
        String caseName = toCaseName.apply(caseData);
        assertThat(caseName)
            .isEqualTo("Mr. Sam Clark \nvs Mr. Alex Richards (proceeding by L/F Mr. Litigation Friend)");
    }

    @Test
    void shouldReturnCaseName_whenApplicantHasLitigationFriend() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant1LitigationFriend(LitigationFriend.builder().fullName("Mr. Litigation Friend").build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. Alex Richards")
                             .build())
            .build();
        String caseName = toCaseName.apply(caseData);
        assertThat(caseName)
            .isEqualTo("Mr. Sam Clark (proceeding by L/F Mr. Litigation Friend) \nvs Mr. Alex Richards");
    }

    @Test
    void shouldReturnCaseName_whenBothHasLitigationFriend() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Mr. Sam Clark")
                            .build())
            .applicant1LitigationFriend(LitigationFriend.builder().fullName("Mr. Applicant Friend").build())
            .respondent1(Party.builder()
                             .type(Party.Type.INDIVIDUAL)
                             .partyName("Mr. Alex Richards")
                             .build())
            .respondent1LitigationFriend(LitigationFriend.builder().fullName("Mr. Respondent Friend").build())
            .build();
        String caseName = toCaseName.apply(caseData);
        assertThat(caseName).isEqualTo("Mr. Sam Clark (proceeding by L/F Mr. Applicant Friend) \nvs Mr. Alex Richards "
                                           + "(proceeding by L/F Mr. Respondent Friend)");
    }

    @Nested
    class FetchSolicitorReferences {

        @Test
        void shouldPopulateNotProvided_whenSolicitorReferencesIsNull() {
            SolicitorReferences solicitorReferences = null;
            SolicitorReferences result = fetchSolicitorReferences(solicitorReferences);
            assertAll(
                "SolicitorReferences not provided",
                () -> assertEquals("Not Provided", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor1Reference())
            );
        }

        @Test
        void shouldPopulateNotProvided_whenSolicitorReferencesMissing() {
            SolicitorReferences solicitorReferences = SolicitorReferences.builder().build();
            SolicitorReferences result = fetchSolicitorReferences(solicitorReferences);
            assertAll(
                "SolicitorReferences not provided",
                () -> assertEquals("Not Provided", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor1Reference())
            );
        }

        @Test
        void shouldPopulateProvidedValues_whenSolicitorReferencesAvailable() {
            SolicitorReferences solicitorReferences = SolicitorReferences
                .builder()
                .applicantSolicitor1Reference("Applicant ref")
                .respondentSolicitor1Reference("Respondent ref")
                .build();

            SolicitorReferences result = fetchSolicitorReferences(solicitorReferences);
            assertAll(
                "SolicitorReferences provided",
                () -> assertEquals("Applicant ref", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Respondent ref", result.getRespondentSolicitor1Reference())
            );
        }

        @Test
        void shouldPopulateNotProvided_whenOneReferencesNotAvailable() {
            SolicitorReferences solicitorReferences = SolicitorReferences
                .builder()
                .applicantSolicitor1Reference("Applicant ref")
                .build();

            SolicitorReferences result = fetchSolicitorReferences(solicitorReferences);

            assertAll(
                "SolicitorReferences one is provided",
                () -> assertEquals("Applicant ref", result.getApplicantSolicitor1Reference()),
                () -> assertEquals("Not Provided", result.getRespondentSolicitor1Reference())
            );
        }
    }
}
