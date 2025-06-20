package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseName;

class CaseNameUtilsTest {

    @Nested
    class GetFormattedPartyName {

        @Test
        void shouldReturnExpectedPartyName_withValidParty() {
            var party = Party.builder()
                .individualFirstName("Applicant")
                .individualLastName("One")
                .type(Party.Type.INDIVIDUAL).build();

            var actual = CaseNameUtils.getFormattedPartyName(party);

            assertEquals("Applicant One", actual);
        }

        @Test
        void shouldReturnEmptyString_withNullParty() {
            var actual = CaseNameUtils.getFormattedPartyName(null);

            assertEquals("", actual);
        }
    }

    @Nested
    class GetFormattedLitigationFriendName {

        @Test
        void shouldReturnExpectedLitigationFriendName_withValidLitigationFriend() {
            var litigationFriend = LitigationFriend.builder().firstName("Litigation").lastName("Friend").build();

            var actual = CaseNameUtils.getFormattedLitigationFriendName(litigationFriend);

            assertEquals(" represented by Litigation Friend (litigation friend)", actual);
        }

        @Test
        void shouldReturnEmptyString_withNullLitigationFriend() {
            var actual = CaseNameUtils.getFormattedLitigationFriendName(null);

            assertEquals("", actual);
        }
    }

    @Nested
    class BuildCaseName {

        Party applicant1;
        Party applicant2;
        Party respondent1;
        Party respondent2;
        LitigationFriend applicant1LitigationFriend;
        LitigationFriend applicant2LitigationFriend;
        LitigationFriend respondent1LitigationFriend;
        LitigationFriend respondent2LitigationFriend;

        @BeforeEach
        void setupParties() {
            applicant1 = Party.builder()
                .individualFirstName("Applicant")
                .individualLastName("One")
                .type(Party.Type.INDIVIDUAL).build();

            applicant1LitigationFriend = LitigationFriend.builder()
                .firstName("ApplicantOne")
                .lastName("LitigationFriend")
                .build();

            applicant2 = Party.builder()
                .individualFirstName("Applicant")
                .individualLastName("Two")
                .type(Party.Type.INDIVIDUAL)
                .build();

            applicant2LitigationFriend = LitigationFriend.builder()
                .firstName("ApplicantTwo")
                .lastName("LitigationFriend")
                .build();

            respondent1 = Party.builder()
                .individualFirstName("Respondent")
                .individualLastName("One")
                .type(Party.Type.INDIVIDUAL)
                .build();

            respondent1LitigationFriend = LitigationFriend.builder()
                .firstName("RespondentOne")
                .lastName("LitigationFriend")
                .build();

            respondent2 = Party.builder()
                .individualFirstName("Respondent")
                .individualLastName("Two")
                .type(Party.Type.INDIVIDUAL)
                .build();

            respondent2LitigationFriend = LitigationFriend.builder()
                .firstName("RespondentTwo")
                .lastName("LitigationFriend")
                .build();
        }

        @Test
        void shouldReturnCaseName_when1v1() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One v Respondent One");
        }

        @Test
        void shouldReturnCaseName_when1v1_withApplicantLipFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo(
                "Applicant One represented by ApplicantOne LitigationFriend (litigation friend) v Respondent One");
        }

        @Test
        void shouldReturnCaseName_when1v1_withRespondentLipFriend() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .addRespondent1LitigationFriend()
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo(
                "John Rambo v Sole Trader represented by Litigation Friend (litigation friend)");
        }

        @Test
        void shouldReturnCaseName_when1v2() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One v Respondent One, Respondent Two");
        }

        @Test
        void shouldReturnCaseName_when1v2_withApplicantLipFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo(
                "Applicant One represented by ApplicantOne LitigationFriend (litigation friend) v Respondent One, Respondent Two");
        }

        @Test
        void shouldReturnCaseName_when1v2_withRespondent1LipFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .respondent1LitigationFriend(respondent1LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo(
                "Applicant One v Respondent One represented by RespondentOne LitigationFriend (litigation friend), Respondent Two");
        }

        @Test
        void shouldReturnCaseName_when1v2_withRespondent2LipFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .respondent2LitigationFriend(respondent2LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo(
                "Applicant One v Respondent One, Respondent Two represented by RespondentTwo LitigationFriend (litigation friend)");
        }

        @Test
        void shouldReturnCaseName_when1v2_withApplicantLipFriend_andRespondent1LipFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .respondent1LitigationFriend(respondent1LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo(
                "Applicant One represented by ApplicantOne LitigationFriend (litigation friend)" +
                    " v Respondent One represented by RespondentOne LitigationFriend (litigation friend), Respondent Two");
        }

        @Test
        void shouldReturnCaseName_when1v2_withApplicantLipFriend_andRespondent2LipFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .respondent2LitigationFriend(respondent2LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo(
                "Applicant One represented by ApplicantOne LitigationFriend (litigation friend)" +
                    " v Respondent One, Respondent Two represented by RespondentTwo LitigationFriend (litigation friend)");
        }

        @Test
        void shouldReturnCaseName_when1v2_withRespondent1LipFriend_andRespondent2LipFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .respondent1LitigationFriend(respondent1LitigationFriend)
                .respondent2LitigationFriend(respondent2LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One v Respondent One represented by RespondentOne LitigationFriend" +
                                             " (litigation friend), Respondent Two represented by RespondentTwo LitigationFriend (litigation friend)");
        }

        @Test
        void shouldReturnCaseName_when1v2_withApplicantLipFriend_andRespondent1LipFriend_andRespondent2LipFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .respondent1(respondent1)
                .respondent2(respondent2)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .respondent1LitigationFriend(respondent1LitigationFriend)
                .respondent2LitigationFriend(respondent2LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo(
                "Applicant One represented by ApplicantOne LitigationFriend (litigation friend) v Respondent One represented by" +
                    " RespondentOne LitigationFriend (litigation friend), Respondent Two represented by RespondentTwo LitigationFriend (litigation friend)");
        }

        @Test
        void shouldReturnCaseName_when2v1() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One, Applicant Two v Respondent One");
        }

        @Test
        void shouldReturnCaseName_when2v1_withApplicant1LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One represented by ApplicantOne LitigationFriend (litigation friend), Applicant Two v Respondent One");
        }

        @Test
        void shouldReturnCaseName_when2v1_withApplicant2LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .applicant2LitigationFriend(applicant2LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One, Applicant Two represented by ApplicantTwo LitigationFriend (litigation friend) v Respondent One");
        }

        @Test
        void shouldReturnCaseName_when2v1_withRespondent1LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .respondent1LitigationFriend(respondent1LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One, Applicant Two v Respondent One represented by RespondentOne LitigationFriend (litigation friend)");
        }

        @Test
        void shouldReturnCaseName_when2v1_withApplicant1LitFriend_andApplicant2LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .applicant2LitigationFriend(applicant2LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One represented by ApplicantOne LitigationFriend (litigation friend)," +
                                             " Applicant Two represented by ApplicantTwo LitigationFriend (litigation friend) v Respondent One");
        }

        @Test
        void shouldReturnCaseName_when2v1_withApplicant1LitFriend_andRespondent1LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .respondent1LitigationFriend(respondent1LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One represented by ApplicantOne LitigationFriend (litigation friend)," +
                                             " Applicant Two v Respondent One represented by RespondentOne LitigationFriend (litigation friend)");
        }

        @Test
        void shouldReturnCaseName_when2v1_withApplicant2LitFriend_andRespondent1LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .applicant2LitigationFriend(applicant2LitigationFriend)
                .respondent1LitigationFriend(respondent1LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One, Applicant Two represented by ApplicantTwo LitigationFriend (litigation friend)" +
                                             " v Respondent One represented by RespondentOne LitigationFriend (litigation friend)");
        }

        @Test
        void shouldReturnCaseName_when2v1_withApplicant1LitFriend_andApplicant2LitFriend_andRespondent1LitFriend() {
            CaseData caseData = CaseData.builder()
                .applicant1(applicant1)
                .applicant2(applicant2)
                .respondent1(respondent1)
                .applicant1LitigationFriend(applicant1LitigationFriend)
                .applicant2LitigationFriend(applicant2LitigationFriend)
                .respondent1LitigationFriend(respondent1LitigationFriend)
                .build();

            String actual = buildCaseName(caseData);

            assertThat(actual).isEqualTo("Applicant One represented by ApplicantOne LitigationFriend (litigation friend)," +
                                             " Applicant Two represented by ApplicantTwo LitigationFriend (litigation friend) " +
                                             "v Respondent One represented by RespondentOne LitigationFriend (litigation friend)");
        }
    }
}
